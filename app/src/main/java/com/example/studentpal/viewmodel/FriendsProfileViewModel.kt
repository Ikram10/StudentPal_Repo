package com.example.studentpal.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.FriendRequest
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase
import com.example.studentpal.model.remote.FriendshipsDatabase.deleteReceiverFriendRequest
import com.example.studentpal.model.remote.FriendshipsDatabase.deleteSenderFriendRequest
import com.example.studentpal.model.remote.FriendshipsDatabase.storeFriendRequest
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.view.messages.ChatLogActivity
import kotlinx.coroutines.launch

/**
 * This class is responsible for executing [FriendProfile] business logic
 *
 *  Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * The entire code in this class belongs to the author.
 *
 */

class FriendsProfileViewModel : ViewModel() {
    // current user profile details
    private val _currentUser = MutableLiveData<User>()

    // public getter
    val currentUser: LiveData<User>
        get() = _currentUser

    // Friends Profile details
    val friendDetails = MutableLiveData<User>()

    /**
     * Enum class created to set [currentState] to a set of predefined constants
     *
     * This was implemented because it provided an efficient way to assign multiple constants to the variable
     * [currentState]
     */
    enum class AccountStates {
        DEFAULT,
        FRIEND,
        SENT_REQUEST,
        RECEIVED_REQUEST,
        DECLINED_REQUEST
    }

    companion object {
        // initial state is set to default
        var currentState = MutableLiveData(AccountStates.DEFAULT)

        // DB states & FIELDS
        const val STATUS = "status"
        const val DECLINE = "decline"
        const val PENDING = "pending"
        const val FRIEND = "friend"
    }

    /**
     * Initialise current user whenever this class is created
     */
    init {
        viewModelScope.launch {
            _currentUser.value = fetchCurrentUser()!!
        }
    }

    /**
     * Method performs a particular functionality based on the current state of the account
     *
     * @see currentState
     */
    fun performAction(activity: FriendProfile) {
        when (currentState.value) {
            /* Default state
             * Users are not friends
             * No friend request is pending
             */
            AccountStates.DEFAULT -> {
                val friendRequest = FriendRequest(PENDING, getCurrentUserId(),
                    friendDetails.value?.id!!)

                // stores the friend request in Firestore
                storeFriendRequest(activity, friendRequest, friendDetails,
                    currentUser)
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
            /* Both users are now friends
             * Button transforms to a message button
             */
            AccountStates.FRIEND -> {
                val intent = Intent(activity, ChatLogActivity::class.java)
                intent.putExtra(Constants.USER_KEY, friendDetails.value)
                activity.startActivity(intent)
            }

            else -> {
            }
        }
    }

    /**
     * Method checks the database to confirm that two users are in fact friends and updates
     * the current state of the account appropriately
     */
    fun checkFriendshipExists() {
        FriendshipsDatabase.searchFriendship(friendDetails.value)
    }

    /**
     * Method handles the functionality when the decline button is clicked
     *
     * Decline button changes its functionality based on the current state of the account.
     * For instance, if [currentState] is [AccountStates.FRIEND] the button will be an unfriend button.
     *
     * @see AccountStates
     */
    fun performDecline(activity: FriendProfile) {
        if (currentState.value == AccountStates.FRIEND) {
            if (friendDetails.value?.id != null) {
                // Removes Friendship document between the two users from database
                FriendshipsDatabase.removeFriendship(
                    activity,
                    friendDetails.value,
                    currentUser.value
                )
            }
            /* Current state: User has received the friend request
             * Button has transformed to a "Decline Friend Request" button
             */
            if (currentState.value == AccountStates.RECEIVED_REQUEST) {
                val hashmap = HashMap<String, Any>()
                hashmap[STATUS] = DECLINE

                FriendshipsDatabase.declineRequest(activity, hashmap, friendDetails.value)

            }
        }

    }
}
