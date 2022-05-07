package com.example.studentpal.view.registration


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivitySignUpBinding
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.viewmodel.SignUpViewModel


class SignUpActivity : BaseActivity() {

    private var binding: ActivitySignUpBinding? = null
    lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Gets a reference to the SignUpViewModel class
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        setupActionBar()

        binding?.btnSignUp?.setOnClickListener {
            //removes spaces from input
            val name: String = binding?.etName?.text.toString().trim { it <= ' ' }
            val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
            val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }
            val username: String = binding?.etUsername?.text.toString().trim { it <= ' ' }
            viewModel.authenticateUser(this, name, email, password, username)
        }

        binding?.existingAccount?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    /* method handles the functionality when a user successfully registers
       * sends the user to the email verification page
       */
    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_LONG
        ).show()

        hideProgressDialog()
        intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    //activates the back button and implements its functionality
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        }
        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }



}