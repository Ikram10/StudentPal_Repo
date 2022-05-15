package com.example.studentpal.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.fetchCurrentUser
import com.example.studentpal.model.remote.UsersDatabase.updateUserProfileData
import com.example.studentpal.view.profile.MyProfileActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlin.collections.set

/**
 * This class is responsible for executing [MyProfileActivity] business logic
 *
 * The methods displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However alterations were made to the code to suit the project's requirements.
 *
 * For instance, Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * All code that was created by the author is labeled with [My Code].
 *
 * @see[com.example.studentpal.common.References]
 */

class MyProfileViewModel(application: Application) : AndroidViewModel(application) {

    // current user profile details
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User>
    get() = _currentUser // public getter

    // Variables store the selected image URI values
    var mSelectedImageFileUri: Uri? = null
    var mSelectedCoverImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private var mProfileCoverImageURL: String = ""
    var profileImgSelected: Boolean = false
    var profileCoverImgSelected: Boolean = false

    /**
     *  [My Code]: Initialise currentUser whenenver this ViewModel class is created
     */
    init {
        viewModelScope.launch {
            // Retrieves current user information from the database
            _currentUser.value = fetchCurrentUser()!!
        }
    }

    /**
     * [Adapted ]: Uploads the selected loaded image to Firebase cloud storage
     *
     * This method was adapted from Panjuta's code, to implement Cover image upload.
     * It reuses the same implementation for uploading a profile image
     */
    fun uploadToStorage(activity: MyProfileActivity) {
        activity.showProgressDialog("Please Wait")
        if (mSelectedImageFileUri != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity,
                    mSelectedImageFileUri
                )
            )
            storageRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { task ->
                Log.i(
                    "Firebase Image URL",
                    task.metadata!!.reference!!.downloadUrl.toString()
                )
                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    Log.i(
                        "Downloadable Image URL", it.toString()
                    )
                    mProfileImageURL = it.toString()

                    updateUserProfile(activity,  currentUser.value?.name.toString(), currentUser.value?.status.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
                activity.hideProgressDialog()
            }
        }
        if (mSelectedCoverImageFileUri != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "COVER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity,
                    mSelectedCoverImageFileUri
                )
            )
            storageRef.putFile(mSelectedCoverImageFileUri!!).addOnSuccessListener { task ->
                Log.i(
                    "Firebase Cover Img URL",
                    task.metadata!!.reference!!.downloadUrl.toString()
                )
                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    Log.i(
                        "Downloadable Image URL", it.toString()
                    )
                    mProfileCoverImageURL = it.toString()

                    updateUserProfile(activity, currentUser.value?.name.toString(), currentUser.value?.status.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                activity.hideProgressDialog()
            }
        }



    }

    /**
     * [Adapted ]: A method to update user profile details in the database
     *
     * This method was adapted from Panjuta's to code add the update cover image feature
     *
     * @param userName the modifed name entered in the Name edit text field
     * @param userStatus the modifed status entered in the Status edit text field
     */
    fun updateUserProfile(activity: MyProfileActivity, userName: String, userStatus : String) {

        val userHashMap = HashMap<String, Any>()

        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != _currentUser.value?.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }

        if (mProfileCoverImageURL.isNotEmpty() && mProfileCoverImageURL != _currentUser.value?.coverImage) {
            userHashMap[Constants.COVER_IMAGE] = mProfileCoverImageURL
            anyChangesMade = true
        }

        if (userName != _currentUser.value?.name) {
            userHashMap[Constants.NAME] = userName
            anyChangesMade = true
        }

        if (userStatus != _currentUser.value?.status) {
            userHashMap[Constants.STATUS] = userStatus
            anyChangesMade = true
        }
        if (anyChangesMade) {
            updateUserProfileData(activity, userHashMap)
        }
        else {
            activity.hideProgressDialog()
        }
    }



}