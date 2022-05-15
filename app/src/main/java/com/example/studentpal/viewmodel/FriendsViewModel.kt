package com.example.studentpal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase
import kotlinx.coroutines.launch

/**
 * This class is responsible for executing FriendsActivity's business logic
 *
 * Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * The entire code in this class belongs to the author.
 *
 */
class FriendsViewModel: ViewModel() {
    private val _friendsList = MutableLiveData<List<User>>()
    var friendsList: LiveData<List<User>> = _friendsList
    // Initialises friends list whenever the view model is created
    init {
        viewModelScope.launch {
            _friendsList.value = FriendshipsDatabase.getFriendsList()
        }
    }
}