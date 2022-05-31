package com.example.studentpal.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.view.messages.ChatLogActivity
import com.example.studentpal.databinding.ItemProfileBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants
/**
 * This adapter will display a list of friends of the user in the Recycler view.
 *
 * The code was reused from previous examples implemented in this project, but was coded by the author to implement this feature.
 */
class FriendsListAdapter(var context: Context,  var list: ArrayList<User>)
    : RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>() {
        inner class FriendsViewHolder(private val itemBinding : ItemProfileBinding):
                RecyclerView.ViewHolder(itemBinding.root) {
                    fun bindItem(user: User){
                        //Loads friend information into textview
                        itemBinding.cvName.text = user.name
                        itemBinding.cvUsername.text = user.username
                        itemBinding.civStatus.text = user.status

                        //Loads friends profile image into imageview
                        Glide
                            .with(context)
                            .load(user.image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(itemBinding.ivProfileImage)

                        //[My Code ]:Sets the status colour
                        when (user.status) {
                            "Available" -> {
                                // Change colour green when status message is Available
                                itemBinding.civStatus.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.available
                                    )
                                )
                            }
                            // Change colour red when status message is Available
                            "Unavailable" -> {
                                itemBinding.civStatus.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.unavailable
                                    )
                                )
                            }
                        }
                        //View Profile button clicked
                        itemBinding.btnViewProfile.setOnClickListener {
                            // navigates user friends profile screen
                            val intent = Intent(it.context, FriendProfile::class.java)
                            intent.putExtra(Constants.USER_KEY, user)
                            it.context.startActivity(intent)
                        }
                        // Send Message button clicked
                        itemBinding.btnSendMessage.setOnClickListener {
                            // navigates user to chat log with friend
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