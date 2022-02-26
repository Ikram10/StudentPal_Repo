package com.example.studentpal

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.TextView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val typeFace: Typeface = Typeface.createFromAsset(assets,"fonts/Font-Nongman.ttf" )
        val tvappname : TextView = findViewById(R.id.tv_app_name)
        tvappname.typeface = typeFace


    }
}