package com.example.studentpal.model.remote

import android.net.Uri
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.PostsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

object Storage {
  private val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)

  fun uploadToStorage(
   activity: PostsActivity,
   uri: Uri,
   user: User,
   imageCaption: String, ) {
   val postRef: StorageReference = FirebaseStorage.getInstance().reference.child(
    "POST_IMAGES" +
            System.currentTimeMillis() + "." + Constants.getFileExtension(
     activity,
     uri
    )
   )

   postRef.child(user.id)
    .putFile(uri)
    .addOnCompleteListener {
     if (it.isSuccessful) {
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
    .addOnFailureListener { exception ->
     Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
     activity.hideProgressDialog()
    }
  }
}