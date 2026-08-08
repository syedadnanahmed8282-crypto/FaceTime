package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    val currentUserId: String,
    val otherUserId: String
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = repository.getMessagesFlow(currentUserId, otherUserId)
        .let { flow ->
            val state = MutableStateFlow<List<ChatMessage>>(emptyList())
            viewModelScope.launch {
                flow.collect { state.value = it }
            }
            state.asStateFlow()
        }

    private val _replyToMessage = MutableStateFlow<ChatMessage?>(null)
    val replyToMessage: StateFlow<ChatMessage?> = _replyToMessage.asStateFlow()

    fun setReplyTo(message: ChatMessage?) {
        _replyToMessage.value = message
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val reply = _replyToMessage.value
        repository.sendMessage(
            senderId = currentUserId,
            receiverId = otherUserId,
            text = text.trim(),
            replyToId = reply?.id,
            replyToText = reply?.messageText
        )
        _replyToMessage.value = null
    }

    fun toggleReaction(messageId: String, emoji: String) {
        repository.toggleReaction(
            senderId = currentUserId,
            receiverId = otherUserId,
            messageId = messageId,
            currentUserId = currentUserId,
            emoji = emoji
        )
    }
}
