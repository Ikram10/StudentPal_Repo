package com.example.studentpal.messages

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.databinding.ActivityChatLogBinding
import com.example.studentpal.models.ChatMessage
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatLogActivity : BaseActivity() {

    companion object {
        const val TAG = "ChatLog"
    }

    private var binding: ActivityChatLogBinding? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null

    //toUser = the user the currently signed in User wants to interact with
    private var toUser: User? = null

    //currently signed in user
    private var user: User? = null
    private val adapter = GroupAdapter<GroupieViewHolder>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        binding?.recyclerviewChatLog?.adapter = adapter

        // need to provide the USER_KEY to extract the User
        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)

        setupActionBar()

        binding?.recyclerviewChatLog?.adapter = adapter

        binding?.btnSend?.setOnClickListener {
            Log.d(TAG, "Attempt to send message ")
            performSendMessage()

        }

        listenForMessages()

    }

    //My code:
    private fun listenForMessages() {

        val fromId: String? = FirebaseAuth.getInstance().uid
        val toId = toUser?.id

        val reference =
            FirebaseFirestore.getInstance().collection(Constants.USER_MESSAGES).document(fromId!!)
                .collection(toId!!)
        reference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen Failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (dc: DocumentChange in snapshot.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val chatMessage = dc.document.toObject(ChatMessage::class.java)
                            Log.d(TAG, chatMessage.text)

                            //conditional statement checks if message was sent by currently signed in user or from another user
                            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {

                                val currentUser = LatestMessagesActivity.currentUser
                                //chat items have different layouts and is dependant on who sent the message
                                adapter.add(ChatFromItem(chatMessage.text, currentUser!!))

                            } else {
                                adapter.add(ChatToItem(chatMessage.text, toUser!!))
                            }

                            //scrolls to the bottom of the chat log wh
                            binding?.recyclerviewChatLog?.scrollToPosition(adapter.itemCount - 1)

                        }
                        DocumentChange.Type.MODIFIED -> {

                        }
                        DocumentChange.Type.REMOVED -> {
                        }
                    }
                }

            }
        }
    }

    private fun performSendMessage() {

        //the text box the allows users to enter a message
        val text = binding?.editTextChatLog?.text

        val fromId: String? = FirebaseAuth.getInstance().uid

        val toId = toUser?.id

        // reference points to the User-Message collection in Firestore which includes a sub-collection of the receiving user.
        val reference =
            FirebaseFirestore.getInstance().collection(Constants.USER_MESSAGES).document(fromId!!)
                .collection(toId!!)

        //a reference to the user the currently signed in user is sending a message to
        val toReference =
            FirebaseFirestore.getInstance().collection(Constants.USER_MESSAGES).document(toId)
                .collection(fromId)

        val chatMessage = ChatMessage(
            reference.document().id,
            fromId,
            toId,
            text.toString(),
            System.currentTimeMillis() / 1000
        )

        reference.document().set(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved chat message: ${reference.id}")
            //clears the edit text when the user hits the send button
            text?.clear()

            //when user hits the send button recycler view scrolls to the last message sent position
            binding?.recyclerviewChatLog?.scrollToPosition(adapter.itemCount - 1)
            Log.d("ChatLogTest", "Message sent by: $fromId to $toId")

        }
        toReference.document().set(chatMessage)

        //A realtime database is used here because it provides the necessary functionalities to display the latest messages
        val latestMessageRef =
            FirebaseDatabase.getInstance("https://studentpal-8f3d3-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("/latest-messages/$fromId/$toId")

        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef =
            FirebaseDatabase.getInstance("https://studentpal-8f3d3-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("/latest-messages/$toId/$fromId")

        latestMessageToRef.setValue(chatMessage)
    }

    inner class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.tv_message_from).text = text

            val targetImageView =
                viewHolder.itemView.findViewById<ImageView>(R.id.iv_profile_image_from)

            //Load user image into to the chat screen when sending a message
            Glide
                .with(this@ChatLogActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_nav_user)
                .into(targetImageView)

        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }

    }

    //this class is responsible for
    inner class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            //textview that shows the users message sent in the chat log
            val toMessageView = viewHolder.itemView.findViewById<TextView>(R.id.tv_message_to)
            toMessageView.text = text

            //The imageview that holds the users image in the xml file
            val targetImageView =
                viewHolder.itemView.findViewById<ImageView>(R.id.iv_profile_image_to)

            //Load user image into to the chat screen when sending a message
            Glide
                .with(this@ChatLogActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_nav_user)
                .into(targetImageView)


        }

        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }

    }

    private fun setupActionBar() {
        toolbar = binding?.toolbarChatLog
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = toUser?.name


        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

}