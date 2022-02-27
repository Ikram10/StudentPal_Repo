package com.example.studentpal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.studentpal.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

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
    }
}