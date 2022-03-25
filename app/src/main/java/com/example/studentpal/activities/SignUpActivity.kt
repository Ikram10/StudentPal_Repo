package com.example.studentpal.activities


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivitySignUpBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity : BaseActivity() {

    private var binding : ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.btnSignUp?.setOnClickListener{
            registerUser()
        }



        binding?.existingAccount?.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java ))
            finish()
        }


    }
    // My Code: This code will retrieve the current date and get the date the user first sign up to StudentPal
    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()

        return formatter.format(date)
    }


    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_LONG
        ).show()

        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }



    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim{ it <= ' '}
        val email: String = binding?.etEmail?.text.toString().trim{ it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim{ it <= ' '}

        if(validateForm(name, email, password)) {

            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                    task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val dateJoined = getCurrentDate()
                    val user = User(firebaseUser.uid, name, registeredEmail, dateJoined)
                    FirestoreClass().registerUser(this, user)
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //activates the back button and implements its functionality
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        }
        binding?.toolbarSignUpActivity?.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun validateForm(name: String, email: String, password: String) : Boolean{
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
            } else -> {
                return true
            }
        }

    }

}