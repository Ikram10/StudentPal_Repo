package com.example.studentpal.model.remote

import android.net.Uri
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.profile.PostsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
/**
 * This object contains all the functionalities responsible for retrieving and storing user content
 * (e.g images) to Firebase Storage
 *
 * The author implemented an object to create a singleton of the database, that can be accessed
 * anywhere in the code
 *
 * Parts of this code in this object was adapted from Denis Panjuta's code as it taught
 * the author how to integrate firebase into the application.
 *
 * @see com.example.studentpal.common.References
 */

object Storage {
    // Converts the date into a readable format
    private val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)

    /**
     * Method to upload images to Firebase storage
     */
    fun uploadToStorage(
        activity: PostsActivity,
        uri: Uri,
        user: User,
        imageCaption: String,
    ) {
        val postRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "POST_IMAGES" +
                    System.currentTimeMillis() + "." + Constants.getFileExtension(
                activity,
                uri
            )
        )
        postRef.child(user.id)
            .putFile(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    postRef.child(user.id).downloadUrl.addOnSuccessListener {
                        val date = Date()
                        val dateString = simpleDateFormat.format(date)
                        val postImageURL = it.toString()
                        val ref = FirebaseFirestore.getInstance().collection(Constants.POSTS)
                            .document()
                        val docID = ref.id
                        val imagePost = Post(
                            user.id,
                            postImageURL,
                            dateString,
                            imageCaption,
                            0,
                            docID
                        )
                        activity.hideProgressDialog()
                        PostsDatabase.uploadPost(activity, imagePost)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                activity.hideProgressDialog()
            }
    }
}