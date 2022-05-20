package com.example.studentpal.view.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivitySignUpBinding
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.viewmodel.SignUpViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This activity is responsible for displaying the Sign Up views
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 *
 * All code that was adapted by the author is labeled with [My Code].
 * The Code was adapted as the author implemented the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * The author made use of code found in the Firebase Documentation to implement the Sign up functionality.
 *
 * { [Firebase Documentation](https://firebase.google.com/docs/auth)
 * @see[com.example.studentpal.common.References]
 * @see viewModel
 */

@Suppress("OPT_IN_IS_NOT_ENABLED")
class SignUpActivity : BaseActivity() {

    private var binding: ActivitySignUpBinding? = null

    lateinit var viewModel: SignUpViewModel

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // MyCode: Reference to the SignUpViewModel
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        setupActionBar()

        binding?.btnSignUp?.setOnClickListener {
            // Removes spaces from input
            val name: String = binding?.etName?.text.toString().trim { it <= ' ' }
            val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
            val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }
            val username: String = binding?.etUsername?.text.toString().trim { it <= ' ' }

            // MyCode: Coroutine executed in IO thread because a network request is being made
            GlobalScope.launch(Dispatchers.IO) {
                viewModel.authenticateUser(this@SignUpActivity, name, email, password, username)
            }
        }

        binding?.existingAccount?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    /**
     * Method called when user successfully registers, awaiting verification
     *
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

    /**
     * Activates back button in toolbar
     */
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