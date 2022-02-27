package com.example.studentpal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.studentpal.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private var binding : ActivitySignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()



    }

    //activates the back button and implements its functionality
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarSignInActivity?.setNavigationOnClickListener{
            onBackPressed()
        }
    }
}