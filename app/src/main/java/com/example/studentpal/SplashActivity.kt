package com.example.studentpal

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

import android.widget.TextView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //sets size of logo on splashscreen
        val logo_image : ImageView = findViewById(R.id.logo)
        logo_image.layoutParams.width = 1000
        logo_image.layoutParams.height = 1000
    }
}