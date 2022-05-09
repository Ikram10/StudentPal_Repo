package com.example.studentpal.model.remote

import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.view.PostsActivity
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.view.messages.ChatLogActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

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
                    Toast.makeText(activity, "Post Uploaded Successfully", Toast.LENGTH_LONG).show()

                } else {
                    activity.hideProgressDialog()
                    Log.d("Post Upload failed", "$imagePost")
                    Toast.makeText(activity, "Post Failed to Upload", Toast.LENGTH_LONG).show()

                }
            }

    }

    // Retrieves the Posts from Firestore
     suspend fun getPosts() : List<Post> {
         return try {
             db
                 .whereEqualTo(Constants.ID, getCurrentUserId())
                 .get()
                 .await()
                 .documents
                 .mapNotNull {
                     /*Function returns a list containing the non-null results
                      *of applying the toObject function to each element in original array
                      */
                     it.toObject(Post::class.java)
                 }
         } catch (e: Exception) {
             Log.e(TAG, "Error getting Posts", e)
             // Returns empty list if error occurs
             emptyList()
         }
    }

    fun getFriendsPosts(activity: FriendProfile, id: String?) {
        db
            .whereEqualTo(Constants.ID, id)
            .addSnapshotListener { snapshot, e ->
                val postList = arrayListOf<Post>()
                if (e != null) {
                    Log.w("db", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val post = dc.document.toObject(Post::class.java)
                            postList.add(post)
                        }
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            val post = dc.document.toObject(Post::class.java)
                            postList.remove(post)
                        }
                    }
                    //activity.populatePostListToUI(postList)
                }


            }
    }
}