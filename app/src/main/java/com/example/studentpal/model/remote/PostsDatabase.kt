package com.example.studentpal.model.remote

import android.util.Log
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Post
import com.example.studentpal.view.PostsActivity
import com.google.firebase.firestore.FirebaseFirestore

object PostsDatabase {
    private const val TAG = "PostsDatabase"
    private val db = FirebaseFirestore.getInstance().collection(Constants.POSTS)

    fun uploadPost(activity: PostsActivity, imagePost: Post) {

        db.document(imagePost.docID)
            .set(imagePost)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    activity.hideProgressDialog()
                    Log.d("Post Uploaded", "$imagePost")

                } else {
                    activity.hideProgressDialog()
                    Log.d("Post Upload failed", "$imagePost")

                }
            }

    }
}