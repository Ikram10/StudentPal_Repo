package com.example.studentpal.model.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.studentpal.R
import com.example.studentpal.view.events.MainActivity
import com.example.studentpal.view.registration.SignInActivity
import com.example.studentpal.common.Constants
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
/**
 * This class is a base class for receiving messages from Firebase Cloud Messaging (FCM).
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (Panjuta,2021)
 * However, the author evolved the code produced by Panjuta to accommodate the project requirements. (see references file)
 *
 * All code that was created by the author will be labelled [My Code].
 *
 * @see[com.example.studentpal.common.References]
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    /**
     * This method is called when the FCM token is updated. This may occur if the previous token had been compromised.
     * Note that this method is called when the FCM token is initially generated.
     */
    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")

        /* If you want to send messages to this application instance or send the
        *token to your app server.
        */
        sendRegistrationToServer(token)
    }

    /**
     * Method persists token to third-party servers
     * @param token the new token
     */
    private fun sendRegistrationToServer(token: String?) {
        // Here we have saved the token in the Shared Preferences
        val sharedPreferences =
            this.getSharedPreferences(Constants.STUDENTPAL_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    /**
     * Method is called when a message is received
     * @param message object representing the message received from Firebase Cloud Messaging
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "FROM: ${message.from}")

        // Check if message contains a data payload
        message.data.isNotEmpty().let {
            // notification data payload is printed in the log
            Log.d(TAG, "Message data Payload: ${message.data}")

            // title and message of the data payload assigned to local variable
            val title = message.data[Constants.FCM_KEY_TITLE]
            val sMessage = message.data[Constants.FCM_KEY_MESSAGE]

            //pass the data to build a notification
            sendNotification(title!!, sMessage!!)
        }
        //check if message contains a notification payload
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    /**
     * Method creates and displays a simple notification containing the received FCM message.
     * @param message FCM message received
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun sendNotification(title: String, message: String) {
        // User navigated into the app if logged in when clicking the notification
        val intent = if (getCurrentUserId().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, SignInActivity::class.java)
        }
        // Before launching the screen add some flags to avoid duplication of activities.
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE)

        val channelId = this.resources.getString(R.string.default_notification_channel_id)

        val defaultSoundUri = RingtoneManager
            .getDefaultUri(RingtoneManager
                .TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(
            this, channelId
        )
            .setSmallIcon(R.drawable.student_icon)
            //Set the title and message for the notification which will be visible in the notification tray
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notificationID = Random.nextInt()

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel StudentPal Title",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.BLUE
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationID, notificationBuilder)
    }


}