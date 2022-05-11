package com.example.studentpal.view.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityViewFriendProfileBinding
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.fcm.PushNotification
import com.example.studentpal.model.fcm.RetrofitInstance
import com.example.studentpal.model.remote.PostsDatabase.getFriendsPosts
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.ImagePostsAdapter
import com.example.studentpal.view.messages.ChatLogActivity
import com.example.studentpal.viewmodel.FriendsProfileViewModel
import com.example.studentpal.viewmodel.FriendsProfileViewModel.Companion.currentState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//My code
class FriendProfile : BaseActivity() {

    companion object {
        private const val TAG = "FriendProfile"
    }

    var binding: ActivityViewFriendProfileBinding? = null

    // Buttons
    private var btnPerform: AppCompatButton? = null
    private var btnDeclineFriendRequest: AppCompatButton? = null

    private var postsList: ArrayList<Post>? = null
    private var postsAdapter: ImagePostsAdapter? = null

    // View Model
    private lateinit var viewModel: FriendsProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewFriendProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding!!.root)

        // Initialise View Model
        viewModel = ViewModelProvider(this)[FriendsProfileViewModel::class.java]

        // Observer for current user
        viewModel.currentUser.observe(this) {
            // TODO
        }
        /* Observer for profile current state
         * Calls updateButtons() function whenever current State changes
         */
        currentState.observe(this) {
            updateButtons(it)
        }

        // Retrieves friend details from intent
        if (intent.hasExtra(Constants.USER_KEY)) {
            viewModel._friendDetails.value = intent.getParcelableExtra(Constants.USER_KEY)!!
            getFriendsPosts(this, viewModel.friendDetails.value?.id)
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
        viewModel.checkUserExists()

        btnDeclineFriendRequest?.setOnClickListener {
            viewModel.unFriend(this)
        }
    }

    // My code: Responsible for modifying the buttons displayed based on the current state of the profile
    private fun updateButtons(it: FriendsProfileViewModel.AccountStates) {
        when (it) {
            FriendsProfileViewModel.AccountStates.SENT_REQUEST -> {
                btnDeclineFriendRequest?.visibility = View.GONE
                btnPerform?.text = "Cancel Friend Request"
                btnPerform?.background =
                    resources.getDrawable(R.drawable.btn_decline_request, theme)
            }
            FriendsProfileViewModel.AccountStates.DECLINED_REQUEST -> {
                //change state back to default
                currentState.value = FriendsProfileViewModel.AccountStates.DEFAULT
                btnPerform?.text = resources.getString(R.string.send_friend_request)
                btnPerform?.background = resources.getDrawable(R.drawable.btn_send_request, theme)
                btnDeclineFriendRequest?.visibility = View.GONE
            }
            FriendsProfileViewModel.AccountStates.RECEIVED_REQUEST -> {
                btnPerform?.text = "Accept Friend Request"
                btnPerform?.background = resources.getDrawable(R.drawable.btn_send_request, theme)
                btnDeclineFriendRequest?.text = "Decline Friend Request"
                btnDeclineFriendRequest?.background =
                    resources.getDrawable(R.drawable.btn_decline_request, theme)
                btnDeclineFriendRequest?.visibility = View.VISIBLE
            }
            FriendsProfileViewModel.AccountStates.FRIEND -> {
                btnPerform?.text = "Message"
                btnPerform?.background =
                    resources
                        .getDrawable(
                            R.drawable.shape_button_rounded,
                            theme
                        )
                btnDeclineFriendRequest?.text = "Unfriend"
                btnDeclineFriendRequest?.background =
                    resources.getDrawable(R.drawable.btn_decline_request, theme)
                btnDeclineFriendRequest?.visibility = View.VISIBLE

                btnPerform?.setOnClickListener {
                    lifecycleScope.launch {
                        fetchCurrentUser()
                    }
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

    // MY code: Loads the users data into the activity
    private fun loadFriendData() {
        //Users profile image loaded
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

    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            //network request: Post request
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(Companion.TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(Companion.TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(Companion.TAG, e.toString())
        }
    }


}