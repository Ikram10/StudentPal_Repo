package com.example.studentpal.activities

import com.example.studentpal.activities.registration.SignUpActivity
import org.junit.Before

class SignUpActivityTest {
    private lateinit var signUpActivity: SignUpActivity

    // runs the set up function before any test case
    @Before
    fun setUp(){

        signUpActivity = SignUpActivity()
    }
}
