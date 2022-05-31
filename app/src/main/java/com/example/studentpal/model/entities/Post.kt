package com.example.studentpal.model.entities
/**
 * [My Code ]: This data class holds all the information related to posts
 */
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
