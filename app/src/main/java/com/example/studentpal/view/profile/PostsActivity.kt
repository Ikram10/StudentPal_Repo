package com.example.studentpal.view.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityPostsBinding
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.loadUserData
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.ImagePostsAdapter
import com.example.studentpal.viewmodel.PostsViewModel
import java.io.IOException

class PostsActivity : BaseActivity(), View.OnClickListener {
    // Global variables to handle adding posts
    private var binding: ActivityPostsBinding? = null
    private var postsAdapter: ImagePostsAdapter? = null // Adapter
    // Clickable views
    private var addImagePost: AppCompatImageView? = null
    private var uploadImagePost: AppCompatImageButton? = null
    private var etCaption: AppCompatEditText? = null
    // ViewModel
    private lateinit var viewModel : PostsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Initialise View Model
        viewModel = ViewModelProvider(this)[PostsViewModel::class.java]
        // Observer to posts list
        viewModel.posts.observe(this) {
            // Populates the UI with the posts list
            populatePostListToUI(it as ArrayList<Post>)
        }

        setupActionBar()

        loadUserData(this)
        // Initialise clickable views
        addImagePost = binding?.ibPostImage
        uploadImagePost = binding?.ibUploadImage
        etCaption = binding?.etPostImage
        // Set on click listeners
        addImagePost?.setOnClickListener(this)
        uploadImagePost?.setOnClickListener(this)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (viewModel.addImageBtnSelected) {
            if (data != null) {
                viewModel.mSelectedImagePostFileUri = data.data
            }
            try {
                Glide
                    .with(this)
                    .load(viewModel.mSelectedImagePostFileUri)
                    .centerCrop()
                    .into(addImagePost!!)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            !viewModel.addImageBtnSelected
        }
    }

    private fun populatePostListToUI(list: ArrayList<Post>) {
        hideProgressDialog()
        // Show the recyclerview if a post exists
        if (list.size > 0) {
            binding?.rvPosts?.visibility = View.VISIBLE
            binding?.llNoPosts?.visibility = View.GONE
            binding?.rvPosts?.layoutManager = LinearLayoutManager(this)
            binding?.rvPosts?.setHasFixedSize(true)

            postsAdapter = ImagePostsAdapter(this, list)
            binding?.rvPosts?.adapter = postsAdapter

        } else {
            // If there are no posts, show the no posts text view
            binding?.rvPosts?.visibility = View.GONE
            binding?.llNoPosts?.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarPosts)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        binding?.toolbarPosts?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            addImagePost -> {
                viewModel.addImageBtnSelected = true
                //The user will be prompted to grant permission to read files from devices media storage in order to upload a cover image image
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // if granted permission, show image chooser
                    Constants.showImageChooser(this)
                } else {
                    // prompt user to grant permission to media storage
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
            uploadImagePost -> {
                viewModel.addPost(this, etCaption!!)
            }
        }
    }

    fun setUserDataInUI(loggedInUser: User) {
        viewModel.mUserDetails = loggedInUser
    }
}