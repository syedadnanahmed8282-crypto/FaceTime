package com.example.agora

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.example.agora.token.RtcTokenBuilder
import com.example.utils.Constants
import io.agora.rtc2.Constants as AgoraConstants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class AgoraManager(private val context: Context) {

    private var rtcEngine: RtcEngine? = null

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid

    private val _isJoined = MutableStateFlow(false)
    val isJoined: StateFlow<Boolean> = _isJoined

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isVideoDisabled = MutableStateFlow(false)
    val isVideoDisabled: StateFlow<Boolean> = _isVideoDisabled

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d("AgoraManager", "Joined channel: $channel with uid: $uid")
            _isJoined.value = true
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("AgoraManager", "Remote user joined: $uid")
            _remoteUid.value = uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("AgoraManager", "Remote user offline: $uid, reason: $reason")
            if (_remoteUid.value == uid) {
                _remoteUid.value = null
            }
        }

        override fun onError(err: Int) {
            Log.e("AgoraManager", "Agora RTC error code: $err")
        }
    }

    init {
        initEngine()
    }

    private fun initEngine() {
        val appId = Constants.AGORA_APP_ID
        if (appId.isBlank() || appId == "YOUR_AGORA_APP_ID_HERE") {
            Log.w("AgoraManager", "Agora App ID is not configured. App will run in simulated call UI mode.")
            return
        }

        runCatching {
            val config = RtcEngineConfig()
            config.mContext = context.applicationContext
            config.mAppId = appId
            config.mEventHandler = eventHandler
            rtcEngine = RtcEngine.create(config)
        }.onFailure {
            Log.e("AgoraManager", "Failed to initialize Agora RTC Engine safely: ${it.message}")
        }
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            Log.w("AgoraManager", "Camera permission not granted yet, skipping local preview")
            return
        }

        runCatching {
            rtcEngine?.let { engine ->
                engine.enableVideo()
                engine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                engine.startPreview()
            }
        }.onFailure {
            Log.e("AgoraManager", "Failed to setup local video preview: ${it.message}")
        }
    }

    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        runCatching {
            rtcEngine?.let { engine ->
                engine.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            }
        }.onFailure {
            Log.e("AgoraManager", "Failed to setup remote video: ${it.message}")
        }
    }

    fun joinCallChannel(channelId: String, userUid: Int, isVideo: Boolean, onComplete: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            val token = generateDynamicToken(channelId, userUid)
            
            CoroutineScope(Dispatchers.Main).launch {
                runCatching {
                    rtcEngine?.let { engine ->
                        if (isVideo) {
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                engine.enableVideo()
                                engine.startPreview()
                            }
                        } else {
                            engine.disableVideo()
                        }
                        engine.enableAudio()
                        engine.setEnableSpeakerphone(true)
                        
                        // Join channel with dynamic RTC token
                        val result = engine.joinChannel(token, channelId, "", userUid)
                        Log.d("AgoraManager", "joinChannel result code: $result with channel: $channelId, uid: $userUid")
                    } ?: run {
                        _isJoined.value = true
                    }
                }.onFailure {
                    Log.e("AgoraManager", "joinCallChannel error: ${it.message}")
                    _isJoined.value = true
                }
                onComplete()
            }
        }
    }

    private fun generateDynamicToken(channelId: String, uid: Int): String? {
        val appId = Constants.AGORA_APP_ID
        val appCertificate = Constants.AGORA_APP_CERTIFICATE

        if (appId.isBlank() || appId == "YOUR_AGORA_APP_ID_HERE") {
            return null
        }

        return try {
            val tokenBuilder = RtcTokenBuilder()
            val expirationTimeInSeconds = 24 * 3600 // 24 hours validity
            val currentTimestamp = (System.currentTimeMillis() / 1000).toInt()
            val privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds

            val token = tokenBuilder.buildTokenWithUid(
                appId = appId,
                appCertificate = appCertificate,
                channelName = channelId,
                uid = uid,
                role = RtcTokenBuilder.Role.Role_Publisher,
                privilegeTs = privilegeExpiredTs
            )
            Log.d("AgoraManager", "Generated dynamic RTC token locally for channel: $channelId")
            token
        } catch (e: Exception) {
            Log.e("AgoraManager", "Local token generation failed: ${e.message}")
            null
        }
    }

    fun toggleMute(): Boolean {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        runCatching {
            rtcEngine?.muteLocalAudioStream(newMuted)
        }
        return newMuted
    }

    fun toggleVideo(): Boolean {
        val newDisabled = !_isVideoDisabled.value
        _isVideoDisabled.value = newDisabled
        runCatching {
            rtcEngine?.muteLocalVideoStream(newDisabled)
            if (newDisabled) {
                rtcEngine?.stopPreview()
            } else {
                rtcEngine?.startPreview()
            }
        }
        return newDisabled
    }

    fun switchCamera() {
        runCatching {
            rtcEngine?.switchCamera()
        }
    }

    fun toggleSpeaker(): Boolean {
        val newSpeaker = !_isSpeakerOn.value
        _isSpeakerOn.value = newSpeaker
        runCatching {
            rtcEngine?.setEnableSpeakerphone(newSpeaker)
        }
        return newSpeaker
    }

    fun leaveCall() {
        runCatching {
            rtcEngine?.stopPreview()
            rtcEngine?.leaveChannel()
        }
        _isJoined.value = false
        _remoteUid.value = null
        _isMuted.value = false
        _isVideoDisabled.value = false
    }

    fun destroyEngine() {
        leaveCall()
        runCatching {
            RtcEngine.destroy()
        }
        rtcEngine = null
    }
}
