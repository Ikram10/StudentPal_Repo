package com.example.studentpal.model.entities

data class Post(
    var id: String,
    var image: String,
    var eventDate: String,
    var caption: String,
    var likes: Int,
    var docID: String
) {
    constructor() :
            this(
                "",
                "",
                "",
                "",
                0,
                ""
            )
}
