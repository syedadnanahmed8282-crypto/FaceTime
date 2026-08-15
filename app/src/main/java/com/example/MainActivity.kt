package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.agora.AgoraManager
import com.example.data.AuthRepository
import com.example.data.CallRepository
import com.example.data.ChatRepository
import com.example.model.CallSession
import com.example.model.User
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingCallScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.FaceTimeTheme
import com.example.utils.AudioRingHelper
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.CallViewModel
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var callRepository: CallRepository
    private lateinit var agoraManager: AgoraManager
    private lateinit var audioRingHelper: AudioRingHelper
    private lateinit var authViewModel: AuthViewModel
    private lateinit var callViewModel: CallViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            audioRingHelper = AudioRingHelper(applicationContext)
            authRepository = AuthRepository(applicationContext)
            chatRepository = ChatRepository()
            callRepository = CallRepository()
            agoraManager = AgoraManager(applicationContext)

            authViewModel = AuthViewModel(authRepository)
            callViewModel = CallViewModel(
                repository = callRepository,
                agoraManager = agoraManager,
                audioRingHelper = audioRingHelper,
                currentUserFlow = authViewModel.currentUser
            )
        } catch (t: Throwable) {
            Log.e("MainActivity", "Initialization error: ${t.message}", t)
        }

        requestCallPermissions()

        setContent {
            FaceTimeTheme {
                val navController = rememberNavController()
                val currentUser by authViewModel.currentUser.collectAsState()
                val usersList by authViewModel.allUsers.collectAsState()
                val myContacts by authViewModel.myContacts.collectAsState()
                val contactRequests by authViewModel.contactRequests.collectAsState()

                val incomingCallState by callViewModel.incomingCall.collectAsState()
                val currentCall by callViewModel.currentCall.collectAsState()
                val isMuted by callViewModel.isMuted.collectAsState()
                val isVideoDisabled by callViewModel.isVideoDisabled.collectAsState()
                val isSpeakerOn by callViewModel.isSpeakerOn.collectAsState()
                val remoteUid by callViewModel.remoteUid.collectAsState()

                // Trigger ringtone on incoming call
                LaunchedEffect(incomingCallState) {
                    val incoming = incomingCallState
                    if (incoming != null && currentCall == null) {
                        callViewModel.onIncomingCallReceived(incoming)
                    }
                }

                // Automatic navigation to CallScreen or IncomingCallScreen when call is active
                LaunchedEffect(currentCall, incomingCallState) {
                    val activeCall = currentCall
                    val incoming = incomingCallState
                    if (activeCall != null && (activeCall.status == CallSession.STATUS_RINGING || activeCall.status == CallSession.STATUS_ACCEPTED)) {
                        if (navController.currentDestination?.route != "call") {
                            navController.navigate("call") {
                                launchSingleTop = true
                            }
                        }
                    } else if (incoming != null && activeCall == null) {
                        if (navController.currentDestination?.route != "incoming_call") {
                            navController.navigate("incoming_call") {
                                launchSingleTop = true
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser != null) "home" else "login",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("login") {
                        val context = LocalContext.current
                        
                        LaunchedEffect(currentUser) {
                            if (currentUser != null) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        LoginScreen(
                            onGoogleSignInClick = {
                                authViewModel.signInWithGoogle(
                                    context = context,
                                    onSuccess = { _ ->
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, "Google Sign-In: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onCustomNameSignIn = { name ->
                                authRepository.signInWithCustomName(name) {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable("home") {
                        val me = currentUser
                        if (me == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        } else {
                            HomeScreen(
                                currentUser = me,
                                usersList = usersList,
                                myContacts = myContacts,
                                contactRequests = contactRequests,
                                incomingCallSession = incomingCallState,
                                onOpenChat = { targetUser ->
                                    navController.navigate("chat/${targetUser.uid}")
                                },
                                onOpenUserProfile = { targetUser ->
                                    navController.navigate("user_profile/${targetUser.uid}")
                                },
                                onStartVoiceCall = { targetUser ->
                                    callViewModel.startCall(
                                        callerId = me.uid,
                                        callerName = me.displayName,
                                        callerPhoto = me.photoUrl,
                                        receiverId = targetUser.uid,
                                        isVideo = false
                                    )
                                },
                                onStartVideoCall = { targetUser ->
                                    callViewModel.startCall(
                                        callerId = me.uid,
                                        callerName = me.displayName,
                                        callerPhoto = me.photoUrl,
                                        receiverId = targetUser.uid,
                                        isVideo = true
                                    )
                                },
                                onAcceptIncomingCall = { session ->
                                    callViewModel.acceptCall(session, me.uid)
                                },
                                onDeclineIncomingCall = { session ->
                                    callViewModel.declineCall(session.callId)
                                },
                                onSendContactRequest = { targetUid ->
                                    authViewModel.sendContactRequest(targetUid)
                                },
                                onAcceptContactRequest = { targetUid, targetUser ->
                                    authViewModel.acceptContactRequest(targetUid, targetUser)
                                },
                                onRemoveContact = { targetUid ->
                                    authViewModel.removeContact(targetUid)
                                },
                                onOpenProfile = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                    }

                    composable(
                        route = "chat/{otherUserId}",
                        arguments = listOf(navArgument("otherUserId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
                        val me = currentUser
                        val otherUser = usersList.find { it.uid == otherUserId }
                            ?: User(uid = otherUserId, displayName = "Contact")
                        val isMutual = myContacts.contains(otherUserId)

                        if (me != null) {
                            val chatViewModel = remember(me.uid, otherUserId) {
                                ChatViewModel(chatRepository, me.uid, otherUserId)
                            }
                            val messages by chatViewModel.messages.collectAsState()
                            val replyToMsg by chatViewModel.replyToMessage.collectAsState()

                            ChatScreen(
                                currentUserId = me.uid,
                                otherUser = otherUser,
                                isMutualContact = isMutual,
                                messages = messages,
                                replyToMessage = replyToMsg,
                                onSendMessage = { chatViewModel.sendMessage(it) },
                                onSetReplyTo = { chatViewModel.setReplyTo(it) },
                                onToggleReaction = { msgId, emoji ->
                                    chatViewModel.toggleReaction(msgId, emoji)
                                },
                                onStartVoiceCall = {
                                    callViewModel.startCall(
                                        callerId = me.uid,
                                        callerName = me.displayName,
                                        callerPhoto = me.photoUrl,
                                        receiverId = otherUser.uid,
                                        isVideo = false
                                    )
                                },
                                onStartVideoCall = {
                                    callViewModel.startCall(
                                        callerId = me.uid,
                                        callerName = me.displayName,
                                        callerPhoto = me.photoUrl,
                                        receiverId = otherUser.uid,
                                        isVideo = true
                                    )
                                },
                                onOpenProfile = {
                                    navController.navigate("user_profile/${otherUser.uid}")
                                },
                                onDeleteChatHistory = {
                                    authViewModel.deleteChatHistory(otherUser.uid)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(
                        route = "user_profile/{targetUserId}",
                        arguments = listOf(navArgument("targetUserId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
                        val me = currentUser
                        val targetUser = usersList.find { it.uid == targetUserId }
                            ?: User(uid = targetUserId, displayName = "User")
                        val isMutual = myContacts.contains(targetUserId)

                        if (me != null) {
                            UserProfileScreen(
                                user = targetUser,
                                isMutualContact = isMutual,
                                onOpenChat = {
                                    navController.navigate("chat/${targetUser.uid}")
                                },
                                onStartVoiceCall = {
                                    callViewModel.startCall(
                                        callerId = me.uid,
                                        callerName = me.displayName,
                                        callerPhoto = me.photoUrl,
                                        receiverId = targetUser.uid,
                                        isVideo = false
                                    )
                                },
                                onStartVideoCall = {
                                    callViewModel.startCall(
                                        callerId = me.uid,
                                        callerName = me.displayName,
                                        callerPhoto = me.photoUrl,
                                        receiverId = targetUser.uid,
                                        isVideo = true
                                    )
                                },
                                onAddContact = {
                                    authViewModel.sendContactRequest(targetUser.uid)
                                },
                                onRemoveContact = {
                                    authViewModel.removeContact(targetUser.uid)
                                },
                                onDeleteChatHistory = {
                                    authViewModel.deleteChatHistory(targetUser.uid)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("profile") {
                        val me = currentUser
                        if (me != null) {
                            ProfileScreen(
                                currentUser = me,
                                onSaveProfile = { name, about, photo ->
                                    authViewModel.updateProfile(name, about, photo)
                                    navController.popBackStack()
                                },
                                onSignOut = {
                                    authViewModel.signOut()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("call") {
                        val activeCall = currentCall ?: incomingCallState
                        val me = currentUser
                        if (activeCall != null && me != null) {
                            val isCaller = activeCall.callerId == me.uid
                            val otherName = if (isCaller) {
                                usersList.find { it.uid == activeCall.receiverId }?.displayName ?: "Recipient"
                            } else {
                                activeCall.callerName
                            }
                            val otherPhoto = if (isCaller) {
                                usersList.find { it.uid == activeCall.receiverId }?.photoUrl ?: ""
                            } else {
                                activeCall.callerPhoto
                            }

                            CallScreen(
                                callSession = activeCall,
                                otherUserName = otherName,
                                otherUserPhoto = otherPhoto,
                                agoraManager = agoraManager,
                                isMuted = isMuted,
                                isVideoDisabled = isVideoDisabled,
                                isSpeakerOn = isSpeakerOn,
                                remoteUid = remoteUid,
                                onToggleMute = { callViewModel.toggleMute() },
                                onToggleVideo = { callViewModel.toggleVideo() },
                                onSwitchCamera = { callViewModel.switchCamera() },
                                onToggleSpeaker = { callViewModel.toggleSpeaker() },
                                onEndCall = {
                                    callViewModel.endCall()
                                    navController.popBackStack("home", false)
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack("home", false)
                            }
                        }
                    }

                    composable("incoming_call") {
                        val incoming = incomingCallState
                        val me = currentUser
                        if (incoming != null && me != null) {
                            IncomingCallScreen(
                                callSession = incoming,
                                onAcceptCall = {
                                    callViewModel.acceptCall(incoming, me.uid)
                                    navController.navigate("call") {
                                        popUpTo("incoming_call") { inclusive = true }
                                    }
                                },
                                onDeclineCall = {
                                    callViewModel.declineCall(incoming.callId)
                                    navController.popBackStack("home", false)
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack("home", false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestCallPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            try {
                permissionLauncher.launch(missing.toTypedArray())
            } catch (t: Throwable) {
                Log.e("MainActivity", "Permission request note: ${t.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::authRepository.isInitialized) {
            authRepository.setPresence(true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::authRepository.isInitialized) {
            authRepository.setPresence(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audioRingHelper.isInitialized) {
            audioRingHelper.stopAll()
        }
        if (::agoraManager.isInitialized) {
            agoraManager.destroyEngine()
        }
    }
}
