package com.example.studentpal

import com.example.studentpal.model.entities.User

object testUserList {
    // List containing 3 users for testing purposes
    var userListTest =
        mutableListOf<User>(
           User(
               name = "Peter",
               email = "peter@gmail.com",
               username = "peter10"
           ),
            User(
                name = "Miles",
                email = "miles@gmail.com",
                username = "miles10"
            ),
            User(
                name = "Gwen",
                email = "gwen@gmail.com",
                username = "gwen10"
            )
        )
}