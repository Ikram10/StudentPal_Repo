package com.example.studentpal.view.messages

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityChatLogBinding
import com.example.studentpal.model.entities.ChatMessage
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.fcm.NotificationData
import com.example.studentpal.model.fcm.PushNotification
import com.example.studentpal.model.fcm.RetrofitInstance
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.view.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * This activity is responsible for displaying the chat log between two
 * users and handles the sending of messages.
 *
 * The code displayed was adapted from Brian Voong's "Kotlin Firebase Messenger" Tutorial (see references file)
 * However, the author significantly evolved the code produced by Voong to accommodate the Server implementation.
 * For instance, the tutorial implemented the Realtime database as a database solution to store messages, but StudentPal
 * used Cloud Firestore. The
 *
 * All code that was adapted by the author will be labelled [My Code].
 *
 * @see[com.example.studentpal.common.References]
 */
@Suppress("OPT_IN_IS_NOT_ENABLED")
class ChatLogActivity : BaseActivity() {

    private var binding: ActivityChatLogBinding? = null

    private var toolbar: androidx.appcompat.widget.Toolbar? = null

    // Recipient User
    private var toUser: User? = null

    // Logged in User
    private var currentUser: User? = null

    private val adapter = GroupAdapter<GroupieViewHolder>()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Initialise current user
        GlobalScope.launch {
            currentUser = fetchCurrentUser()!!
        }

        // need to provide the USER_KEY to extract the User from intent
        toUser = intent.getParcelableExtra(Constants.USER_KEY)

        binding?.recyclerviewChatLog?.adapter = adapter

        setupActionBar()

        binding?.recyclerviewChatLog?.adapter = adapter

        binding?.btnSend?.setOnClickListener {
            Log.d(TAG, "Attempt to send message ")
            performSendMessage()

        }

        listenForMessages()
    }

    /**[Adapted ]: Method listens for new messages being sent and displays it in the chat log with
     * the appropriate layout structure.
     *
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun listenForMessages() {
        val fromId = getCurrentUserID()
        val toId = toUser?.id
        //[Adapted]: user-Messages firestore query, between the two users
        val reference = FirebaseFirestore.getInstance()
            .collection(Constants.USER_MESSAGES)
            .document(fromId)
            .collection(toId!!)

        reference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen Failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                for (dc: DocumentChange in snapshot.documentChanges) {
                    when (dc.type) {
                        //when new message is added
                        DocumentChange.Type.ADDED -> {
                            val chatMessage = dc.document.toObject(ChatMessage::class.java)
                            Log.d(TAG, chatMessage.text)
                            // checks if message was sent by currently signed in user or from other user
                            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                                /* Launches Coroutine in the IO dispatcher
                                 * Because we are executing an IO operation (Firestore request)
                                 */
                                GlobalScope.launch(Dispatchers.IO) {
                                    currentUser = fetchCurrentUser()!!
                                    // reverts back to the Main thread to interact with UI
                                    withContext(Dispatchers.Main) {
                                        adapter.add(
                                            ChatFromItem(
                                                chatMessage.text,
                                                currentUser!!, chatMessage.timeStamp
                                            )
                                        )
                                    }
                                }
                            }
                            // message sender is the other user
                            else {
                                adapter.add(
                                    ChatToItem(
                                        chatMessage.text,
                                        toUser!!,
                                        chatMessage.timeStamp
                                    )
                                )
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

    /**
     * [Adapted ]: Method handles the functionality when a user sends a message.
     * This was adapted to accommodate Cloud firestore and implement push notifications
     */
    private fun performSendMessage() {
        // text box the allows users to enter a message
        val message = binding?.editTextChatLog?.text

        // Current user id
        val fromId: String = getCurrentUserID()

        // Recipient user id
        val toId = toUser?.id

        if (message!!.isNotEmpty()) {
            // reference points to the User-Message collection in Firestore which includes a sub-collection of the receiving user.
            val reference =
                FirebaseFirestore.getInstance().collection(Constants.USER_MESSAGES).document(fromId)
                    .collection(toId!!)

            //a reference to the user the currently signed in user is sending a message to
            val toReference =
                FirebaseFirestore.getInstance().collection(Constants.USER_MESSAGES).document(toId)
                    .collection(fromId)

            // chat message object
            val chatMessage = ChatMessage(
                reference.document().id,
                fromId,
                toId,
                message.toString(),
                System.currentTimeMillis()
            )

            Log.d("long time", "Time is: ${convertLongToTime(chatMessage.timeStamp)}")

            // Stores message data in Firestore
            reference.document().set(chatMessage).addOnSuccessListener {
                Log.d(TAG, "Saved chat message: ${reference.id}")

                Log.d("userFCM", "${toUser?.fcmToken}")

                // [Adapted ]: prepares push notification with recipients fcm token
                PushNotification(
                    NotificationData("Message", message.toString()),
                    toUser!!.fcmToken
                ).also {
                    sendNotification(it)
                }


                // clears the edit text when the user hits the send button
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

    /**
     * Constructs the message received layout
     *
     * This was copied from Voong's tutorial
     * @see[com.example.studentpal.common.References]
     */
    inner class ChatFromItem(val text: String, val user: User, private val timeSent: Long) :
        Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            //text view that shows the users message received in the chat log
            viewHolder.itemView.findViewById<TextView>(R.id.tv_message_from).text = text
            // Message time sent loaded
            viewHolder.itemView.findViewById<TextView>(R.id.tv_time_sent_from).text =
                convertLongToTime(timeSent)
            // user profile image
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
        // sets the layout
        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }

    /**
     * Constructs the message sent layout
     *
     * This was copied from Voong's tutorial
     * @see[com.example.studentpal.common.References]
     */
    inner class ChatToItem(val text: String, val user: User, private val timeStamp: Long) :
        Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            //text view that shows the users message sent in the chat log
            val toMessageView = viewHolder.itemView.findViewById<TextView>(R.id.tv_message_to)
            toMessageView.text = text

            // Message time received loaded
            viewHolder.itemView.findViewById<TextView>(R.id.tv_time_sent_to).text =
                convertLongToTime(timeStamp)

            //The imageview that holds the users image
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

    /**
     * [Adapted ]: Sets up the Action bar to contain the recipients name
     * profile image and status.
     */
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
        // Sets the User name in Action bar
        toolbar?.findViewById<TextView>(R.id.tb_profile_name)?.text = toUser?.name
        // Sets user status in Action bar
        toolbar?.findViewById<TextView>(R.id.tb_online)?.text = toUser?.status


        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Method converts the time in long stored in Firestore a readable string
     * Reused from Stack Overflow
     * @see [com.example.studentpal.common.References]
     *
     */
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return format.format(date)
    }

    // Sends notification to firebase server
    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //network request: Post request
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

    companion object {
        private const val TAG = "ChatLog"
    }

}