package com.example.studentpal.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityMyProfileBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.loadUserData
import com.example.studentpal.viewmodel.MyProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

//My code
class MyProfileActivity : BaseActivity(), View.OnClickListener {

    private var binding: ActivityMyProfileBinding? = null
    private val toolbar = binding?.toolbarMyProfile
    // Buttons
    private var ivProfileImage: CircleImageView? = null
    private var btnAddCoverImage: AppCompatImageButton? = null
    private var btnUpdate: AppCompatButton? = null
    // ViewModel
    private lateinit var viewModel : MyProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Initialise ViewModel
        viewModel = ViewModelProvider(this)[MyProfileViewModel::class.java]

        // User Observer
        viewModel.userProfile.observe(this) {
            setUserDataInUI(it)
        }

        setupActionBar()

        // Initialise buttons
        ivProfileImage = binding?.ivProfileUserImage
        btnAddCoverImage = binding?.btnAddCoverImage
        btnUpdate = binding?.btnUpdate
        // Set on click listeners
        ivProfileImage?.setOnClickListener(this)
        btnAddCoverImage?.setOnClickListener(this)
        btnUpdate?.setOnClickListener(this)


        loadUserData(this)
    }
    //My code:
    override fun onClick(v: View) {
        when (v) {
            ivProfileImage -> {
                viewModel.profileImgSelected = true
                //The user will be prompted to grant permission to read files from devices media storage in order to upload a profile image
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Constants.showImageChooser(this)
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
            //My Code: Enables users to add a cover image
            btnAddCoverImage -> {
                // when button clicked set this to true
                viewModel.profileCoverImgSelected = true
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

            //The user will be able to click on the update button
            btnUpdate -> {
                val name = binding?.etName?.text.toString()
                val status = binding?.etStatus?.text.toString()
                // Checks if an Image uri exists before uploading the user image
                if (viewModel.mSelectedImageFileUri != null) {
                    viewModel.uploadToStorage(this)
                } else {
                    showProgressDialog("Please Wait")
                    viewModel.updateUserProfile(this, name, status)
                }
                // Check if a Cover image uri exists before uploading cover image
                if (viewModel.mSelectedCoverImageFileUri != null) {
                    viewModel.uploadToStorage(this)
                } else {
                    showProgressDialog("Please Wait")
                    viewModel.updateUserProfile(this,name, status)
                }

            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            if (viewModel.profileImgSelected) {
                viewModel.mSelectedImageFileUri = data.data
                try {
                    binding?.ivProfileUserImage?.let {
                        Glide
                            .with(this)
                            .load(viewModel.mSelectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.ic_nav_user)
                            .into(it)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                !viewModel.profileImgSelected
            }
            if (viewModel.profileCoverImgSelected) {
                viewModel.mSelectedCoverImageFileUri = data.data
                try {
                    binding?.ciMyProfile?.let {
                        Glide
                            .with(this)
                            .load(viewModel.mSelectedCoverImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.add_screen_image_placeholder)
                            .into(it)

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                !viewModel.profileCoverImgSelected
            }
        }
    }
    //Checks for the specified request code permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "You just denied access permission to storage. You can allow it in the settings",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarMyProfile)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        Log.d("MyProfileActivity", "onBackPressed")
        Toast.makeText(this, "onBackPressed", Toast.LENGTH_SHORT).show()
    }


    fun setUserDataInUI(user: User) {
        binding?.let {

            // My profile image
            Glide
                .with(this)
                .load(user.image)
                .circleCrop()
                .placeholder(R.drawable.ic_nav_user)
                .into(it.ivProfileUserImage)

            it.etName.setText(user.name)
            it.etEmail.setText(user.email)
            it.etStatus.setText(user.status)
            it.profileName.text = user.name
            it.civStatus.text = user.status
            it.dateNum.text = user.dateJoined //Sets the date joined text in user's profile card
            it.friendsNum.text = user.numFriends.toString()

            // My profile Cover image
            Glide
                .with(this)
                .load(user.coverImage)
                .centerCrop()
                .into(it.ciMyProfile)


            //My code: Sets the text colour of users status depending on the Status
            when (user.status) {
                "Available" -> {
                    binding!!.civStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.available
                        )
                    )
                }
                "Unavailable" -> {
                    binding!!.civStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.unavailable
                        )
                    )
                }
            }
        }

    }

    fun profileUpdateSuccess() {
        setResult(Activity.RESULT_OK)
        hideProgressDialog()
        finish()
    }
}









