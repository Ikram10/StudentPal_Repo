package com.example.studentpal.models

data class ChatMessage (
    val id: String,
    val fromId : String,
    val toId: String,
    val text : String,
    val timeStamp: Long
    ) {

    constructor():
            this("",
                "",
                "",
                "",
                -1)
}