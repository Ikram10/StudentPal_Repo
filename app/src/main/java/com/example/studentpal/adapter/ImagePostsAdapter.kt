package com.example.studentpal.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.ItemAssignFriendBinding
import com.example.studentpal.databinding.SinglePostViewBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.models.ImagePost
import com.example.studentpal.models.User

class ImagePostsAdapter(var context: Context, var list: ArrayList<ImagePost>)
    : RecyclerView.Adapter<ImagePostsAdapter.ImagePostViewHolder>(){

    private var onClickListener: ImagePostsAdapter.OnClickListener? = null

    inner class  ImagePostViewHolder(private var itemBinding: SinglePostViewBinding ):
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItem(post: ImagePost){
            itemBinding.tvPostDate.text = post.eventDate
            itemBinding.tvPostCaption.text = post.caption
            itemBinding.tvLikeCount.text = post.likes.toString()
            itemBinding.ibLike.setOnClickListener {
                    if (onClickListener != null) {
                        onClickListener!!.onClick(position, post)

                }

            }
            Glide
                .with(context)
                .load(post.image)
                .centerCrop()
                .into(itemBinding.ivPostImage)
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