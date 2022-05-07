package com.example.studentpal.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.view.adapter.ImagePostsAdapter
import com.example.studentpal.databinding.ActivityPostsBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.model.entities.ImagePost
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PostsActivity : BaseActivity(), View.OnClickListener {
    // Global variables to handle adding posts
    private var binding: ActivityPostsBinding? = null
    private var postsList: ArrayList<ImagePost>? = null
    private var postsAdapter: ImagePostsAdapter? = null // Adapter
    private var addImageBtnSelected: Boolean = false
    private var mSelectedImagePostFileUri: Uri? = null
    private var mPostImageURL: String = ""
    private val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    // Clickable views
    private var addImagePost: AppCompatImageView? = null
    private var uploadImagePost: AppCompatImageButton? = null
    private var etCaption: AppCompatEditText? = null

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)
        // Initialise clickable views
        addImagePost = binding?.ibPostImage
        uploadImagePost = binding?.ibUploadImage
        etCaption = binding?.etPostImage
        // Set on click listeners
        addImagePost?.setOnClickListener(this)
        uploadImagePost?.setOnClickListener(this)

        FirestoreClass().getImagePostsList(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (addImageBtnSelected) {
            if (data != null) {
                mSelectedImagePostFileUri = data.data
            }
            try {
                Glide
                    .with(this)
                    .load(mSelectedImagePostFileUri)
                    .centerCrop()
                    .into(addImagePost!!)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            !addImageBtnSelected
        }
    }

    fun populatePostListToUI(list: ArrayList<ImagePost>) {
        hideProgressDialog()
        postsList = list

        // Show the recyclerview if a post exists
        if (postsList?.size!! > 0) {
            binding?.rvPosts?.visibility = View.VISIBLE
            binding?.llNoPosts?.visibility = View.GONE
            binding?.rvPosts?.layoutManager = LinearLayoutManager(this)
            binding?.rvPosts?.setHasFixedSize(true)

            postsAdapter = ImagePostsAdapter(this, postsList!!)
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

    private fun addPost() {
        // Image caption for post
        val imageCaption: String = etCaption?.text.toString()

        when {
            imageCaption.isEmpty() -> {
                etCaption?.error = "Please enter a caption for your post"
            }
            mSelectedImagePostFileUri == null -> {
                showErrorSnackBar("Please select an image to post")
            }
            else -> {
                showProgressDialog(resources.getString(R.string.please_wait))
                uploadToStorage()
            }
        }
    }

    private fun uploadToStorage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImagePostFileUri != null) {
            val postRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "POST_IMAGES" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedImagePostFileUri
                )
            )
            postRef.child(mUserDetails.id)
                .putFile(mSelectedImagePostFileUri!!)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        postRef.child(mUserDetails.id).downloadUrl.addOnSuccessListener {
                            val date = Date()
                            val dateString = simpleDateFormat.format(date)
                            mPostImageURL = it.toString()
                            val ref = FirebaseFirestore.getInstance().collection(Constants.POSTS)
                                .document()
                            val docID = ref.id
                            val imagePost = ImagePost(
                                getCurrentUserID(),
                                mPostImageURL,
                                dateString,
                                etCaption?.text.toString(),
                                0,
                                docID
                            )
                            hideProgressDialog()
                            FirestoreClass().uploadPost(this, imagePost)
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            addImagePost -> {
                addImageBtnSelected = true
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
                addPost()
            }
        }
    }

    fun setUserDataInUI(loggedInUser: User) {
        mUserDetails = loggedInUser
    }
}