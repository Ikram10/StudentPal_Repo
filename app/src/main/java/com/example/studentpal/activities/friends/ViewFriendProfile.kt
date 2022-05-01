package com.example.studentpal.activities.friends

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.activities.messages.ChatLogActivity
import com.example.studentpal.activities.messages.ChatLogActivity.Companion.TAG
import com.example.studentpal.adapter.ImagePostsAdapter
import com.example.studentpal.databinding.ActivityViewFriendProfileBinding
import com.example.studentpal.fcm.RetrofitInstance
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.ImagePost
import com.example.studentpal.models.NotificationData
import com.example.studentpal.models.PushNotification
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

//My code
class ViewFriendProfile : BaseActivity() {
    //DB references
    private var requestRef: CollectionReference? = null
    private var friendRef: CollectionReference? = null
    private var mUser: FirebaseUser? = null
    private var friendDetails: User? = null
    var binding: ActivityViewFriendProfileBinding? = null
    var btnPerform: AppCompatButton? = null
    var btnDeclineFriendRequest: AppCompatButton? = null

    private var postsList: ArrayList<ImagePost>? = null
    private var postsAdapter: ImagePostsAdapter? = null

    // Starting state
    var currentState = DEFAULT

    companion object {
        // User account states
        const val DEFAULT = "default"
        const val FRIEND = "friend"
        const val SENT_REQUEST = "sent_request"
        const val RECEIVED_REQUEST = "received_request"
        const val DECLINED_REQUEST = "declined_request"
        // DB states & FIELDS
        const val STATUS = "status"
        const val DECLINE = "decline"
        const val PENDING = "pending"
        const val SENDER = "sender"
        const val RECEIVER = "receiver"

        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewFriendProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding!!.root)

        // Retrieves users from intent
        if (intent.hasExtra(Constants.USER_KEY)) {
            friendDetails = intent.getParcelableExtra<User>(Constants.USER_KEY)
            FirestoreClass().getFriendsPosts(this, friendDetails?.id)
        }

        setupActionBar()

        mUser = FirebaseAuth.getInstance().currentUser

        requestRef = FirebaseFirestore.getInstance().collection(Constants.FRIEND_REQUEST)
        friendRef = FirebaseFirestore.getInstance().collection(Constants.FRIENDSHIPS)

        btnPerform = binding?.btnSendRequest?.findViewById(R.id.btn_send_request)
        btnDeclineFriendRequest = binding?.btnDeclineRequest?.findViewById(R.id.btn_decline_request)

        //Loads the selected users information into the UI
        loadFriendData()
        FirestoreClass().fetchCurrentUser(this)

        btnPerform?.setOnClickListener {

            performAction(friendDetails?.id)
        }
        checkUserExists(friendDetails?.id)

        btnDeclineFriendRequest?.setOnClickListener {
            unFriend(friendDetails?.id)
        }


    }

    //MY code
    private fun loadFriendData() {
        //Users profile image
        binding?.civFriendImage?.let {
            Glide
                .with(this)
                .load(friendDetails?.image)
                .centerCrop()
                .placeholder(R.drawable.ic_nav_user)
                .into(it)
        }

        // users cover images
        binding?.coverImageFriend?.let {
            Glide
                .with(this)
                .load(friendDetails?.coverImage)
                .centerCrop()
                .into(it)
        }
        binding?.tvFriendName?.text = friendDetails?.name.toString()
        binding?.cvFriendStatus?.text = friendDetails?.status
        binding?.dateNum?.text = friendDetails?.dateJoined
        binding?.friendsNum?.text = friendDetails?.numFriends.toString()

    }


    private fun unFriend(friendUid: String?) {
        if (currentState == FRIEND) {
            //removes both friends from each others document
            if (friendUid != null) {
                friendRef?.whereEqualTo(SENDER, getCurrentUserID())
                    ?.whereEqualTo(RECEIVER, friendUid)
                    ?.get()
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {

                            for (doc in it.result.documents) {
                                doc.reference.delete().addOnCompleteListener {
                                    if (it.isSuccessful) {

                                        FirestoreClass().decrementFriendsCount(currentUser, friendDetails)
                                        Toast.makeText(
                                            this,
                                            "You unfriended ${friendDetails?.name}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        currentState = DEFAULT
                                        btnPerform?.text = resources.getString(R.string.send_friend_request)
                                        btnDeclineFriendRequest?.visibility = View.GONE
                                    }
                                }
                            }

                        }
                    }
            }
            if (friendUid != null) {
                friendRef?.whereEqualTo(SENDER, friendUid)
                    ?.whereEqualTo(RECEIVER, getCurrentUserID())
                    ?.get()
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {

                            for (doc in it.result.documents) {
                                doc.reference.delete().addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        FirestoreClass().decrementFriendsCount(currentUser,friendDetails)
                                        Toast.makeText(
                                            this,
                                            "You unfriended ${friendDetails?.name}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        currentState = DEFAULT
                                        btnPerform?.text = resources.getString(R.string.send_friend_request)
                                        btnDeclineFriendRequest?.visibility = View.GONE
                                    }
                                }
                            }

                        }
                    }

            }
        }
        if (currentState == RECEIVED_REQUEST) {
            val hashmap = HashMap<String, Any>()
            if (friendUid != null) {
                hashmap[STATUS] = DECLINE

                requestRef?.whereEqualTo(SENDER, friendUid)
                    ?.whereEqualTo(RECEIVER, getCurrentUserID())
                    ?.get()
                    ?.addOnCompleteListener {
                        for (doc in it.result.documents) {
                            doc.reference.update(hashmap).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "You have declined friend request from ${friendDetails?.name}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentState = DECLINED_REQUEST
                                    btnPerform?.visibility = View.GONE
                                    btnDeclineFriendRequest?.visibility = (View.GONE)
                                }
                            }
                        }


                    }
            }
        }

    }

    private fun checkUserExists(userID: String?) {
        friendRef
            ?.whereEqualTo(SENDER, getCurrentUserID())
            ?.whereEqualTo(RECEIVER, userID)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                currentState = FRIEND

                                btnPerform?.text = resources.getString(R.string.message)
                                btnDeclineFriendRequest?.text = "Unfriend"
                                btnDeclineFriendRequest?.visibility = View.VISIBLE


                            }
                            DocumentChange.Type.MODIFIED -> {

                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }
                }


            }

        friendRef
            ?.whereEqualTo(SENDER, userID)
            ?.whereEqualTo(RECEIVER, getCurrentUserID())
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                currentState = FRIEND
                                btnPerform?.text = resources.getString(R.string.message)
                                btnDeclineFriendRequest?.text = "Unfriend"
                                btnDeclineFriendRequest?.visibility = View.VISIBLE


                            }
                            DocumentChange.Type.MODIFIED -> {

                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }
                }


            }



        requestRef
            ?.whereEqualTo(SENDER, getCurrentUserID())
            ?.whereEqualTo(RECEIVER, userID)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                if (dc.document[STATUS] == PENDING) {

                                    currentState = SENT_REQUEST
                                    btnPerform?.text = "Cancel Friend Request"
                                    btnDeclineFriendRequest?.visibility = View.GONE
                                    btnPerform?.background =
                                        resources.getDrawable(R.drawable.btn_decline_request, theme)
                                }
                                if (dc.document[STATUS] == DECLINE) {
                                    currentState = DECLINED_REQUEST
                                    btnPerform?.text = resources.getString(R.string.send_friend_request)
                                    btnDeclineFriendRequest?.visibility = View.GONE
                                    btnPerform?.background =
                                        resources.getDrawable(R.drawable.btn_decline_request, theme)
                                }

                            }
                            DocumentChange.Type.MODIFIED -> {

                            }

                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }
                }
            }


        requestRef
            ?.whereEqualTo(SENDER, userID)
            ?.whereEqualTo(RECEIVER, getCurrentUserID())
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                if (dc.document[STATUS] == PENDING) {
                                    currentState = RECEIVED_REQUEST
                                    btnPerform?.text = "Accept Friend Request"
                                    btnDeclineFriendRequest?.text = "Decline Friend Request"
                                    btnDeclineFriendRequest?.visibility = View.VISIBLE
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }
                }
            }

        if (currentState == DEFAULT) {
            btnPerform?.text = resources.getString(R.string.send_friend_request)
            btnDeclineFriendRequest?.visibility = View.GONE
        }
    }

    //My code: This is responsible for handling all the account states between users when sending friend requests

    private fun performAction(friendUserID: String?) {
        when (currentState) {
            /* Default state
             * Users are not friends
             * No friend request is pending
             */
            DEFAULT -> {
                val friendRequestData = HashMap<String, Any>()
                friendRequestData[STATUS] = PENDING
                friendRequestData[SENDER] = getCurrentUserID()
                friendRequestData[RECEIVER] = friendUserID!!

                requestRef?.document()
                    ?.set(friendRequestData)
                    ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "You have sent a friend Request to ${friendDetails?.name}",
                            Toast.LENGTH_LONG
                        ).show()
                        val notification = NotificationData("Friend Request",
                            "${currentUser?.name} sent a friend request")

                        sendNotification(PushNotification(notification, friendDetails!!.fcmToken))

                        btnDeclineFriendRequest?.visibility = View.GONE
                        currentState = SENT_REQUEST
                        btnPerform?.text = "Cancel Friend Request"
                        btnPerform?.background =
                            resources.getDrawable(R.drawable.btn_decline_request, theme)

                    } else {
                        Log.e(javaClass.simpleName, it.exception.toString())

                        Toast.makeText(
                            this,
                            "Error sending friend request to ${friendDetails?.name}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            /* Current user sent a friend request
             * The button has transformed to a Cancel request button
             */
            SENT_REQUEST -> {
                requestRef?.whereEqualTo(SENDER, getCurrentUserID())
                    ?.whereEqualTo(RECEIVER, friendUserID)?.get()?.addOnCompleteListener {

                        if (it.isSuccessful) {
                            //change state back to default
                            currentState = DEFAULT
                            btnPerform?.text = resources.getString(R.string.send_friend_request)
                            btnPerform?.background =
                                resources.getDrawable(R.drawable.btn_send_request, theme)

                            btnDeclineFriendRequest?.visibility = View.GONE
                            //delete friend request document
                            for (doc in it.result.documents) {
                                doc.reference.delete()
                                Toast.makeText(
                                    this,
                                    "You have cancelled friend request",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        } else {
                            Log.e(javaClass.simpleName, it.exception.toString())
                            Toast.makeText(
                                this,
                                "Error cancelling friend request",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


            }

            // Current User declined a friend request
            DECLINED_REQUEST -> {
                requestRef?.whereEqualTo(SENDER, friendUserID)
                    ?.whereEqualTo(RECEIVER, getCurrentUserID())?.get()?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "You have declined friend request from ${friendDetails?.name}",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            //change state back to default
                            currentState = DEFAULT
                            btnPerform?.text = resources.getString(R.string.send_friend_request)
                            btnDeclineFriendRequest?.visibility = View.GONE

                            for (doc in it.result.documents) {
                                doc.reference.delete()
                            }
                        } else {
                            Log.e(javaClass.simpleName, it.exception.toString())
                            Toast.makeText(
                                this,
                                "Error sending friend request to ${friendDetails?.name}",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }


            }

            // Current user is a recipient of a friend request
            RECEIVED_REQUEST -> {
                val collectionQuery = requestRef?.whereEqualTo(SENDER, friendUserID)
                    ?.whereEqualTo(RECEIVER, getCurrentUserID())?.get()

                collectionQuery?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result.documents) {
                            doc.reference.delete().addOnSuccessListener {
                                val hashMap = HashMap<String, Any>()
                                hashMap[STATUS] = FRIEND
                                hashMap[SENDER] = friendUserID!!
                                hashMap[RECEIVER] = getCurrentUserID()

                                friendRef?.document()?.set(hashMap, SetOptions.merge())
                                    ?.addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            //Increments number of friends by 1
                                            FirestoreClass().incrementFriendsCount(currentUser, friendDetails)
                                            Toast.makeText(
                                                this,
                                                "You added a Friend",
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                            currentState = FRIEND
                                            btnPerform?.text = "Message"
                                            btnPerform?.background =
                                                resources.getDrawable(R.drawable.shape_button_rounded, theme)
                                            btnDeclineFriendRequest?.text = "Unfriend"
                                            btnDeclineFriendRequest?.visibility = View.VISIBLE


                                            btnPerform?.setOnClickListener {
                                                FirestoreClass().fetchCurrentUser(this)
                                                // Sends the friend details to the Chat log activity
                                                val intent =
                                                    Intent(this, ChatLogActivity::class.java)
                                                intent.putExtra(
                                                    Constants.USER_KEY,
                                                    friendDetails
                                                )
                                                startActivity(intent)
                                            }
                                        }
                                    }
                            }
                        }

                    }


                }
            }

            FRIEND -> {
                val intent = Intent(this, ChatLogActivity::class.java)
                intent.putExtra(Constants.USER_KEY, friendDetails)
                startActivity(intent)
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarFriendProfile)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = friendDetails?.name.toString()
        }
        binding?.toolbarFriendProfile?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {

            //network request: Post request
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {

            Log.e(TAG, e.toString())

        }
    }



}