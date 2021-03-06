package com.example.studentpal.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.events.AssignFriendsActivity
import com.example.studentpal.view.events.EditEventActivity
import com.example.studentpal.model.entities.Event
import com.example.studentpal.common.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
/**
 * This adapter will display a list of events created by the user in a Recycler view.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However alterations were made to the code to suit the project's requirements.
 *
 * @see[com.example.studentpal.common.References]
 */
open class EventItemsAdapter(private val context: Context, private var list: ArrayList<Event>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_event, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /* list parameter contains an array list of events.
         * position will provide a single Event object in the array
         */
        val model = list[position]
        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(holder.itemView.findViewById(R.id.iv_board_image))


            val card: CardView = holder.itemView.findViewById(R.id.cv_event)
            if (model.cardColor.isNotEmpty()) {
                card.setCardBackgroundColor(Color.parseColor(model.cardColor))
            }
            holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text =
                "Created by: ${model.createBy}"
            holder.itemView.findViewById<TextView>(R.id.tv_assigned).text =
                "Assigned: ${model.assignedTo.size - 1} users"

            holder.itemView.findViewById<TextView>(R.id.tv_event_date).text =
                "Date: ${convertLongToTime(model.eventDate)}"

            //handles the functionality when an event card is selected
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }

            // Enables event edit buttons if the event creator id is the same as current user id
            if (model.creatorID == BaseActivity().getCurrentUserID()) {
                holder.itemView.findViewById<LinearLayoutCompat>(R.id.layout_edit_btns).visibility =
                    View.VISIBLE
            }
            //handles functionality for deleting an event
            holder.itemView.findViewById<AppCompatImageButton>(R.id.ib_delete_event)
                .setOnClickListener {

                    val builder = AlertDialog.Builder(context, R.style.MyDialogTheme)
                    //The event can only be deleted by the event creator
                        builder.setTitle("Alert")
                            .setIcon(R.drawable.ic_round_warning_24)
                            .setMessage("Do you want to delete Event:  ${model.name}?")
                            .setCancelable(true)
                            .setPositiveButton("Yes") { _, _ ->
                                val db = FirebaseFirestore.getInstance()
                                db.collection(Constants.EVENTS).document(model.documentID).delete()
                                    .addOnSuccessListener {
                                        //My code: Handles the deletion of events
                                        list.removeAt(position)
                                        notifyItemChanged(position)
                                        notifyItemRangeRemoved(position, 1)
                                        Log.d("FirestoreDelete", "Event deleted from FireStore.")
                                        Toast.makeText(
                                            context,
                                            "Event deleted successfully",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                            .setNegativeButton("No") { DialogInterface, _ ->
                                DialogInterface.cancel()
                            }
                            .show()


                }


            //handles navigation to the edit event activity
            holder.itemView.findViewById<AppCompatImageButton>(R.id.ib_edit_event)
                .setOnClickListener {
                    val intent = Intent(it.context, EditEventActivity::class.java)
                    intent.putExtra(Constants.EVENT_DETAIL, model)
                    it.context.startActivity(intent)
                }

            //handles navigation to the edit assign friends activity
            holder.itemView.findViewById<AppCompatImageButton>(R.id.assign_friends)
                .setOnClickListener {
                    //intent passes this event details to the friends activity
                    val intent = Intent(it.context, AssignFriendsActivity::class.java)
                    intent.putExtra(Constants.EVENT_DETAIL, model)
                    //TODO start activity for result
                    it.context.startActivity(intent)

                }
        }
    }

    //converts the long value stored in Firestore to a string
    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        return format.format(date)
    }

    interface OnClickListener {
        /* onClick function takes a position where a click was received
         * and a model that was clicked, which contains the position
         */
        fun onClick(position: Int, model: Event)
    }

    //on click listener when event cards are clicked
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener

    }


    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}