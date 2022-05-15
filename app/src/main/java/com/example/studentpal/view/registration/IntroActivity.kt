package com.example.studentpal.view.registration

import android.content.Intent
import android.os.Bundle
import com.example.studentpal.databinding.ActivityIntroBinding
import com.example.studentpal.view.BaseActivity
/**
 * Intro Activity welcomes users, prompting them to sign in or sign up
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * Minute additions were made to accommodate the project requirements.
 *
 * For instance, a forgotten password feature was implemented
 *
 * @see[com.example.studentpal.common.References]
 */

class IntroActivity : BaseActivity() {

    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSignUp?.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding?.btnSignIn?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // My Code: Navigates user to the Reset password activity
        binding?.btnForgotPassword?.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }
    }
}