package com.example.studentpal.model.fcm.notification

import com.example.studentpal.common.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * interface contains function declarations for handling Notification feature.
 *
 * This class was reused from Philipp Lackner's Source code (Lackner, 2021)
 *
 * See references file for source code link
 * @see[com.example.studentpal.common.References]
 */
interface NotificationAPI {
    /**
     * Function responsible for sending notification data to the FCM server.
     * Annotations on this method indicate how a request will be handled.
     */
    @Headers("Authorization: key= ${Constants.FCM_SERVER_KEY}",
        "Content_Type:${Constants.CONTENT_TYPE}")
    @POST("/fcm/send")
    suspend fun postNotification (
        @Body notification: PushNotification
    ): Response<ResponseBody>

}