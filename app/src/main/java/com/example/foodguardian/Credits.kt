package com.example.foodguardian

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class Credits : AppCompatActivity() {
    private lateinit var layoutCredits: ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView
    private lateinit var headerLayout: LinearLayout
    private var isReachable = false

    private lateinit var sharedPreferences: SharedPreferences
    private var savedDarkmode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credits)
        layoutCredits = findViewById(R.id.layoutCredits)
        layoutOnline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOnline)
        layoutOffline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOffline)
        headerLayout = findViewById(R.id.header)
        sharedPreferences = getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        savedDarkmode = sharedPreferences.getBoolean("darkmodeSwitch", false)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuProductList -> {
                    val intent = Intent(this@Credits, Screen::class.java)
                    startActivity(intent)
                }

                R.id.menuSettings -> {
                    val intent = Intent(this@Credits, Settings::class.java)
                    startActivity(intent)
                }

                R.id.menuCredits -> {
                    val intent = Intent(this@Credits, Credits::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        checkStatusChangeStatus()
        applyDarkMode()
    }

    private fun checkStatusChangeStatus() {
        Thread {
            val host = "ifridge.local"
            val port = 3306
            runOnUiThread {
            }
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 5000)
                isReachable = true
                socket.close()
            } catch (_: IOException) {
                // Kan geen verbinding maken met de opgegeven host en poort
                isReachable = false
            }
            runOnUiThread {
                if (isReachable) {
                    layoutOnline.visibility = View.VISIBLE
                    layoutOffline.visibility = View.GONE
                    layoutCredits.visibility = View.VISIBLE
                } else {
                    layoutOnline.visibility = View.GONE
                    layoutOffline.visibility = View.VISIBLE
                    layoutCredits.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun applyDarkMode() {
        if (savedDarkmode) {
            layoutCredits?.setBackgroundColor(Color.DKGRAY)
            headerLayout?.setBackgroundColor(Color.BLACK)
        }
    }
}