package com.example.studentpal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.models.Board

open class BoardItemsAdapter (private val context: Context, private var list: ArrayList<Board>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_board, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /* list parameter contains an array list of Boards.
         * position will provide a single Board object in the array
         */
        val model = list[position]
        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_board_image))
        }

        holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
        holder.itemView.findViewById<TextView>(R.id.tv_created_by).text = "Created by: ${model.createBy}"

        //handles the functionality when an event item is selected
        holder.itemView.setOnClickListener{

            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    interface OnClickListener {
        /* onClick function takes a position where a click was recieved
         * and a model that was clicked, which contains the position
         */
        fun onClick(position: Int, model: Board)
    }



    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}