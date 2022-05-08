package com.example.studentpal.view.messages

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.databinding.ActivityChatLogBinding
import com.example.studentpal.common.fcm.RetrofitInstance
import com.example.studentpal.model.entities.ChatMessage
import com.example.studentpal.model.entities.NotificationData
import com.example.studentpal.model.entities.PushNotification
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

const val TOPIC = "/topics/myTopic"
class ChatLogActivity : BaseActivity() {

    companion object {
        const val TAG = "ChatLog"
        var currentUser: User? = null
    }

    private var binding: ActivityChatLogBinding? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    //toUser = the user the currently signed in User wants to interact with
    private var toUser: User? = null
    private val adapter = GroupAdapter<GroupieViewHolder>()



    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        binding?.recyclerviewChatLog?.adapter = adapter


        // need to provide the USER_KEY to extract the User from intent
        toUser = intent.getParcelableExtra(Constants.USER_KEY)

        setupActionBar()

        binding?.recyclerviewChatLog?.adapter = adapter

        binding?.btnSend?.setOnClickListener {
            Log.d(TAG, "Attempt to send message ")
            performSendMessage()

        }
        GlobalScope.launch {
            fetchCurrentUser(getCurrentUserID())
        }

        listenForMessages()

    }


    //My code:
    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.id
        //User-Messages query, between two users
        val reference = FirebaseFirestore.getInstance()
            .collection(Constants.USER_MESSAGES)
            .document(fromId!!)
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

                            //conditional statement checks if message was sent by currently signed in user or from other user
                            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {

                                //chat items have different layouts and is dependant on who sent the message
                                adapter.add(ChatFromItem(chatMessage.text, currentUser!!, chatMessage.timeStamp))
                            } else {
                                adapter.add(ChatToItem(chatMessage.text, toUser!!, chatMessage.timeStamp))
                            }

                            //scrolls to the bottom of the chat log, to always display latest message
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
        val message = binding?.editTextChatLog?.text

        val fromId: String? = FirebaseAuth.getInstance().uid

        val toId = toUser?.id

        if (message!!.isNotEmpty()){
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
                message.toString(),
                System.currentTimeMillis()
            )
            Log.d("long time", "Time is: ${convertLongToTime(chatMessage.timeStamp)}")
            reference.document().set(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved chat message: ${reference.id}")

                Log.d("userFCM","${toUser?.fcmToken}")

                    PushNotification(
                        NotificationData("Message", message.toString()),
                        toUser!!.fcmToken
                    ).also {
                        sendNotification(it)
                    }


                //clears the edit text when the user hits the send button
                message.clear()

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


        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }


    }

    inner class ChatFromItem(val text: String, val user: User, private val timeSent: Long) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.tv_message_from).text = text
            viewHolder.itemView.findViewById<TextView>(R.id.tv_time_sent_from).text = convertLongToTime(timeSent)

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
    inner class ChatToItem(val text: String, val user: User, val timeStamp: Long) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            //textview that shows the users message sent in the chat log
            val toMessageView = viewHolder.itemView.findViewById<TextView>(R.id.tv_message_to)
            toMessageView.text = text
            viewHolder.itemView.findViewById<TextView>(R.id.tv_time_sent_to).text = convertLongToTime(timeStamp)

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
        toolbar = binding?.include?.tbChatlog
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = toUser?.name
        }

        // Sets the profile image in Action bar
        toolbar?.findViewById<CircleImageView>(R.id.tb_profile_image).let {
                Glide
                    .with(this)
                    .load(toUser?.image)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(it!!)

        }
        // Sets the User name in Toolbar
        toolbar?.findViewById<TextView>(R.id.tb_profile_name)?.text = toUser?.name
        // Sets user status in Toolbar
        toolbar?.findViewById<TextView>(R.id.tb_online)?.text  = toUser?.status




        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return format.format(date)
    }

    // Sends notification to firebase server
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