package com.example.studentpal.model.entities

/**
 * This data class holds the chat message data
 *
 * The code displayed was adapted from Brian Voong's "Kotlin Firebase Messenger" Tutorial (see references file)
 *
 * @see[com.example.studentpal.common.References]
 */

data class ChatMessage (
    val id: String,
    val fromId : String,
    val toId: String,
    val text : String,
    val timeStamp: Long,
    val status: String = "unseen"
    ) {
    constructor():
            this("",
                "",
                "",
                "",
                -1,
                "unseen")
}
