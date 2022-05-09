package com.example.studentpal.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.view.messages.ChatLogActivity
import com.example.studentpal.databinding.ItemProfileBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants

class FriendsListAdapter(var context: Context,  var list: ArrayList<User>)
    : RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>() {

        inner class FriendsViewHolder(private val itemBinding : ItemProfileBinding):
                RecyclerView.ViewHolder(itemBinding.root) {
                    fun bindItem(user: User){
                        itemBinding.cvUsername.text = user.name
                        itemBinding.cvEmail.text = user.email
                        Glide
                            .with(context)
                            .load(user.image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(itemBinding.ivProfileImage)

                        itemBinding.btnViewProfile.setOnClickListener {
                            val intent = Intent(it.context, FriendProfile::class.java)
                            intent.putExtra(Constants.USER_KEY, user)
                            it.context.startActivity(intent)
                        }
                        itemBinding.btnSendMessage.setOnClickListener {
                            val intent = Intent(it.context, ChatLogActivity::class.java)
                            intent.putExtra(Constants.USER_KEY, user)
                            it.context.startActivity(intent)
                        }

                    }
                }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
      return FriendsViewHolder(
          ItemProfileBinding
              .inflate(LayoutInflater
                  .from(parent.context), parent, false)
      )
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val model = list[position]
        holder.bindItem(model)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}