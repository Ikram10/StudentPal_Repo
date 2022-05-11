package com.example.studentpal.view.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivitySignInBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.events.MainActivity
import com.example.studentpal.viewmodel.SignInViewModel

class SignInActivity : BaseActivity() {



    private var binding: ActivitySignInBinding? = null
    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        setupActionBar()

        binding?.btnSignIn?.setOnClickListener {
            val email: String = binding?.etEmailSignIn?.text.toString().trim { it <= ' ' }
            val password: String = binding?.etPasswordSignIn?.text.toString().trim { it <= ' ' }
            viewModel.signInRegisteredUser(this,email, password)
        }

        binding?.btnForgotPassword?.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

    }


    //activates the back button and implements its functionality
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        if (viewModel.auth.currentUser?.isEmailVerified == true) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(
                this,
                "${user.email} is not verified, please check email",
                Toast.LENGTH_LONG
            ).show()
        }

    }

}