package com.example.studentpal.activities.messages

import android.content.ContentValues
import android.util.Log
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.models.ChatMessage
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessageRow(private val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.latest_message_tv).text =
            chatMessage.text

        //this variable will hold the id of the user the message is sent from
        val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else
            chatMessage.fromId

        //My Code: Fetches the user document from Firestore that has the same id as chatPartnerId
        val ref = FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(
            "id", chatPartnerId
        )
        ref.addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(ContentValues.TAG, "Listen Failed")
            }
            for (dc: DocumentChange in value!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.MODIFIED -> {
                        //converts the modified user document into a User object so we can access its properties
                        chatPartnerUser = dc.document.toObject(User::class.java)
                        viewHolder.itemView.findViewById<TextView>(R.id.name_tv).text = chatPartnerUser!!.name

                        Glide
                            .with(viewHolder.itemView.context)
                            .load(chatPartnerUser!!.image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_nav_user)
                            .into(viewHolder.itemView.findViewById(R.id.profile_iv))
                    }
                    else -> {
                        chatPartnerUser = dc.document.toObject(User::class.java)
                        viewHolder.itemView.findViewById<TextView>(R.id.name_tv).text = chatPartnerUser!!.name

                        Glide
                            .with(viewHolder.itemView.context)
                            .load(chatPartnerUser!!.image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_nav_user)
                            .into(viewHolder.itemView.findViewById(R.id.profile_iv))
                    }
                }
            }
        }
    }


    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}