package com.example.studentpal.view.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentpal.databinding.ActivityPasswordResetBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Password reset activity responsible for sending a password reset email
 *
 * [My Code] The code illustrated was implemented by the author.
 * The author made use of code found in the Firebase Documentation to implement the password
 * reset email.
 * [Firebase Documentation](https://firebase.google.com/docs/auth/web/manage-users)
 */

class PasswordResetActivity : AppCompatActivity() {

    var binding: ActivityPasswordResetBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding?.btnSendMessage?.setOnClickListener {
            sendPasswordResetEmail()
        }

        binding?.btnGoBack?.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }

    private fun sendPasswordResetEmail() {
        val email = binding!!.etEmail.text.toString()
        Firebase
            .auth
            .sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this,
                        "Password reset email sent",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this,
                        "Error sending password reset email",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}