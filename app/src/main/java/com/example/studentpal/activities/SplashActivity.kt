package com.example.studentpal.activities

import android.annotation.SuppressLint
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.studentpal.R
import com.example.studentpal.firebase.FirestoreClass


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //sets size of logo on splashscreen
        val logoimage: ImageView = findViewById(R.id.logo)
        logoimage.layoutParams.width = 1000
        logoimage.layoutParams.height = 1000

        /**Handler delays the intent to Intro Activity by 2 seconds
         * Handler also handles auto login feature
         * If user is logged in they will be directed to the main activity, otherwise the Intro activity
         where they will be asked to sign in or sign up.
         */
        Handler().postDelayed({

            var currentUserID = FirestoreClass().getCurrentUserId()

            if (currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish() }, 2000)

        hideSystemBars()

    }
    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}


