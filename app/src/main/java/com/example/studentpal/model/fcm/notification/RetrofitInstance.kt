package com.example.studentpal.model.fcm.notification

import com.example.studentpal.common.Constants

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
/**
 * This class initialises retrofit.
 *
 * This class was reused from Philipp Lackner's Source code (Lackner, 2021)
 *
 * See references file for source code link
 * @see[com.example.studentpal.common.References]
 */
class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(Constants.FCM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val api: NotificationAPI by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}