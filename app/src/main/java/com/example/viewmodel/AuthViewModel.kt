package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.model.ContactRequest
import com.example.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel(val repository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUserState

    val allUsers: StateFlow<List<User>> = repository.getUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myContacts: StateFlow<Set<String>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getContactsFlow(user.uid) else flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val contactRequests: StateFlow<List<ContactRequest>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getContactRequestsFlow(user.uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun signInWithGoogle(
        context: android.content.Context,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.signInWithGoogle(context, onSuccess, onError)
        }
    }

    fun updateProfile(displayName: String, aboutStatus: String, photoUrl: String) {
        repository.updateProfile(displayName, aboutStatus, photoUrl)
    }

    fun sendContactRequest(targetUid: String) {
        val me = currentUser.value ?: return
        repository.sendContactRequest(me.uid, me, targetUid)
    }

    fun acceptContactRequest(targetUid: String, targetUser: User?) {
        val me = currentUser.value ?: return
        repository.acceptContactRequest(me.uid, me, targetUid, targetUser)
    }

    fun removeContact(targetUid: String) {
        val me = currentUser.value ?: return
        repository.removeContact(me.uid, targetUid)
    }

    fun deleteChatHistory(otherUserId: String) {
        val me = currentUser.value ?: return
        repository.deleteChatHistory(me.uid, otherUserId)
    }

    fun signOut() {
        repository.signOut()
    }
}
