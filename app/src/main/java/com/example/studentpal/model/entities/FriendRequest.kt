package com.example.studentpal.model.entities


/**
 * [My Code ]: This data class holds all the information related to Friend Requests
 */
data class FriendRequest(
    val status: String,
    val sender: String,
    val receiver: String,
)
