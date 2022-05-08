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
import com.example.studentpal.view.MainActivity
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.google.firebase.auth.FirebaseAuth


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //sets size of logo on splashscreen
        val logoImage: ImageView = findViewById(R.id.logo)
        logoImage.layoutParams.width = 1000
        logoImage.layoutParams.height = 1000

        /**Handler delays the intent to Intro Activity by 2 seconds
         * Handler also handles auto login feature
         * If user is logged and has verified email they will be directed to the main activity,
        otherwise the Intro activity where they will be asked to sign in or sign up.
         */
        Handler(Looper.myLooper()!!).postDelayed({

            val currentUserID = getCurrentUserId()
            val fUser = FirebaseAuth.getInstance().currentUser

            /* Checks if there is a currently signed in User
             * If not send user to the Intro activity to sign in or sign up
             */
            if (fUser != null) {
                /* checks if there is a current user first
                 * Then checks if email is verified before sending user to the main activity
                 */
                if (currentUserID.isNotEmpty() && fUser.isEmailVerified) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, IntroActivity::class.java))
                }
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2000)

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


