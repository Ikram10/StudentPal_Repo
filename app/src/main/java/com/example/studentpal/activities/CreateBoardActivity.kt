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
import com.example.studentpal.databinding.ActivityCreateBoardBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    var binding: ActivityCreateBoardBinding? = null
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setupActionBar()

        //retrieves the intent extra by using the name key
        if (intent.hasExtra(Constants.NAME)) {
            //initialises this variable with the user's name that was passed with the intent
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        //handles the functionality when the user selects the board image
        binding?.ivBoardImage?.setOnClickListener {
            /* Application first checks if the user has granted permission to read files from the devices storage media
               before displaying the image chooser
             */
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
                /*If permission is denied, the user will be prompted to grant permission to read
                  files from devices media storage in order to upload a board image.
                 */
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        //handles the functionality when user selects the create button
        binding!!.btnCreate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    //method is responsible for creating and storing a board in cloud Firestore
    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        //board information that will be stored in firestore
        val board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignedUsersArrayList
        )

        //this function handles the creation of the board in cloud Firestore
        FirestoreClass().createBoard(this, board)

    }

    //method handles the uploading of board images to cloud storage securely
    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        //reference to firebase storage
        val sref: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" +
                    System.currentTimeMillis() + "." + Constants.getFileExtension(
                this,
                mSelectedImageFileUri
            )
        )
        //places the image file into Firebase storage
        sref.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
            /* logs the task snapshot to display where the image file is stored.
             * download URL is a publicly accessible URL that is used to access a file from Cloud Storage.
             * The download URL contains a download token which acts as a security measure to restrict access only to those who possess the token.
             */
            Log.i(
                "Board Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                Log.i(
                    "Downloadable Image URL", it.toString()
                )
                //stores the image uri in this variable
                mBoardImageUrl = it.toString()
                //only successful storage of board image will call this createBoard() method
                createBoard()
            }
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }


    //this function is called if the board is created and stored successfully in Firestore
    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        finish()
    }


    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                binding?.ivBoardImage?.let {
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

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
}