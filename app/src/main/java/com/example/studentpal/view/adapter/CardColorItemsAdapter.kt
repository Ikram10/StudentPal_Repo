package com.example.studentpal.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentpal.R

/* This adapter intends to be used for inside the EventCardColorList Dialog.
 * Which displays a list of colors the user can select
 */
class CardColorItemsAdapter(
    private val context : Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card_color, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //item is an array of strings which will be parsed into a color
        val item = list[position]
        if (holder is MyViewHolder) {

            holder.itemView.findViewById<View>(R.id.view_main).setBackgroundColor(Color.parseColor(item))
            //if the item is the selected color
            if (item == mSelectedColor){
                holder.itemView.findViewById<AppCompatImageView>(R.id.iv_selected_color).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<AppCompatImageView>(R.id.iv_selected_color).visibility = View.GONE
            }

            holder.itemView.setOnClickListener{

                if (onItemClickListener != null) {
                    onItemClickListener!!.OnClick(position, item)
                }
            }

        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnItemClickListener {
        fun OnClick(position: Int, Color: String)
    }
}