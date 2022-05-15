package com.example.studentpal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase.getRequests
import kotlinx.coroutines.launch

/**
 * This class is responsible for executing [com.example.studentpal.view.friends.RequestsActivity] business logic
 *
 * Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * The entire code in this class belongs to the author.
 *
 */

class RequestsViewModel: ViewModel() {
    // List of users that sent a friend request
    private val _requestList = MutableLiveData<List<User>>()
    // Public getter of requests list
    val requestList: LiveData<List<User>>
        get() = _requestList

    init {
        viewModelScope.launch {
            // Suspend functions need to be executed in a coroutine.
            getRequests().also { _requestList.value = it }
        }
    }


}