package com.example.studentpal.view.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.RequestItemBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.FriendshipsDatabase
import com.example.studentpal.model.remote.FriendshipsDatabase.createFriendship
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
/**
 * This adapter will display a list of friend requests received in the Recycler view.
 *
 * The code was reused from previous examples, but was coded by the author to implement this feature.
 */
@Suppress("OPT_IN_IS_NOT_ENABLED")
class RequestsAdapter(var context: Context, var list: ArrayList<User>)
    : RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder>() {

    inner class RequestsViewHolder(private val itemBinding: RequestItemBinding):
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(user: User){
            //Loads user name into textview
            itemBinding.tvMemberName.text = user.name
            //Loads user username into textview
            itemBinding.tvMemberUsername.text = user.username
            //Loads user profile image
            Glide
                .with(context)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(itemBinding.ivFriendImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsViewHolder {
        return RequestsViewHolder(
            RequestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: RequestsViewHolder, position: Int) {
        val model = list[position]

        holder.bindItem(model)

        // [My Code ]: Accept button clicked
        holder.itemView.findViewById<ImageButton>(R.id.btn_accept).setOnClickListener {
            GlobalScope.launch {
                // Store friendship details in Firestore
                createFriendship(context as Activity, model, fetchCurrentUser()!! )
                // remove the request item from the Recyclerview
                list.removeAt(position)
                notifyItemChanged(position)
                notifyItemRangeRemoved(position, 1)
            }
        }
        // [My Code ]: Reject button clicked
        holder.itemView.findViewById<ImageButton>(R.id.btn_reject).setOnClickListener {
            GlobalScope.launch {
                // Deletes tge friend request data from Firestore
                FriendshipsDatabase.deleteReceiverFriendRequest(context as Activity, model)
                // remove the request item from the Recyclerview
                list.removeAt(position)
                notifyItemChanged(position)
                notifyItemRangeRemoved(position, 1)
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}