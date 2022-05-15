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

@Suppress("OPT_IN_IS_NOT_ENABLED")
class RequestsAdapter(var context: Context, var list: ArrayList<User>)
    : RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder>() {

    inner class RequestsViewHolder(private val itemBinding: RequestItemBinding):
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(user: User){
            itemBinding.tvMemberName.text = user.name
            itemBinding.tvMemberUsername.text = user.username
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


        holder.itemView.findViewById<ImageButton>(R.id.btn_accept).setOnClickListener {
            GlobalScope.launch {
                createFriendship(context as Activity, model, fetchCurrentUser()!! )
                list.removeAt(position)
                notifyItemChanged(position)
                notifyItemRangeRemoved(position, 1)
            }
        }
        holder.itemView.findViewById<ImageButton>(R.id.btn_reject).setOnClickListener {
            GlobalScope.launch {
                FriendshipsDatabase.deleteReceiverFriendRequest(context as Activity, model)
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