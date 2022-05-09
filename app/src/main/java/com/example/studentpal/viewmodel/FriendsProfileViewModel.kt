package com.example.studentpal.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.common.Constants
import com.example.studentpal.common.Constants.RECEIVER
import com.example.studentpal.common.Constants.SENDER
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase
import com.example.studentpal.model.remote.FriendshipsDatabase.deleteReceiverFriendRequest
import com.example.studentpal.model.remote.FriendshipsDatabase.deleteSenderFriendRequest
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.view.messages.ChatLogActivity
import kotlinx.coroutines.launch

class FriendsProfileViewModel : ViewModel() {
    // current user profile details
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User>
        get() = _currentUser

    // Friends Profile details
    val _friendDetails = MutableLiveData<User>()
    val friendDetails: LiveData<User>
        get() = _friendDetails

    // User account states
    enum class AccountStates {
        DEFAULT,
        FRIEND,
        SENT_REQUEST,
        RECEIVED_REQUEST,
        DECLINED_REQUEST
    }

    companion object {
        // Starting state
        var currentState = MutableLiveData(AccountStates.DEFAULT)

        // DB states & FIELDS
        const val STATUS = "status"
        const val DECLINE = "decline"
        const val PENDING = "pending"
        const val FRIEND = "friend"
    }

    // Initialise current user
    init {
        viewModelScope.launch {
            _currentUser.value = fetchCurrentUser(getCurrentUserId())!!
        }
    }

    fun performAction(activity: FriendProfile) {
        when (currentState.value) {
            /* Default state
             * Users are not friends
             * No friend request is pending
             */
            AccountStates.DEFAULT -> {
                val friendRequestData = HashMap<String, Any>()
                friendRequestData[STATUS] = PENDING
                friendRequestData[SENDER] = getCurrentUserId()
                friendRequestData[RECEIVER] = friendDetails.value?.id!!

                FriendshipsDatabase.storeFriendRequest(
                    activity,
                    friendRequestData,
                    friendDetails,
                    currentUser
                )
            }
            /* Current user sent a friend request
             * The button has transformed to a Cancel request button
             */
            AccountStates.SENT_REQUEST -> {

                deleteSenderFriendRequest(activity, friendDetails.value)
            }
            // Current User declined a friend request
            AccountStates.DECLINED_REQUEST -> {
                deleteReceiverFriendRequest(activity, friendDetails.value)
            }
            // Current user is a recipient of a friend request
            AccountStates.RECEIVED_REQUEST -> {
                FriendshipsDatabase.createFriendship(
                    activity,
                    friendDetails.value!!,
                    currentUser.value!!
                )
            }

            AccountStates.FRIEND -> {
                val intent = Intent(activity, ChatLogActivity::class.java)
                intent.putExtra(Constants.USER_KEY, friendDetails.value)
                activity.startActivity(intent)
            }

            else -> {}
        }
    }

    fun checkUserExists() {
        FriendshipsDatabase.searchFriendship(friendDetails.value)

    }

    fun unFriend(activity: FriendProfile) {
        if (currentState.value == AccountStates.FRIEND) {
            //removes both friends from each others document
            if (friendDetails.value?.id != null) {
                FriendshipsDatabase.removeFriendship(activity, friendDetails.value, currentUser.value)

            }
            if (currentState.value == AccountStates.RECEIVED_REQUEST) {
                val hashmap = HashMap<String, Any>()
                hashmap[STATUS] = DECLINE

                FriendshipsDatabase.declineRequest(activity, hashmap, friendDetails.value)

            }
        }

    }
}
