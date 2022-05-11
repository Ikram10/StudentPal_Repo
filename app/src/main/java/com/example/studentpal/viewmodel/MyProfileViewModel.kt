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
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.model.remote.UsersDatabase.updateUserProfileData
import com.example.studentpal.view.profile.MyProfileActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlin.collections.HashMap
import kotlin.collections.set

class MyProfileViewModel(application: Application) : AndroidViewModel(application) {

    // current user profile details
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User>
    get() = _currentUser

    // Variables stores the selected image URI  value
    var mSelectedImageFileUri: Uri? = null
    var mSelectedCoverImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private var mProfileCoverImageURL: String = ""
    var profileImgSelected: Boolean = false
    var profileCoverImgSelected: Boolean = false

    init {
        /*special CoroutineScope provided by Android
         * Firebase functions are defined as suspend functions, therefore can only be called
         * from within a coroutine scope or another suspend function.
         */
        viewModelScope.launch {
            _currentUser.value = fetchCurrentUser()!!
        }
    }


    fun uploadToStorage(activity: MyProfileActivity) {
        activity.showProgressDialog("Please Wait")
        if (mSelectedImageFileUri != null) {
            val sref: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity,
                    mSelectedImageFileUri
                )
            )

            sref.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                Log.i(
                    "Firebase Image URL",
                    it.metadata!!.reference!!.downloadUrl.toString()
                )
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
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
            val sref: StorageReference = FirebaseStorage.getInstance().reference.child(
                "COVER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity,
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

                    updateUserProfile(activity, currentUser.value?.name.toString(), currentUser.value?.status.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                activity.hideProgressDialog()
            }
        }



    }


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