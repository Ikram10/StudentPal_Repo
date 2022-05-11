package com.example.studentpal.viewmodel

import android.net.Uri
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.PostsDatabase
import com.example.studentpal.model.remote.Storage.uploadToStorage
import com.example.studentpal.model.remote.UsersDatabase
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.view.profile.PostsActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostsViewModel: ViewModel() {

    val TAG = "PostsViewModel"
    lateinit var mUserDetails: User
    private val _postsList = MutableLiveData<List<Post>>() // Posts List
    var posts: LiveData<List<Post>> = _postsList // Posts list getter

    var addImageBtnSelected: Boolean = false
    var mSelectedImagePostFileUri: Uri? = null

    init {
        // Initialises the user and posts list in a Coroutine
        viewModelScope.launch {
            mUserDetails = fetchCurrentUser()!!
            _postsList.value = PostsDatabase.getPosts()
        }
    }

    /* Checks if an image has been added
     * Checks if a caption has been added before uploading to storage
     */
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
                viewModelScope.launch {
                    uploadToStorage(activity, mSelectedImagePostFileUri!!, mUserDetails, imageCaption)
                    delay(1200)
                    _postsList.value = PostsDatabase.getPosts()
                    Log.d(TAG, "Number of posts = ${_postsList.value?.size}")
                }
            }
        }
    }
}
