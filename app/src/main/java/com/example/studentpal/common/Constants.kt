package com.example.studentpal.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.studentpal.BuildConfig

object Constants {
    // user related Constants
    const val USER_KEY = "USER_KEY"
    const val USERS : String = "users"
    const val USERNAME: String = "username"
    const val USER_MESSAGES: String = "user-messages"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val STATUS: String = "status"
    const val ID : String = "id"
    const val COVER_IMAGE = "coverImage"
    const val NUMBER_FRIENDS = "numFriends"
    const val SENDER: String = "sender"
    const val RECEIVER: String = "receiver"
    const val POSTS: String = "posts"

    /**
     * GOOGLE MAPS API KEY : This needs to be filled
     */
    const val MAPS_API_KEY = BuildConfig.MAPS_API_KEY

    // Friend Requests related Constants
    const val FRIENDSHIPS = "friendships" // friendships collection
    const val FRIEND_REQUEST = "friend-requests" // friend-requests collection

    // Event related Constants
    const val EVENTS: String = "events"
    const val ASSIGNED_TO: String = "assignedTo"
    const val EVENT_DETAIL: String = "event_detail"
    const val CARD_COLOR: String = "cardColor"
    const val SELECT : String = "Select"
    const val UN_SELECT : String = "UnSelect"
    const val EVENT_DATE : String = "eventDate"
    const val CREATOR_ID : String = "creatorID"

    // Preferences
    const val STUDENTPAL_PREFERENCES = "StudentPalPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    // START
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com"
    const val CONTENT_TYPE = "application/json"

    /**
     * FCM SERVER KEY NEEDS TO BE FILLED
     */
    const val FCM_SERVER_KEY:String = BuildConfig.FCM_SERVER_KEY
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
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