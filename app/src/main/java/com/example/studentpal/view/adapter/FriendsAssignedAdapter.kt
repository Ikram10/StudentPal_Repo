package com.example.studentpal.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants
/**
 * This adapter will display a list of friends assigned to an event in a Recycler view.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However alterations were made to the code to suit the project's requirements.
 *
 * @see[com.example.studentpal.common.References]
 */
open class FriendsAssignedAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return MyViewHolder(
           LayoutInflater.from(context).inflate(
               R.layout.item_assign_friend,
               parent,
               false
           )
       )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_friend_image))

            holder.itemView.findViewById<TextView>(R.id.tv_member_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_member_username).text = model.username
        }



        holder.itemView.setOnClickListener{
            if (onClickListener != null){
                if (model.selected){
                    onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                } else {
                    onClickListener!!.onClick(position, model, Constants.SELECT)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }
}