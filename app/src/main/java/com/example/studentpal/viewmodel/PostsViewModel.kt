package com.example.studentpal.viewmodel

import android.net.Uri
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.Storage.uploadToStorage
import com.example.studentpal.model.remote.UsersDatabase
import com.example.studentpal.view.PostsActivity
import kotlinx.coroutines.launch

class PostsViewModel: ViewModel() {

    lateinit var mUserDetails: User
    var postsList: ArrayList<Post>? = null
    var addImageBtnSelected: Boolean = false
    var mSelectedImagePostFileUri: Uri? = null

    init {
        viewModelScope.launch {
            mUserDetails = UsersDatabase.fetchCurrentUser(UsersDatabase.getCurrentUserId())!!
        }
    }

    fun addPost(activity: PostsActivity, etCaption: AppCompatEditText) {
        // Image caption for post
        val imageCaption: String = etCaption.text.toString()
        when {
            imageCaption.isEmpty() -> {
                etCaption.error = "Please enter a caption for your post"
            }
            mSelectedImagePostFileUri == null -> {
                activity.showErrorSnackBar("Please select an image to post")
            }
            else -> {
                activity.showProgressDialog("Please Wait")
                uploadToStorage(activity, mSelectedImagePostFileUri!!, mUserDetails, imageCaption)
            }
        }
    }
}
