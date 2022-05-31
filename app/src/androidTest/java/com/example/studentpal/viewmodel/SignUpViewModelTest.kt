package com.example.studentpal.viewmodel

import com.example.studentpal.model.entities.User
import com.example.studentpal.testUserList
import com.example.studentpal.view.registration.SignUpActivity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@Suppress("IllegalIdentifier")
class SignUpViewModelTest {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var userList: MutableList<User>
    private lateinit var user1: User
    private lateinit var user2: User

    @Before
    fun setup() {
        viewModel = SignUpViewModel()
        userList = testUserList.userListTest
        // New account
        user1 = User(
            name = "Steve",
            username = "steve10",
            email = "steve@gmail.com"
        )
        // Existing Account
        user2 = User(
            name = "Miles",
            username = "miles10",
            email = "miles@gmail.com"
        )
    }

    @Test
    fun `is username unique - returns true`() = runBlocking {
       val result =  viewModel.signUpUser(SignUpActivity(), user1.name, user1.email, "", user1.username)

        result
    }
}