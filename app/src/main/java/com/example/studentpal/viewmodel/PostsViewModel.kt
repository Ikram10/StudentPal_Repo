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
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.view.profile.MyProfileActivity
import com.example.studentpal.view.profile.PostsActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
/**
 * This class is responsible for executing [MyProfileActivity] business logic
 *
 *  Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * The entire code in this class belongs to the author.
 *
 */

class PostsViewModel: ViewModel() {

    private lateinit var mUserDetails: User
    // Posts List
    private val _postsList = MutableLiveData<List<Post>>()
    // Public Posts list getter
    var posts: LiveData<List<Post>> = _postsList

    var addImageBtnSelected: Boolean = false

    var mSelectedImagePostFileUri: Uri? = null

    init {
        // Initialises the user and posts list in a Coroutine
        viewModelScope.launch {
            mUserDetails = fetchCurrentUser()!!
            _postsList.value = PostsDatabase.getPosts()
        }
    }

    /**
     * Method checks if an image and caption has been added before uploading to storage
     *
     * @param etCaption the caption edit text field
     *
     * @see com.example.studentpal.model.remote.Storage
     */
    fun addPost(activity: PostsActivity, etCaption: AppCompatEditText) {
        val imageCaption: String = etCaption.text.toString()

        when {
            imageCaption.isEmpty() -> {
                etCaption.error = "Please enter a caption for your post"
            }
            // No image has been selected, resulting in the uri field to be null
            mSelectedImagePostFileUri == null -> {
                activity.showErrorSnackBar("Please select an image to post")
            }
            else -> {
                activity.showProgressDialog("Please Wait")

                viewModelScope.launch {
                    uploadToStorage(activity, mSelectedImagePostFileUri!!, mUserDetails, imageCaption)
                    delay(1500) // delay to allow storage upload to execute first
                    _postsList.value = PostsDatabase.getPosts() // set post lists with new post list
                    Log.d(TAG, "Number of posts = ${_postsList.value?.size}")
                }
            }
        }
    }

    companion object {
        const val TAG = "PostsViewModel"
    }
}
