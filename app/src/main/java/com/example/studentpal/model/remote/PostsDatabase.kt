package com.example.studentpal.model.remote

import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.view.profile.PostsActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
/**
 * This object contains all the functionalities responsible for retrieving and storing data
 * in the post's collection in Firestore.
 *
 * The author implemented an object to create a singleton of the database, that can be accessed
 * anywhere in the code
 *
 * [My Code ]: The entire code in this object was written by the author
 *
 */
object PostsDatabase {
    private const val TAG = "PostsDatabase"
    //Reference to post's collection in firestore
    private val db = FirebaseFirestore.getInstance().collection(Constants.POSTS)

    /**
     * Method stored the post's data in firestore
     */
    fun uploadPost(activity: PostsActivity, imagePost: Post) {
        db
            .document(imagePost.docID)
            .set(imagePost) // If a document already exists, it will be overwritten.
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

    /**
     * Method retrieves all the current user's posts from Firestore
     * @return all the users posts stored in firestore
     */
     suspend fun getPosts() : List<Post> {
         return try {
             db
                 .whereEqualTo(Constants.ID, getCurrentUserId())
                 .get()
                 .await()
                 .documents
                 .mapNotNull {
                     /*Function returns a list containing the non-null results
                      *of applying the toObject function converting each document into a post object
                      */
                     it.toObject(Post::class.java)
                 }
         } catch (e: Exception) {
             Log.e(TAG, "Error getting Posts", e)
             // Returns empty list if error occurs
             emptyList()
         }
    }

    /**
     * Method returns all the friends post in firestore
     */
    fun getFriendsPosts(id: String?) {
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