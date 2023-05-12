package com.example.foodguardian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class Scherm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)

        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

    }
}