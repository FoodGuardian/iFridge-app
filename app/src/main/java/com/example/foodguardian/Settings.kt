package com.example.foodguardian

import android.content.Intent
import android.os.Bundle

import android.content.SharedPreferences
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.foodguardian.R.*
import com.google.android.material.navigation.NavigationView

class Settings : AppCompatActivity() {
    private lateinit var cld: ConnectionCheck
    private lateinit var layoutToolBarWithNoConnectionWithModule : ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork: ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView
    private lateinit var saveButton: Button
    private lateinit var notifications_switch: Switch
    private lateinit var dark_mode_switch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.settings_page)
        saveButton = findViewById<Button>(R.id.saveButton)
        notifications_switch = findViewById<Switch>(R.id.notifications_switch)
        dark_mode_switch = findViewById<Switch>(R.id.dark_mode_switch)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)
        layoutToolBarWithNoConnectionWithModule = findViewById(R.id.layoutToolBarWithNoConnectionWithModule)
        layoutOnline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOnline)
        layoutOffline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOffline)
        checkNetworkConnection()
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
    }
    private fun saveData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("darkmodeSwitch", dark_mode_switch.isChecked)
        editor.putBoolean("notificationSwitch", notifications_switch.isChecked)
        editor.apply()
        Toast.makeText(this, "Data opgeslagen", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        val savedNotification = sharedPreferences.getBoolean("notificationSwitch", true)
        val savedDarkmode = sharedPreferences.getBoolean("darkmodeSwitch", false)

        notifications_switch.isChecked = savedNotification
        dark_mode_switch.isChecked = savedDarkmode
    }

    private fun checkNetworkConnection() {
        cld = ConnectionCheck(application)

        cld.observe(this) { isConnected ->
            if (isConnected) {
                layout.settings_page = View.VISIBLE
                layoutToolBarWithNoNetwork.visibility = View.GONE
            } else {
                layout.settings_page = View.GONE
                layoutToolBarWithNoNetwork.visibility = View.VISIBLE
                layoutToolBarWithNoConnectionWithModule.visibility = View.GONE
            }
        }
    }
}