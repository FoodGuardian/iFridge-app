package com.example.foodguardian

import android.content.Intent
import android.os.Bundle

import android.os.PersistableBundle
import android.view.View
import android.widget.TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.settings_page)
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