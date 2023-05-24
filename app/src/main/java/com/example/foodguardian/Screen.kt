package com.example.foodguardian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class Screen : AppCompatActivity() {

    private lateinit var cld : ConnectionCheck

    private lateinit var layoutToolBarWithNetwork : ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork : ConstraintLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)

        checkNetworkConnection()

        ProductGenerator(this).addProduct(
            "https://www.misterslipper.nl/wp-content/uploads/2019/08/markrutte_thumb.jpg",
            "FoodGuardian",
            "Mark Rutte Slippers",
            "Nooit"
        )

        layoutToolBarWithNetwork = findViewById(R.id.layoutToolBarWithNetwork)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)

        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun checkNetworkConnection(){
       cld = ConnectionCheck(application)

       cld.observe(this) { isConnected ->
           if (isConnected) {
               layoutToolBarWithNetwork.visibility = View.VISIBLE
               layoutToolBarWithNoNetwork.visibility = View.GONE
           } else {
               layoutToolBarWithNetwork.visibility = View.GONE
               layoutToolBarWithNoNetwork.visibility = View.VISIBLE
           }
       }
    }

}