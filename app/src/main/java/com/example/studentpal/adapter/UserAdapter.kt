package com.example.studentpal.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.ItemProfileBinding
import com.example.studentpal.models.User
//UserAdapter will display a list of Users in the Recycler view
class UserAdapter(var context: Context, var userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var userListFull = ArrayList<User>(userList)

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemProfileBinding = ItemProfileBinding.bind(itemView)


    }

    //when the recycler view needs to display each user item, this function will inflate an instance of the layout file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(itemView)
    }

    //This method gets called whenever data needs to be added to the UserViewHolder
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.binding.cvUsername.text = user.name
        holder.binding.cvEmail.text = user.email
        holder.binding.civStatus.text = user.status

        Glide
            .with(context)
            .load(user.image)
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.binding.ivProfileImage)

        //My code
        when(user.status) {
            "Available" -> {
                holder.binding.civStatus.setTextColor(ContextCompat.getColor(context, R.color.available))
            }
            "Unavailable" -> {
                holder.binding.civStatus.setTextColor(ContextCompat.getColor(context, R.color.unavailable))
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}


