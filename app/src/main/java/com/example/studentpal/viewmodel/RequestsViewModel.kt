package com.example.studentpal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase.getRequests
import kotlinx.coroutines.launch

class RequestsViewModel: ViewModel() {
    private val _requestList = MutableLiveData<List<User>>()
    val requestList: LiveData<List<User>>
        get() = _requestList

    init {
        viewModelScope.launch {
            _requestList.value = getRequests()
        }
    }


}