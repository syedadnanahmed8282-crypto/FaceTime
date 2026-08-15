package com.example.data

import android.util.Log
import com.example.model.ChatMessage
import com.example.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository {

    private val database: FirebaseDatabase? = runCatching {
        FirebaseDatabase.getInstance("https://callapp-119cd-default-rtdb.firebaseio.com")
    }.recoverCatching {
        FirebaseDatabase.getInstance()
    }.getOrNull()

    // Local in-memory storage fallback for offline/demo operation
    private val localChatsMap = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    fun getMessagesFlow(currentUserId: String, otherUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val chatId = getChatId(currentUserId, otherUserId)
        val db = database

        if (db == null) {
            val stateFlow = localChatsMap.getOrPut(chatId) {
                MutableStateFlow(emptyList())
            }
            trySend(stateFlow.value)
            awaitClose { }
            return@callbackFlow
        }

        try {
            val ref = db.getReference(Constants.NODE_CHATS).child(chatId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<ChatMessage>()
                    for (child in snapshot.children) {
                        val msg = child.getValue(ChatMessage::class.java)
                        if (msg != null) {
                            messages.add(msg)
                            // Auto-mark as delivered if received by current user
                            if (msg.receiverId == currentUserId && msg.status == ChatMessage.STATUS_SENT) {
                                child.ref.child("status").setValue(ChatMessage.STATUS_DELIVERED)
                            }
                        }
                    }
                    trySend(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatRepository", "Chat sync cancelled: ${error.message}")
                }
            }

            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        } catch (t: Throwable) {
            Log.e("ChatRepository", "getMessagesFlow note: ${t.message}")
            trySend(emptyList())
            awaitClose { }
        }
    }

    fun sendMessage(
        senderId: String,
        receiverId: String,
        text: String,
        replyToId: String? = null,
        replyToText: String? = null
    ) {
        val chatId = getChatId(senderId, receiverId)
        val msgId = "msg_${System.currentTimeMillis()}"
        val message = ChatMessage(
            id = msgId,
            senderId = senderId,
            receiverId = receiverId,
            messageText = text,
            timestamp = System.currentTimeMillis(),
            status = ChatMessage.STATUS_SENT,
            replyToId = replyToId,
            replyToText = replyToText
        )

        val db = database
        if (db != null) {
            try {
                val ref = db.getReference(Constants.NODE_CHATS)
                    .child(chatId)
                    .child(msgId)
                ref.setValue(message)
            } catch (t: Throwable) {
                Log.e("ChatRepository", "sendMessage note: ${t.message}")
            }
        } else {
            val stateFlow = localChatsMap.getOrPut(chatId) {
                MutableStateFlow(emptyList())
            }
            val currentList = stateFlow.value.toMutableList()
            currentList.add(message)
            stateFlow.value = currentList
        }
    }

    fun toggleReaction(senderId: String, receiverId: String, messageId: String, currentUserId: String, emoji: String) {
        val chatId = getChatId(senderId, receiverId)
        val db = database
        if (db != null) {
            try {
                val reactionRef = db.getReference(Constants.NODE_CHATS)
                    .child(chatId)
                    .child(messageId)
                    .child("reactions")
                    .child(currentUserId)

                reactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existing = snapshot.getValue(String::class.java)
                        if (existing == emoji) {
                            reactionRef.removeValue()
                        } else {
                            reactionRef.setValue(emoji)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            } catch (t: Throwable) {
                Log.e("ChatRepository", "toggleReaction note: ${t.message}")
            }
        } else {
            val stateFlow = localChatsMap[chatId] ?: return
            val list = stateFlow.value.toMutableList()
            val index = list.indexOfFirst { it.id == messageId }
            if (index != -1) {
                val msg = list[index]
                val reactions = msg.reactions.toMutableMap()
                if (reactions[currentUserId] == emoji) {
                    reactions.remove(currentUserId)
                } else {
                    reactions[currentUserId] = emoji
                }
                list[index] = msg.copy(reactions = reactions)
                stateFlow.value = list
            }
        }
    }
}
