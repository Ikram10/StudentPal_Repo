package com.example.studentpal.model.fcm.notification

/**
 * Data class for the push notification data.
 *
 * This class was reused from Philipp Lackner's Source code (Lackner, 2021)
 *
 * See references file for source code link
 *
 * @param data the notification data including the title and message
 * @param to the recipients FCM token, so Firebase knows which device to send notification
 *
 * @see[com.example.studentpal.common.References]
 */
data class PushNotification(
    val data: NotificationData,
    val to: String //recipient token
)
