package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.model.ContactRequest
import com.example.model.User
import com.example.utils.Constants
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val database: FirebaseDatabase? = runCatching { FirebaseDatabase.getInstance() }.getOrNull()

    private val _currentUserState = MutableStateFlow<User?>(null)
    val currentUserState: StateFlow<User?> = _currentUserState

    // In-memory contact sets fallback
    private val localContacts = mutableMapOf<String, MutableSet<String>>()
    private val localRequests = mutableMapOf<String, MutableList<ContactRequest>>()

    val demoUsers = emptyList<User>()

    init {
        checkCurrentSession()
    }

    fun checkCurrentSession() {
        val fbUser = auth?.currentUser
        if (fbUser != null) {
            val user = User(
                uid = fbUser.uid,
                displayName = fbUser.displayName ?: "FaceTime User",
                email = fbUser.email ?: "",
                photoUrl = fbUser.photoUrl?.toString() ?: "",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            _currentUserState.value = user
            syncUserToFirebase(user)
        } else {
            _currentUserState.value = null
        }
    }

    suspend fun signInWithGoogle(
        activityContext: Context,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(Constants.WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activityContext)

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val currentAuth = auth
                if (currentAuth != null) {
                    currentAuth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fbUser = currentAuth.currentUser
                            val user = User(
                                uid = fbUser?.uid ?: "user_${System.currentTimeMillis() % 100000}",
                                displayName = fbUser?.displayName ?: googleIdTokenCredential.displayName ?: "Family User",
                                email = fbUser?.email ?: googleIdTokenCredential.id ?: "",
                                photoUrl = fbUser?.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                                isOnline = true,
                                lastSeen = System.currentTimeMillis()
                            )
                            _currentUserState.value = user
                            syncUserToFirebase(user)
                            onSuccess(user)
                        } else {
                            onError(task.exception?.localizedMessage ?: "Firebase Google Sign-In failed")
                        }
                    }
                } else {
                    val user = User(
                        uid = "user_${System.currentTimeMillis() % 100000}",
                        displayName = googleIdTokenCredential.displayName ?: "Family User",
                        email = googleIdTokenCredential.id ?: "",
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                        isOnline = true,
                        lastSeen = System.currentTimeMillis()
                    )
                    _currentUserState.value = user
                    syncUserToFirebase(user)
                    onSuccess(user)
                }
            } else {
                onError("Unsupported credential response")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "1-Tap Google Sign-In failed: ${e.message}", e)
            onError(e.localizedMessage ?: "Google Sign-In error")
        }
    }

    fun signInWithCustomName(name: String, onResult: () -> Unit = {}) {
        val currentAuth = auth
        val displayName = name.ifBlank { "Family User" }
        if (currentAuth != null) {
            currentAuth.signInAnonymously().addOnCompleteListener { task ->
                val fbUser = currentAuth.currentUser
                val user = if (task.isSuccessful && fbUser != null) {
                    User(
                        uid = fbUser.uid,
                        displayName = displayName,
                        email = fbUser.email ?: "${displayName.lowercase().replace(" ", ".")}@familycall.app",
                        photoUrl = fbUser.photoUrl?.toString() ?: "",
                        isOnline = true,
                        lastSeen = System.currentTimeMillis()
                    )
                } else {
                    User(
                        uid = "user_${System.currentTimeMillis() % 100000}",
                        displayName = displayName,
                        email = "${displayName.lowercase().replace(" ", ".")}@familycall.app",
                        photoUrl = "",
                        isOnline = true,
                        lastSeen = System.currentTimeMillis()
                    )
                }
                _currentUserState.value = user
                syncUserToFirebase(user)
                onResult()
            }
        } else {
            val user = User(
                uid = "user_${System.currentTimeMillis() % 100000}",
                displayName = displayName,
                email = "${displayName.lowercase().replace(" ", ".")}@familycall.app",
                photoUrl = "",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            _currentUserState.value = user
            syncUserToFirebase(user)
            onResult()
        }
    }

    fun syncUserToFirebase(user: User) {
        val db = database ?: return
        if (user.uid.isEmpty()) return
        runCatching {
            val userRef = db.getReference(Constants.NODE_USERS).child(user.uid)
            val map = mapOf(
                "uid" to user.uid,
                "displayName" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "aboutStatus" to user.aboutStatus,
                "isOnline" to true,
                "lastSeen" to System.currentTimeMillis()
            )
            userRef.updateChildren(map)
        }.onFailure {
            Log.e("AuthRepository", "Firebase sync failed: ${it.message}")
        }
    }

    fun updateProfile(displayName: String, aboutStatus: String, photoUrl: String) {
        val current = _currentUserState.value ?: return
        val updated = current.copy(
            displayName = displayName,
            aboutStatus = aboutStatus,
            photoUrl = photoUrl
        )
        _currentUserState.value = updated
        syncUserToFirebase(updated)
    }

    fun setPresence(isOnline: Boolean) {
        val current = _currentUserState.value ?: return
        val updated = current.copy(
            isOnline = isOnline,
            lastSeen = System.currentTimeMillis()
        )
        _currentUserState.value = updated
        val db = database
        if (db != null && current.uid.isNotEmpty()) {
            runCatching {
                val presenceRef = db.getReference(Constants.NODE_USERS)
                    .child(current.uid)
                presenceRef.child("isOnline").setValue(isOnline)
                presenceRef.child("lastSeen").setValue(System.currentTimeMillis())
            }
        }
    }

    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val db = database
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val ref = db.getReference(Constants.NODE_USERS)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<User>()
                val myUid = _currentUserState.value?.uid
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null && user.uid != myUid) {
                        list.add(user)
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Contact Management Flows
    fun getContactsFlow(myUid: String): Flow<Set<String>> = callbackFlow {
        val db = database
        if (db == null) {
            val set = localContacts.getOrPut(myUid) { mutableSetOf() }
            trySend(set)
            awaitClose { }
            return@callbackFlow
        }

        val ref = db.getReference(Constants.NODE_CONTACTS).child(myUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactIds = mutableSetOf<String>()
                for (child in snapshot.children) {
                    if (child.getValue(Boolean::class.java) == true) {
                        contactIds.add(child.key ?: "")
                    }
                }
                trySend(contactIds)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptySet())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getContactRequestsFlow(myUid: String): Flow<List<ContactRequest>> = callbackFlow {
        val db = database
        if (db == null) {
            val list = localRequests.getOrPut(myUid) { mutableListOf() }
            trySend(list)
            awaitClose { }
            return@callbackFlow
        }

        val ref = db.getReference(Constants.NODE_CONTACT_REQUESTS).child(myUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = mutableListOf<ContactRequest>()
                for (child in snapshot.children) {
                    val req = child.getValue(ContactRequest::class.java)
                    if (req != null && req.status == "pending") {
                        requests.add(req)
                    }
                }
                trySend(requests)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun sendContactRequest(myUid: String, me: User, targetUid: String) {
        val db = database
        val request = ContactRequest(
            id = "req_${System.currentTimeMillis()}",
            senderId = myUid,
            senderName = me.displayName,
            senderEmail = me.email,
            senderPhoto = me.photoUrl,
            timestamp = System.currentTimeMillis(),
            status = "pending"
        )

        // Mark local side added
        if (db != null) {
            db.getReference(Constants.NODE_CONTACTS).child(myUid).child(targetUid).setValue(true)
            db.getReference(Constants.NODE_CONTACT_REQUESTS).child(targetUid).child(myUid).setValue(request)
        } else {
            localContacts.getOrPut(myUid) { mutableSetOf() }.add(targetUid)
            localRequests.getOrPut(targetUid) { mutableListOf() }.add(request)
        }
    }

    fun acceptContactRequest(myUid: String, me: User, targetUid: String, targetUser: User?) {
        val db = database
        if (db != null) {
            // Both users added to each other's contacts node -> MUTUAL CONTACT == true
            db.getReference(Constants.NODE_CONTACTS).child(myUid).child(targetUid).setValue(true)
            db.getReference(Constants.NODE_CONTACTS).child(targetUid).child(myUid).setValue(true)
            db.getReference(Constants.NODE_CONTACT_REQUESTS).child(myUid).child(targetUid).removeValue()
        } else {
            localContacts.getOrPut(myUid) { mutableSetOf() }.add(targetUid)
            localContacts.getOrPut(targetUid) { mutableSetOf() }.add(myUid)
            localRequests[myUid]?.removeAll { it.senderId == targetUid }
        }
    }

    fun removeContact(myUid: String, targetUid: String) {
        val db = database
        if (db != null) {
            // Revoke mutual contact status from both users
            db.getReference(Constants.NODE_CONTACTS).child(myUid).child(targetUid).removeValue()
            db.getReference(Constants.NODE_CONTACTS).child(targetUid).child(myUid).removeValue()
        } else {
            localContacts[myUid]?.remove(targetUid)
            localContacts[targetUid]?.remove(myUid)
        }
    }

    fun deleteChatHistory(currentUserId: String, otherUserId: String) {
        val chatId = if (currentUserId < otherUserId) "${currentUserId}_${otherUserId}" else "${otherUserId}_${currentUserId}"
        val db = database
        if (db != null) {
            db.getReference(Constants.NODE_CHATS).child(chatId).removeValue()
        }
    }

    fun signOut() {
        setPresence(false)
        auth?.signOut()
        _currentUserState.value = null
    }
}
