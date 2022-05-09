package com.example.studentpal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase
import kotlinx.coroutines.launch

class FriendsViewModel: ViewModel() {
    private val _friendsList = MutableLiveData<List<User>>()
    var friendsList: LiveData<List<User>> = _friendsList
    init {
        viewModelScope.launch {
            _friendsList.value = FriendshipsDatabase.getFriendsList()
        }
    }
}