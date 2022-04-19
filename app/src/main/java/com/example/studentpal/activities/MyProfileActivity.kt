package com.example.studentpal.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityMyProfileBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MyProfileActivity : BaseActivity() {

    //variable stores the selected image URI  value
    private var mSelectedImageFileUri: Uri? = null
    private var mSelectedCoverImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private var mProfileCoverImageURL: String = ""
    private lateinit var mUserDetails: User
    private val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    private var profileImgSelected: Boolean = false
    private var profileCoverImgSelected: Boolean = false


    private var binding: ActivityMyProfileBinding? = null

    private val toolbar = binding?.toolbarMyProfile
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)
        mAuth = FirebaseAuth.getInstance()

        //The user will be able to select the profile image
        binding?.ivProfileUserImage?.setOnClickListener {
            profileImgSelected = true
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
        binding?.btnAddCoverImage?.setOnClickListener {
            // when button clicked set this to true
            profileCoverImgSelected = true
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
        binding?.btnUpdate?.setOnClickListener {
            // CheckS if an Image uri exists before uploading the user image
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
            // Checks if a Cover image uri exists before uploading cover image
            if (mSelectedCoverImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            if (profileImgSelected) {
                mSelectedImageFileUri = data.data
                try {

                    binding?.ivProfileUserImage?.let {
                        Glide
                            .with(this)
                            .load(mSelectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.ic_nav_user)
                            .into(it)

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                !profileImgSelected
            }
            if (profileCoverImgSelected) {
                mSelectedCoverImageFileUri = data.data
                try {
                    binding?.ciMyProfile?.let {
                        Glide
                            .with(this)
                            .load(mSelectedCoverImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.add_screen_image_placeholder)
                            .into(it)

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                !profileCoverImgSelected
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
        Log.d("MyProfileActivity", "onBackPressed");
        Toast.makeText(this, "onBackPressed", Toast.LENGTH_SHORT).show();
    }

    fun getDateString(time: Long): String = simpleDateFormat.format(time * 1000)

    fun setUserDataInUI(user: User) {
        mUserDetails = user

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

            // My profile Cover image
            Glide
                .with(this)
                .load(user.coverImage)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
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

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val sref: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedImageFileUri
                )
            )

            sref.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i(
                        "Downloadable Image URL", uri.toString()
                    )
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
         if (mSelectedCoverImageFileUri != null) {
            val sref: StorageReference = FirebaseStorage.getInstance().reference.child(
                "COVER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedCoverImageFileUri
                )
            )
            sref.putFile(mSelectedCoverImageFileUri!!).addOnSuccessListener {
                Log.i(
                    "Firebase Cover Img URL",
                    it.metadata!!.reference!!.downloadUrl.toString()
                )
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    Log.i(
                        "Downloadable Image URL", it.toString()
                    )
                    mProfileCoverImageURL = it.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }


    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }

        if (mProfileCoverImageURL.isNotEmpty() && mProfileCoverImageURL != mUserDetails.coverImage) {
            userHashMap[Constants.COVER_IMAGE] = mProfileCoverImageURL
            anyChangesMade = true
        }

        if (binding?.etName?.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            anyChangesMade = true
        }

        if (binding?.etStatus?.text.toString() != mUserDetails.status) {
            userHashMap[Constants.STATUS] = binding?.etStatus?.text.toString()
            anyChangesMade = true
        }

        if (anyChangesMade) {
            FirestoreClass().updateUserProfileData(this, userHashMap)
        } else {
            hideProgressDialog()
        }
    }


    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

}



