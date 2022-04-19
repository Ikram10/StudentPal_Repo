package com.example.studentpal.adapter

import android.content.Context
import android.print.PrintDocumentAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.ItemAssignFriendBinding
import com.example.studentpal.databinding.ItemProfileBinding
import com.example.studentpal.models.User
import com.google.firebase.auth.FirebaseUser

//UserAdapter will display a list of Users in the Recycler view
class UsersAdapter(var context: Context, var list: ArrayList<User>) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>(){

    inner class  UsersViewHolder(val itemBinding: ItemAssignFriendBinding):
        RecyclerView.ViewHolder(itemBinding.root) {
            fun bindItem(user: User){
                itemBinding.tvMemberName.text = user.name
                itemBinding.tvMemberEmail.text = user.email

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            ItemAssignFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val model = list[position]

        if ()
        holder.bindItem(model)
        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.itemView.findViewById(R.id.iv_friend_image))
    }

    override fun getItemCount(): Int {
        return list.size
    }

}


