package com.example.studentpal.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.activities.FriendsActivity
import com.example.studentpal.models.Board
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BoardItemsAdapter (private val context: Context, private var list: ArrayList<Board>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }

        //handles functionality for deleting an event
        holder.itemView.findViewById<AppCompatImageButton>(R.id.ib_delete_event).setOnClickListener {

            val builder = AlertDialog.Builder(context)
            //The event can only be deleted by the event creator
            if (model.creatorID == FirebaseAuth.getInstance().currentUser!!.uid) {
                builder.setTitle("Alert")
                    .setMessage("Do you want to delete Event?")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { _, _ ->
                        val db = FirebaseFirestore.getInstance()
                        db.collection(Constants.BOARDS).document(model.documentID).delete().addOnSuccessListener {
                            Log.d("FirestoreDelete", "Event deleted from FireStore.")
                            Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("No") { DialogInterface, _ ->
                        DialogInterface.cancel()
                    }
                    .show()
            } else {
                builder.setTitle("Alert")
                    .setMessage("Only the event creator has permission to delete the event")
                    .setCancelable(true)
            }

            }

        holder.itemView.findViewById<AppCompatImageButton>(R.id.assign_friends).setOnClickListener {
            //intent passes this event details to the friends activity
            val intent = Intent(it.context, FriendsActivity::class.java)
            intent.putExtra(Constants.BOARD_DETAIL, model)
            it.context.startActivity(intent)
        }
    }



    interface OnClickListener {
        /* onClick function takes a position where a click was recieved
         * and a model that was clicked, which contains the position
         */
        fun onClick(position: Int, model: Board)
    }

    //on click listener when event cards are clicked
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener

    }


    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}