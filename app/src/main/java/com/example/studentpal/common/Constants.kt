package com.example.studentpal.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.studentpal.BuildConfig

/**
 * This Object contains all the constant that will be required throughout the application.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 * However, the author significantly evolved the code to accommodate the project requirements.
 *
 * All code that was created by the author will be labelled [My Code].
 *
 *
 * @see[com.example.studentpal.common.References]
 */
object Constants {

    const val USER_KEY = "USER_KEY"

    /***************** Firebase Constants **********************/
    //START [MY CODE]
    const val USERS : String = "users" // users collection
    const val FRIENDSHIPS = "friendships" // friendships collection
    const val FRIEND_REQUEST = "friend-requests" // friend-requests collection
    const val USER_MESSAGES: String = "user-messages" // user-messages collection
    const val POSTS: String = "posts" // posts collection
    const val EVENTS: String = "events" // events collection
    // Event collection fields [MY CODE]
    const val ASSIGNED_TO: String = "assignedTo"
    const val EVENT_DETAIL: String = "event_detail"
    const val CARD_COLOR: String = "cardColor"
    const val SELECT : String = "Select"
    const val UN_SELECT : String = "UnSelect"
    const val EVENT_DATE : String = "eventDate"
    const val CREATOR_ID : String = "creatorID"
    // user Collection fields [My Code]
    const val USERNAME: String = "username"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val STATUS: String = "status"
    const val ID : String = "id"
    const val COVER_IMAGE = "coverImage"
    const val NUMBER_FRIENDS = "numFriends"
    //Friendship collection fields [MY CODE]
    const val SENDER: String = "sender"
    const val RECEIVER: String = "receiver"
    //FCM constants (Lackner, 2021)
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com"
    const val CONTENT_TYPE = "application/json"
    // Preferences (Lackner, 2021)
    const val STUDENTPAL_PREFERENCES = "StudentPalPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"
    /**
     * FCM SERVER KEY NEEDS TO BE FILLED
     *
     * Check project report for set up instructions
     */
    const val FCM_SERVER_KEY:String = BuildConfig.FCM_SERVER_KEY
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"

    //END
    /**
     * GOOGLE MAPS API KEY : This needs to be filled
     *
     * Check project report for set up instructions
     */
    const val MAPS_API_KEY = BuildConfig.MAPS_API_KEY

    //permission request codes (Panjuta, 2021)
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    /**
     * The functions below are placed in the Constant file
       so they can be reused throughout the application within several activities (Panjuta,2021)
     */
    //allows app to open the users media storage to select an image
    //requires an activity as a parameter to know which activity to startActivityForResult
    fun showImageChooser (activity: Activity){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }
    /** Converts the uri retrieved from image choose into a string
     * requires an activity as a parameter to know which activity to return the file extension to (Panjuta,2021)
     */
     fun getFileExtension(activity: Activity, uri : Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}