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





    //user related Constants
    const val USER_KEY = "USER_KEY"
    const val USERS : String = "Users"
    const val MESSAGES: String = "Messages"
    const val USER_MESSAGES: String = "User-Messages"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val STATUS: String = "status"
    const val EMAIL: String = "email"
    const val ID : String = "id"
    const val COVER_IMAGE = "coverImage"
    const val NUMBER_FRIENDS = "numFriends"
    const val SENDER: String = "sender"
    const val RECEIVER: String = "receiver"


    // Friend Requests
    const val FRIENDSHIPS = "Friendships" // friendships collection
    const val FRIEND_REQUEST = "Friend-Requests"

    //Event related Constants
    const val BOARDS: String = "boards"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID : String = "documentId"
    const val BOARD_DETAIL: String = "board_detail"
    const val CARD_COLOR: String = "cardColor"
    const val SELECT : String = "Select"
    const val UN_SELECT : String = "UnSelect"

    const val STUDENTPAL_PREFERENCES = "StudentPalPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"


    // START
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAA5KLpOgc:APA91bEriQauq41gnZxAUt42ZxVU7c_cGzegCqd74Krc0038V2bLd9OJnmpSF0FhIc7DXTYEyZ1db_FKimnpspwNr3gA1Mj3ENbiQqP00X_UrCFUHppCoS8Eskh_gASSx9zjEWR2h0cX"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"
    // END



    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    /**
     * The functions below are placed in the Constant file
       so they can be reused throughout the application within several activities
     */

    //allows app to open the users media storage to select an image
    //requires an activity as a parameter to know which activity to startActivityForResult
    fun showImageChooser (activity: Activity){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /* Converts the uri retrieved from image choose into a string
     * requires an activity as a parameter to know which activity to return the file extension to
     */
     fun getFileExtension(activity: Activity, uri : Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}