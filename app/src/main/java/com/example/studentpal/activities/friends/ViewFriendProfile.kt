package com.example.studentpal.activities.friends

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityViewFriendProfileBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.activities.messages.ChatLogActivity
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

//My code
class ViewFriendProfile : AppCompatActivity() {
    //DB references
    private var requestRef: DatabaseReference? = null
    private var friendRef: DatabaseReference? = null
    private var mUser: FirebaseUser? = null
    var friendDetails: User? = null
    var binding: ActivityViewFriendProfileBinding? = null
    var btnSendFriendRequest: AppCompatButton? = null
    var btnDeclineFriendRequest: AppCompatButton? = null

    // Starting state
    var currentState = DEFAULT

    companion object {
        // User account states
        const val DEFAULT = "default"
        const val FRIEND = "friend"
        const val SENT_REQUEST = "sent_request"
        const val RECEIVED_REQUEST = "received_request"
        const val DECLINED_REQUEST = "declined_request"

        // DB states
        const val STATUS = "status"
        const val DECLINE = "decline"
        const val PENDING = "pending"

        var currentUser: User? = null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewFriendProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding!!.root)

        if (intent.hasExtra(Constants.USER_KEY)) {
            friendDetails = intent.getParcelableExtra<User>(Constants.USER_KEY)
        }
        setupActionBar()

        mUser = FirebaseAuth.getInstance().currentUser

        requestRef = FirebaseDatabase.getInstance().reference.child("Requests")
        friendRef = FirebaseDatabase.getInstance().reference.child("Friends")

        btnSendFriendRequest = binding?.btnSendRequest?.findViewById(R.id.btn_send_request)
        btnDeclineFriendRequest = binding?.btnDeclineRequest?.findViewById(R.id.btn_decline_request)

        //Loads the selected users information into the UI
        loadFriendData()


        btnSendFriendRequest?.setOnClickListener {
            performAction(friendDetails?.id)

        }
        checkUserExistance(friendDetails?.id)

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
        binding?.friendsNum?.text = "0"

    }


    private fun unFriend(friendUid: String?) {
        if (currentState == FRIEND) {

            //removes both friends from each others document
            if (friendUid != null) {
                friendRef?.child(mUser!!.uid)?.child(friendUid)?.removeValue()
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            friendRef!!.child(friendUid).child(mUser!!.uid).removeValue()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "You unfriended ${friendDetails?.name}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        currentState = DEFAULT
                                        btnSendFriendRequest?.text = "Send Friend Request"
                                        btnDeclineFriendRequest?.visibility = View.GONE
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
                requestRef?.child(friendUid)?.child(mUser!!.uid)?.updateChildren(hashmap)
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "You have declined friend request from ${friendDetails?.name}",
                                Toast.LENGTH_LONG
                            ).show()
                            currentState = DECLINED_REQUEST
                            btnSendFriendRequest?.visibility = View.GONE
                            btnDeclineFriendRequest?.visibility = (View.GONE)
                        }

                    }
            }
        }

    }

    private fun checkUserExistance(userID: String?) {
        friendRef?.child(mUser!!.uid)?.child(userID!!)
            ?.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    // if snapshot exists, the two users are already friends
                    if (snapshot.exists()) {
                        currentState = FRIEND
                        btnSendFriendRequest?.text = "Message"
                        btnDeclineFriendRequest?.text = "Unfriend"
                        btnDeclineFriendRequest?.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        if (userID != null) {
            friendRef?.child(userID)?.child(mUser!!.uid)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            currentState = FRIEND
                            btnSendFriendRequest?.text = "Message"
                            btnDeclineFriendRequest?.text = "Unfriend"
                            btnDeclineFriendRequest?.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }


        if (userID != null) {
            requestRef?.child(mUser!!.uid)?.child(userID)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child(STATUS).value.toString() == PENDING) {
                                currentState = SENT_REQUEST
                                btnSendFriendRequest?.text = "Cancel Friend Request"
                                btnDeclineFriendRequest?.visibility = View.GONE

                            }
                            if (snapshot.child(STATUS).value.toString() == DECLINE) {
                                currentState = DECLINED_REQUEST
                                btnSendFriendRequest?.text = "Cancel Friend Request"
                                btnDeclineFriendRequest?.visibility = View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        if (userID != null) {
            requestRef?.child(userID)?.child(mUser!!.uid)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // if a user send the current user a friend request
                            if (snapshot.child(STATUS).value.toString() == PENDING) {
                                currentState = RECEIVED_REQUEST
                                btnSendFriendRequest?.text = "Accept Friend Request"
                                btnDeclineFriendRequest?.text = "Decline Friend Request"
                                btnDeclineFriendRequest?.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        if (currentState == DEFAULT) {
            btnSendFriendRequest?.text = "Send Friend Request"
            btnDeclineFriendRequest?.visibility = View.GONE
        }
    }

    //My code: This is responsible for handling all the account states between users when sending friend requests

    @RequiresApi(Build.VERSION_CODES.M)
    private fun performAction(userID: String?) {

        when (currentState) {
            /* Default state
             * Users are not friends
             * No friend request is pending
             */
            DEFAULT -> {
                val hashMap = HashMap<String, Any>()
                hashMap[STATUS] = PENDING

                requestRef!!.child(mUser!!.uid).child(userID!!).updateChildren(hashMap)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "You have sent a friend Request to ${friendDetails?.name}",
                                Toast.LENGTH_LONG
                            ).show()
                            btnDeclineFriendRequest?.visibility = View.GONE
                            currentState = SENT_REQUEST
                            btnSendFriendRequest?.text = "Cancel Friend Request"
                            btnSendFriendRequest?.background =
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

            //Current user sent a friend request
            SENT_REQUEST -> {
                requestRef!!.child(mUser!!.uid).child(userID!!).removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "You have cancelled friend request",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            //change state back to default
                            currentState = DEFAULT
                            btnSendFriendRequest?.text = "Send Friend Request"
                            btnSendFriendRequest?.background =
                                resources.getDrawable(R.drawable.btn_send_request, theme)

                            btnDeclineFriendRequest?.visibility = View.GONE
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
            // Current User declined a friend request
            DECLINED_REQUEST -> {
                requestRef!!.child(mUser!!.uid).child(userID!!).removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "You have cancelled friend request",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            //change state back to default
                            currentState = DEFAULT
                            btnSendFriendRequest?.text = "Send Friend Request"
                            btnDeclineFriendRequest?.visibility = View.GONE
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
                requestRef!!.child(userID!!).child(mUser!!.uid).removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val hashMap = HashMap<String, Any>()
                            hashMap[STATUS] = FRIEND

                            friendRef!!.child(mUser!!.uid).child(userID).updateChildren(hashMap)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        friendRef!!.child(userID).child(mUser!!.uid)
                                            .updateChildren(hashMap).addOnCompleteListener {
                                                Toast.makeText(
                                                    this,
                                                    "You added a Friend",
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()
                                                currentState = FRIEND
                                                btnSendFriendRequest?.text = "Message"
                                                btnDeclineFriendRequest?.text = "Unfriend"
                                                btnDeclineFriendRequest?.visibility = View.VISIBLE

                                                btnSendFriendRequest?.setOnClickListener {
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

}