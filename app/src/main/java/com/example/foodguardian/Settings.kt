package com.example.foodguardian

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Settings : AppCompatActivity() {
    private lateinit var layoutSettings: ConstraintLayout
    private lateinit var saveButton: Button
    private lateinit var notificationSwitch: Switch
    private lateinit var darkModeSwitch: Switch
    private lateinit var headerLayout: View
    private lateinit var textTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        layoutSettings = findViewById(R.id.layoutSettings)
        saveButton = findViewById(R.id.saveButton)
        notificationSwitch = findViewById(R.id.notifications_switch)
        darkModeSwitch = findViewById(R.id.dark_mode_switch)
        headerLayout = findViewById(R.id.header)
        textTitle = findViewById(R.id.textTitle)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        loadData()

        saveButton.setOnClickListener{
            saveData()
        }
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuProductList -> {
                    val intent = Intent(this@Settings, Screen::class.java)
                    startActivity(intent)
                }

                R.id.menuSettings -> {
                    val intent = Intent(this@Settings, Settings::class.java)
                    startActivity(intent)
                }

                R.id.menuCredits -> {
                    val intent = Intent(this@Settings, Screen::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateTheme(isChecked)
        }
    }

    private fun saveData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putBoolean("darkmodeSwitch", darkModeSwitch.isChecked)
        editor.putBoolean("notificationSwitch", notificationSwitch.isChecked)

        editor.apply()
        Toast.makeText(this, "Data opgeslagen", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        val savedNotification = sharedPreferences.getBoolean("notificationSwitch", true)
        val savedDarkmode = sharedPreferences.getBoolean("darkmodeSwitch", false)

        notificationSwitch.isChecked = savedNotification
        darkModeSwitch.isChecked = savedDarkmode
        updateTheme(savedDarkmode)
    }

    private fun updateTheme(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.dark_grey else android.R.color.white
        val textColor = if (isDarkMode) android.R.color.white else android.R.color.black
        val headerColor = if (isDarkMode) android.R.color.black else R.color.colorPrimary

        layoutSettings.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        textTitle.setTextColor(ContextCompat.getColor(this, textColor))
        headerLayout.setBackgroundColor(ContextCompat.getColor(this, headerColor))
    }
}
