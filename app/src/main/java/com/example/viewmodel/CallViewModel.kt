package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.AgoraManager
import com.example.data.CallRepository
import com.example.model.CallSession
import com.example.utils.AudioRingHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel(
    val repository: CallRepository,
    val agoraManager: AgoraManager,
    val audioRingHelper: AudioRingHelper? = null
) : ViewModel() {

    private val _currentCall = MutableStateFlow<CallSession?>(null)
    val currentCall: StateFlow<CallSession?> = _currentCall.asStateFlow()

    val isJoined: StateFlow<Boolean> = agoraManager.isJoined
    val remoteUid: StateFlow<Int?> = agoraManager.remoteUid
    val isMuted: StateFlow<Boolean> = agoraManager.isMuted
    val isVideoDisabled: StateFlow<Boolean> = agoraManager.isVideoDisabled
    val isSpeakerOn: StateFlow<Boolean> = agoraManager.isSpeakerOn

    fun startCall(
        callerId: String,
        callerName: String,
        callerPhoto: String,
        receiverId: String,
        isVideo: Boolean
    ) {
        val session = repository.startCall(callerId, callerName, callerPhoto, receiverId, isVideo)
        _currentCall.value = session

        // Start dialtone ringing
        audioRingHelper?.startOutgoingDialtone()

        val userUid = (callerId.hashCode() and 0x7FFFFFFF) % 100000 + 1

        agoraManager.joinCallChannel(
            channelId = session.channelId,
            userUid = userUid,
            isVideo = isVideo
        )

        // Listen for call session status updates
        viewModelScope.launch {
            repository.listenToCallSession(session.callId).collect { updated ->
                _currentCall.value = updated
                if (updated?.status == CallSession.STATUS_ACCEPTED) {
                    audioRingHelper?.stopOutgoingDialtone()
                } else if (updated?.status == CallSession.STATUS_ENDED || updated?.status == CallSession.STATUS_DECLINED) {
                    audioRingHelper?.stopAll()
                    agoraManager.leaveCall()
                }
            }
        }
    }

    fun onIncomingCallReceived(session: CallSession) {
        audioRingHelper?.startIncomingRingtone()
    }

    fun acceptCall(session: CallSession, myUserId: String) {
        audioRingHelper?.stopAll()
        repository.updateCallStatus(session.callId, CallSession.STATUS_ACCEPTED)
        _currentCall.value = session.copy(status = CallSession.STATUS_ACCEPTED)

        val userUid = (myUserId.hashCode() and 0x7FFFFFFF) % 100000 + 1

        agoraManager.joinCallChannel(
            channelId = session.channelId,
            userUid = userUid,
            isVideo = session.isVideoCall
        )
    }

    fun declineCall(callId: String) {
        audioRingHelper?.stopAll()
        repository.updateCallStatus(callId, CallSession.STATUS_DECLINED)
        agoraManager.leaveCall()
        _currentCall.value = null
    }

    fun endCall() {
        audioRingHelper?.stopAll()
        val session = _currentCall.value
        if (session != null) {
            repository.updateCallStatus(session.callId, CallSession.STATUS_ENDED)
        }
        agoraManager.leaveCall()
        _currentCall.value = null
    }

    fun toggleMute() = agoraManager.toggleMute()
    fun toggleVideo() = agoraManager.toggleVideo()
    fun switchCamera() = agoraManager.switchCamera()
    fun toggleSpeaker() = agoraManager.toggleSpeaker()

    override fun onCleared() {
        super.onCleared()
        audioRingHelper?.stopAll()
        agoraManager.destroyEngine()
    }
}
