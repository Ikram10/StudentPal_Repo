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
/**
 * This activity is responsible for displaying the Sign In views
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 *
 * All code that was created by the author is labeled with [My Code].
 * The code was adapted as the author implemented the MVVM design pattern
 * which required architectural principles to be implemented.
 * @see[com.example.studentpal.common.References]
 * @see viewModel
 *
 *
 */

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
            // Retrieves email and password text input
            val email: String = binding?.etEmailSignIn?.text.toString().trim { it <= ' ' }
            val password: String = binding?.etPasswordSignIn?.text.toString().trim { it <= ' ' }

            viewModel.signInUser(this,email, password)
        }

        binding?.btnForgotPassword?.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

    }

    /**
     * Activates back button in the toolbar
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            // Sends user back to the previous activity
            onBackPressed()
        }
    }

    /**
     * A method that ensures email is verified before signing user in
     */
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