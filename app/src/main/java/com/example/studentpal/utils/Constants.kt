package com.example.studentpal.utils

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.studentpal.activities.MyProfileActivity

object Constants {

    const val USERS : String = "Users"
    const val BOARDS: String = "boards"
    const val MESSAGES: String = "Messages"
    const val USER_MESSAGES: String = "User-Messages"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val STATUS: String = "status"
    const val EMAIL: String = "email"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    /**
     * The functions below are placed in the Constant file
       so they can be reused throughout the application within several activities
     */

    //allows app to open the users media storage to select an image
    //requires an activity as a parameter to know which activity to the startActivityForResult
    fun showImageChooser (activity: Activity){
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    //requires an activity as a parameter to know which activity to return the file extension to
     fun getFileExtension(activity: Activity, uri : Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}