package com.example.studentpal.fcm

import com.example.studentpal.models.PushNotification
import com.example.studentpal.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key= ${Constants.FCM_SERVER_KEY}", "Content_Type:${Constants.CONTENT_TYPE}")
    @POST("/fcm/send")
    suspend fun postNotification (
        @Body notification: PushNotification
    ): Response<ResponseBody>

}