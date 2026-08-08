package com.example.data

import android.util.Log
import com.example.model.CallSession
import com.example.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class CallRepository {

    private val database: FirebaseDatabase? = runCatching { FirebaseDatabase.getInstance() }.getOrNull()

    // Active local call state fallback
    private val activeCallState = MutableStateFlow<CallSession?>(null)

    fun startCall(
        callerId: String,
        callerName: String,
        callerPhoto: String,
        receiverId: String,
        isVideo: Boolean
    ): CallSession {
        val callId = "call_${System.currentTimeMillis()}"
        val channelId = "channel_${callerId.take(4)}_${receiverId.take(4)}_${System.currentTimeMillis() % 10000}"

        val session = CallSession(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            callerPhoto = callerPhoto,
            receiverId = receiverId,
            channelId = channelId,
            isVideoCall = isVideo,
            status = CallSession.STATUS_RINGING,
            timestamp = System.currentTimeMillis()
        )

        activeCallState.value = session

        val db = database
        if (db != null) {
            runCatching {
                val ref = db.getReference(Constants.NODE_CALLS).child(callId)
                ref.setValue(session)
            }.onFailure {
                Log.e("CallRepository", "Failed to register call in Firebase: ${it.message}")
            }
        }

        return session
    }

    fun updateCallStatus(callId: String, newStatus: String) {
        val current = activeCallState.value
        if (current?.callId == callId) {
            activeCallState.value = current.copy(status = newStatus)
        }

        val db = database
        if (db != null) {
            runCatching {
                db.getReference(Constants.NODE_CALLS)
                    .child(callId)
                    .child("status")
                    .setValue(newStatus)
            }
        }
    }

    fun listenForIncomingCalls(currentUserId: String): Flow<CallSession?> = callbackFlow {
        val db = database
        if (db == null) {
            val listener = activeCallState.value
            if (listener?.receiverId == currentUserId && listener.status == CallSession.STATUS_RINGING) {
                trySend(listener)
            } else {
                trySend(null)
            }
            awaitClose { }
            return@callbackFlow
        }

        val ref = db.getReference(Constants.NODE_CALLS)
        val query = ref.orderByChild("receiverId").equalTo(currentUserId)

        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var activeIncoming: CallSession? = null
                for (child in snapshot.children) {
                    val session = child.getValue(CallSession::class.java)
                    if (session != null && session.status == CallSession.STATUS_RINGING) {
                        activeIncoming = session
                        break
                    }
                }
                trySend(activeIncoming)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }

        query.addValueEventListener(valueListener)
        awaitClose { query.removeEventListener(valueListener) }
    }

    fun listenToCallSession(callId: String): Flow<CallSession?> = callbackFlow {
        val db = database
        if (db == null) {
            trySend(activeCallState.value)
            awaitClose { }
            return@callbackFlow
        }

        val ref = db.getReference(Constants.NODE_CALLS).child(callId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(CallSession::class.java)
                trySend(session)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
