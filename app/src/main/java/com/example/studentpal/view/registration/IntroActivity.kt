package com.example.studentpal.view.registration

import android.content.Intent
import android.os.Bundle
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private var binding : ActivityIntroBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSignUp?.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding?.btnSignIn?.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding?.btnForgotPassword?.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }
    }
}