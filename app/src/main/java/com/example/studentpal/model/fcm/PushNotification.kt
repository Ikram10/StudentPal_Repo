package com.example.studentpal.model.fcm

data class PushNotification(
    val data: NotificationData,
    val to: String //recipient
)
