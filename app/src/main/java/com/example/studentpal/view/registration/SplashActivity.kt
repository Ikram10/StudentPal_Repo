package com.example.studentpal.view.registration

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.studentpal.R
import com.example.studentpal.view.events.MainActivity
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.google.firebase.auth.FirebaseAuth

/**
 * This activity is responsible for displaying the splash screen
 * and handling the auto login feature.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However minute changes were made to accommodate to the project requirements. For instance, autologin
 * only if the user has verified their email and is currently logged in.
 *
 * All code that was created by the author is labeled with [My Code].
 *
 * @see[com.example.studentpal.common.References]
 * @see[isEmailV]
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //sets size of logo on splashscreen
        val logoImage: ImageView = findViewById(R.id.logo)
        logoImage.layoutParams.width = 1000
        logoImage.layoutParams.height = 1000

        /**
         * Handler delays the intent to Intro Activity by 2 seconds
         * If user is logged and has verified email they will be directed to the main activity,
         */
        Handler(Looper.myLooper()!!).postDelayed({
            val currentUserID = getCurrentUserId()
            val fUser = FirebaseAuth.getInstance().currentUser

            // Checks if there is a user currently logged in
            if (fUser != null) {
                // My Code: Checks if email is verified before autologin
                if (currentUserID.isNotEmpty()
                    && fUser.isEmailVerified) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, IntroActivity::class.java))
                }
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        },
            2000)

        //hideSystemBars()
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


