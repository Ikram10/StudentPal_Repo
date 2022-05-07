package com.example.studentpal.model.entities

data class PushNotification(
    val data: NotificationData,
    val to: String // recipient
)
