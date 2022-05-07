package com.example.studentpal.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.SinglePostViewBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.model.entities.ImagePost
import com.example.studentpal.common.Constants
import com.example.studentpal.model.remote.UserDatabase.getCurrentUserId
import com.google.firebase.firestore.FirebaseFirestore

class ImagePostsAdapter(var context: Context, var list: ArrayList<ImagePost>)
    : RecyclerView.Adapter<ImagePostsAdapter.ImagePostViewHolder>(){

    private var onClickListener: OnClickListener? = null

    inner class  ImagePostViewHolder(private var itemBinding: SinglePostViewBinding ):
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItem(post: ImagePost) {
            itemBinding.tvPostDate.text = post.eventDate
            itemBinding.tvPostCaption.text = post.caption
            itemBinding.tvLikeCount.text = post.likes.toString()
            itemBinding.ibLike.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, post)

                }

            }
            // Loads post image into the image view
            Glide
                .with(context)
                .load(post.image)
                .centerCrop()
                .into(itemBinding.ivPostImage)

            // Hides delete button if the current user did not post the image
            if (post.id != getCurrentUserId()) {
                itemBinding.ibDeletePost.visibility = View.GONE
            }


        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ImagePostViewHolder {
        return ImagePostViewHolder(
           SinglePostViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImagePostViewHolder, position: Int) {
        val model = list[position]

        holder.bindItem(model)
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }
            // Deletes the selected post
            holder
                .itemView
                .findViewById<AppCompatImageButton>(R.id.ib_delete_post)
                .setOnClickListener {
                val builder = AlertDialog.Builder(context, R.style.MyDialogTheme)
                //The event can only be deleted by the event creator
                builder.setTitle("Alert")
                    .setIcon(R.drawable.ic_round_warning_24)
                    .setMessage("Do you want to delete Post?")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { _, _ ->
                        val db = FirebaseFirestore.getInstance()
                        db.collection(Constants.POSTS)
                            .document(model.docID)
                            .delete()
                            .addOnSuccessListener {
                                //My code: Handles the deletion of events
                                list.removeAt(position)
                                notifyItemChanged(position)
                                notifyItemRangeRemoved(position, 1)
                                Log.d("FirestoreDelete", "Post deleted from FireStore.")
                                Toast.makeText(
                                    context,
                                    "Post deleted successfully",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .setNegativeButton("No") { DialogInterface, _ ->
                        DialogInterface.cancel()
                    }
                    .show()

        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    interface OnClickListener {
        /* onClick function takes a position where a click was recieved
         * and a model that was clicked, which contains the position
         */
        fun onClick(position: Int, model: ImagePost)
    }

    //on click listener when event cards are clicked
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}