package com.example.foodguardian

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

// Laat tijdelijk een afbeelding zien voordat de home pagina wordt weergegeven
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
           val intent = Intent(this@MainActivity, Screen::class.java )
            startActivity(intent)
            finish()
        }, 3000)
    }
}