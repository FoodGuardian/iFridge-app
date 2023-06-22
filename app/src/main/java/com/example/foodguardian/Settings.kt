package com.example.foodguardian

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.foodguardian.R.*
import com.google.android.material.navigation.NavigationView

class Settings : AppCompatActivity() {
    private lateinit var cld: ConnectionCheck
    private lateinit var layoutToolBarWithNoConnectionWithModule: ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork: ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView
    private lateinit var darkModeSwitch: Switch
    private lateinit var layoutSettings: ConstraintLayout
    private lateinit var header: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.settings_page)

        layoutSettings = findViewById(R.id.layoutSettings)
        header = findViewById(R.id.header)
        darkModeSwitch = findViewById(R.id.dark_mode_switch)

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
            if (isChecked) {
                setDarkMode()
            } else {
                setLightMode()
            }
        }
    }

    private fun setDarkMode() {
        layoutSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        header.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
        layoutOnline.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        layoutOffline.setTextColor(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun setLightMode() {
        layoutSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        header.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        layoutOnline.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        layoutOffline.setTextColor(ContextCompat.getColor(this, android.R.color.black))
    }

    private fun checkNetworkConnection() {
        cld = ConnectionCheck(application)

        cld.observe(this) { isConnected ->
            if (isConnected) {
                layoutSettings.visibility = View.VISIBLE
                layoutToolBarWithNoNetwork.visibility = View.GONE
            } else {
                layoutSettings.visibility = View.GONE
                layoutToolBarWithNoNetwork.visibility = View.VISIBLE
                layoutToolBarWithNoConnectionWithModule.visibility = View.GONE
            }
        }
    }
}
