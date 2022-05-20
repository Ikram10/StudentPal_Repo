package com.example.studentpal.view.profile

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
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.viewmodel.MyProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

/**
 * This activity is responsible for displaying and modifying the users profile
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 * However, the author significantly evolved the code produced by Panjuta to accommodate the project requirements.
 *
 * For instance, this project integrated the MVVM design pattern, resulting in structural differences and code additions.
 *
 * All code that was created by the author will be labelled [My Code].
 *
 * Reused code that has been adapted by the author is labeled [Adapted ].
 *
 * @see[com.example.studentpal.common.References]
 * @see viewModel
 */

@Suppress("DEPRECATION")
class MyProfileActivity : BaseActivity(), View.OnClickListener {

    private var binding: ActivityMyProfileBinding? = null
    private val toolbar = binding?.toolbarMyProfile
    // Buttons
    private var ivProfileImage: CircleImageView? = null
    private var btnAddCoverImage: AppCompatImageButton? = null
    private var btnUpdate: AppCompatButton? = null
    // ViewModel
    private lateinit var viewModel: MyProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // My Code: Initialise ViewModel
        viewModel = ViewModelProvider(this)[MyProfileViewModel::class.java]

        // My Code: observes changes made to current users profile
        viewModel.currentUser.observe(this) {
            //This method is executed whenever the user profile changes
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

    /**
     * [My Code]: The is method handles all the functionalities that occur
     * when a user clicks a view.
     *
     * A separate method was created, because the code was verbose and
     * the author wanted to minimise the logic displayed in the [onCreate]
     */
    override fun onClick(v: View) {
        when (v) {
            // Profile image is selectable
            ivProfileImage -> {
                //My Code: Distinguishes between the buttons that are selected.
                //If the profile image is selected, this variable is set to true.
                viewModel.profileImgSelected = true
                //The user will be prompted to grant permission to read files from devices media storage
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Displays users image gallery
                    Constants.showImageChooser(this)
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
            //My Code: Reused the same functionality produced by Panjuta to add a cover image
            btnAddCoverImage -> {

                //My Code: Distinguishes between the buttons that are selected.
                //If the cover image is selected, this variable is set to true.
                viewModel.profileCoverImgSelected = true

                //The user will be prompted to grant permission to read files from devices media storage
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // if permission granted, show image chooser
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
                    viewModel.updateUserProfile(this, name, status)
                }

            }
        }
    }

    /**
     * This method retrieves the data from the image chooser
     * and populates the view with the image.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {

            //My Code: If statement added to know where to load the image data
            if (viewModel.profileImgSelected) {

                viewModel.mSelectedImageFileUri = data.data

                try {
                    //If profile image is selected, loads image into the profile image
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
                // reset to default after loading
                !viewModel.profileImgSelected
            }

            //My Code: If statement added to know where to load the image data
            if (viewModel.profileCoverImgSelected) {

                viewModel.mSelectedCoverImageFileUri = data.data

                try {

                    //If cover image is selected, loads image into the cover image
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
                // reset to default after loading
                !viewModel.profileCoverImgSelected
            }
        }
    }

    /**
     * This method will identify the result of runtime permission after the user allows or denies permission
     * based on the unique code
     *
     * @param requestCode predefined codes allowing the system to distinguish between requests. See [Constants] file
     * @param permissions array list of defined permissions needed
     * @param grantResults result of permission request
     */
    @Deprecated("Deprecated in Java")
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
    /**
     * [Adapted ]: A method to set the existing users details in UI.
     *
     * This method is called whenever the users data is changed to
     * update UI and display the changes
     *
     * @param user the current user's information
     *
     * @see [MyProfileViewModel.currentUser]
     */
    fun setUserDataInUI(user: User) {
        binding?.let {
            // Uses Glide library to load an image into users profile image
            Glide
                .with(this)
                .load(user.image)
                .circleCrop()
                .placeholder(R.drawable.ic_nav_user)
                .into(it.ivProfileUserImage)

            //My Code: Sets each view with the appropriate user data
            it.etName.setText(user.name)
            it.etEmail.setText(user.email)
            it.etStatus.setText(user.status)
            it.profileName.text = user.name
            it.profileUsername.text = user.username
            it.civStatus.text = user.status
            it.dateNum.text = user.dateJoined
            it.friendsNum.text = user.numFriends.toString()

            // My Code: Reused the Glide library to load an image into users cover image
            Glide
                .with(this)
                .load(user.coverImage)
                .centerCrop()
                .into(it.ciMyProfile)

            //My code: Sets the text colour of users status depending on the Status
            when (user.status) {
                "Available" -> {
                    // Change colour green when status message is Available
                    binding!!.civStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.available
                        )
                    )
                }
                // Change colour red when status message is Available
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

    /**
     * A method to notify the user profile is updated successfully
     */
    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_LONG).show()
        finish()
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
}










