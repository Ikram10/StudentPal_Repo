package com.example.studentpal.view.friends

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityViewFriendProfileBinding
import com.example.studentpal.model.fcm.PushNotification
import com.example.studentpal.model.fcm.RetrofitInstance
import com.example.studentpal.model.remote.PostsDatabase.getFriendsPosts
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.messages.ChatLogActivity
import com.example.studentpal.viewmodel.FriendsProfileViewModel
import com.example.studentpal.viewmodel.FriendsProfileViewModel.Companion.currentState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This activity is responsible for displaying a friends profile.
 *
 * When a users selects a specific friend, they will be navigated to this activity
 *
 * The entire code in this activity belongs to the author.
 */
class FriendProfile : BaseActivity() {

    companion object {
        private const val TAG = "FriendProfile"
    }

    var binding: ActivityViewFriendProfileBinding? = null
    // Buttons
    private var btnPerform: AppCompatButton? = null
    private var btnDeclineFriendRequest: AppCompatButton? = null
    // View Model
    private lateinit var viewModel: FriendsProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewFriendProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding!!.root)

        // Initialise View Model
        viewModel = ViewModelProvider(this)[FriendsProfileViewModel::class.java]

        // Observer the changes made to the current state of the account
        currentState.observe(this) {
            // This function is called whenever the current state changes
            updateButtons(it)
        }

        // Retrieves friend details from intent
        if (intent.hasExtra(Constants.USER_KEY)) {
            viewModel.friendDetails.value = intent.getParcelableExtra(Constants.USER_KEY)!!
            getFriendsPosts(viewModel.friendDetails.value?.id)
        }

        setupActionBar()

        //Loads the selected users information into the UI
        loadFriendData()

        // Initialise buttons
        btnPerform = binding?.btnSendRequest
        btnDeclineFriendRequest = binding?.btnDeclineRequest

        btnPerform?.setOnClickListener {
            viewModel.performAction(this)
        }

        viewModel.checkFriendshipExists()

        btnDeclineFriendRequest?.setOnClickListener {
            viewModel.performDecline(this)
        }
    }

    /**
     * This method is responsible for modifying the button's text,
     * colour and functionality based on the current state of the account.
     *
     * The state defines the relationship between two users, for example if they are friends or not.
     *
     * @param state defines the current state of the account
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateButtons(state: FriendsProfileViewModel.AccountStates) {
        when (state) {
            FriendsProfileViewModel.AccountStates.SENT_REQUEST -> {
                btnDeclineFriendRequest?.visibility = View.GONE
                btnPerform?.setText(R.string.cancelFriendRequest)
                btnPerform?.background =
                    resources.getDrawable(R.drawable.btn_decline_request, theme)
            }
            FriendsProfileViewModel.AccountStates.DECLINED_REQUEST -> {
                //change current state back to default
                currentState.value = FriendsProfileViewModel.AccountStates.DEFAULT
                btnPerform?.text = resources.getString(R.string.send_friend_request)
                btnPerform?.background = resources.getDrawable(R.drawable.btn_send_request, theme)
                btnDeclineFriendRequest?.visibility = View.GONE
            }

            FriendsProfileViewModel.AccountStates.RECEIVED_REQUEST -> {
                btnPerform?.setText(R.string.acceptFriendRequest)
                btnPerform?.background = resources.getDrawable(R.drawable.btn_send_request, theme)
                btnDeclineFriendRequest?.setText(R.string.declineFriendRequest)
                btnDeclineFriendRequest?.background =
                    resources.getDrawable(R.drawable.btn_decline_request, theme)
                btnDeclineFriendRequest?.visibility = View.VISIBLE
            }
            FriendsProfileViewModel.AccountStates.FRIEND -> {
                btnPerform?.setText(R.string.message)
                btnPerform?.background =
                    resources
                        .getDrawable(
                            R.drawable.shape_button_rounded,
                            theme
                        )
                btnDeclineFriendRequest?.setText(R.string.unfriend)
                btnDeclineFriendRequest?.background =
                    resources.getDrawable(R.drawable.btn_decline_request, theme)
                btnDeclineFriendRequest?.visibility = View.VISIBLE

                btnPerform?.setOnClickListener {
                    // Sends the friend details to the Chat log activity
                    val intent =
                        Intent(this, ChatLogActivity::class.java)
                    intent.putExtra(
                        Constants.USER_KEY,
                        viewModel.friendDetails.value
                    )
                    startActivity(intent)
                }
            }
            FriendsProfileViewModel.AccountStates.DEFAULT -> {
                btnPerform?.text = resources.getString(R.string.send_friend_request)
                btnPerform?.background = resources.getDrawable(R.drawable.btn_send_request, theme)
                btnDeclineFriendRequest?.visibility = View.GONE
            }
        }
    }

    /**
     * A method to set the friend's details in the UI.
     *
     * This method is called whenever the users data is changed to
     * update the UI and display the changes
     *
     * @see [FriendsProfileViewModel.friendDetails]
     */
    private fun loadFriendData() {
        //Users profile image loaded using Glide
        binding?.civFriendImage?.let {
            Glide
                .with(this)
                .load(viewModel.friendDetails.value?.image)
                .centerCrop()
                .placeholder(R.drawable.ic_nav_user)
                .into(it)
        }

        // users cover image loaded
        binding?.coverImageFriend?.let {
            Glide
                .with(this)
                .load(viewModel.friendDetails.value?.coverImage)
                .centerCrop()
                .into(it)
        }
        binding?.tvFriendName?.text = viewModel.friendDetails.value?.name
        binding?.cvFriendStatus?.text = viewModel.friendDetails.value?.status
        binding?.dateNum?.text = viewModel.friendDetails.value?.dateJoined
        binding?.friendsNum?.text = viewModel.friendDetails.value?.numFriends.toString()
        binding?.friendUsername?.text  = viewModel.friendDetails.value?.username.toString()

        //My code: Sets the text colour of users status depending on the Status
        when (viewModel.friendDetails.value?.status) {
            "Available" -> {
                // Change colour green when status message is Available
                binding!!.cvFriendStatus.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.available
                    )
                )
            }
            // Change colour red when status message is Available
            "Unavailable" -> {
                binding!!.cvFriendStatus.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.unavailable
                    )
                )
            }
        }

    }

    /**
     * This method is responsible for sending the notification to the Firebase server
     * This method starts a coroutine to execute the code in the IO thread
     * @see [com.example.studentpal.model.fcm.RetrofitInstance]
     */
    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            //network request: Posts notification to the firebase server
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarFriendProfile)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = viewModel.friendDetails.value?.name.toString()
        }
        binding?.toolbarFriendProfile?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}