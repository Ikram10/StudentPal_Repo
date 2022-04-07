package com.example.studentpal.activities


import android.content.ContentValues
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivitySignUpBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.User
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.actionCodeSettings
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class SignUpActivity : BaseActivity() {

    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }



        binding?.existingAccount?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }


    }

    // My Code: This code will retrieve the current date and get the date the user first sign up to StudentPal
    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()

        return formatter.format(date)
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


    //method responsible for authenticating users and sending verification email
    private fun registerUser() {
        var mAuth = FirebaseAuth.getInstance()
        val name: String = binding?.etName?.text.toString().trim { it <= ' ' }
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }


        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val firebaseUser: FirebaseUser = it.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val dateJoined = getCurrentDate()
                        //User object constructed to be stored in Firestore
                        val user = User(firebaseUser.uid, name, registeredEmail, dateJoined)

                        firebaseUser.sendEmailVerification().addOnSuccessListener {
                            Toast.makeText(this, "Email verification link sent to ${firebaseUser.email}", Toast.LENGTH_LONG).show()
                            FirestoreClass().registerUser(this, user)
                        }.addOnFailureListener{
                            Log.e(javaClass.simpleName,"error sending verification link")
                            Toast.makeText(this, "Could not send email verification link", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        //if a user with the same credentials already exists, registration will fail
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
        }
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

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                return true
            }
        }

    }

}