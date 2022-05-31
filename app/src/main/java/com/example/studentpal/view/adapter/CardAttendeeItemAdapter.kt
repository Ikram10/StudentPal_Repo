package com.example.studentpal.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.model.entities.User
/**
 * This adapter will display a list of friends assigned to an event in the card Recycler view.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 *
 * @see[com.example.studentpal.common.References]
 */
open class CardAttendeeItemAdapter(
    private val context: Context,
    private val list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_attendee,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder)
        /* if there are no users assigned to event only the add button will be visible
         * otherwise the users image will be displayed
         */
            if (position == list.size - 1) {
                holder.itemView.findViewById<ImageView>(R.id.iv_add_member).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_member_image).visibility =
                    View.GONE
            } else {
                holder.itemView.findViewById<ImageView>(R.id.iv_add_member).visibility = View.GONE
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_member_image).visibility =
                    View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .placeholder(R.drawable.ic_user_place_holder)
                    .centerCrop()
                    .into(holder.itemView.findViewById(R.id.iv_selected_member_image))
            }

        holder.itemView.setOnClickListener{

            if (onClickListener != null){
                onClickListener!!.onClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }


    interface OnClickListener {
        fun onClick()
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}