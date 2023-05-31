package com.example.foodguardian

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.net.InetAddress
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Screen : AppCompatActivity() {

    private lateinit var cld: ConnectionCheck

    private lateinit var layoutToolBarWithNetwork: ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork: ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView


    private var productList = ProductList(this)
    private var Channel_ID = "Channel_ID_Test"
    private var notifications = arrayListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)
        layoutToolBarWithNetwork = findViewById(R.id.layoutToolBarWithNetwork)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)
        layoutOnline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOnline)
        layoutOffline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOffline)
        checkNetworkConnection()
        ensureNotificationPermission()
        createNotificationChannel()
        checkStatusChangeStatus()

        this.productList.syncProducts()

        var drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuProductList -> {
                }

                R.id.menuSettings -> {
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        var refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            this.productList.syncProducts()
        }

        Thread {
            while (true) {
                checkdate()
            }
        }
    }

    private fun checkNetworkConnection() {
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

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Eten is bijna overdatum"
            val descriptionText = "Het volgende product is bijna overdatum [Eten]"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Channel_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(product: LinearLayout) {
        var product = this.productList.getProduct(product)
        var pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        var builder = NotificationCompat.Builder(this, Channel_ID)
            .setSmallIcon(R.drawable.ifridge)
            .setContentTitle("Overdatum")
            .setContentText("Het volgende product is bijna overdatum ${product?.productName}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
        val rnds = (0..10000).random()
        this.notifications.add(rnds)
        with(NotificationManagerCompat.from(this)) {
            notify(rnds, builder.build())
        }
    }

    private fun checkStatusChangeStatus() {
        Thread {
            val host = "ifridge.local"
            val port = 3306
            var isReachable = false

            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 5000)
                isReachable = true
                socket.close()
            } catch (e: IOException) {
                // Kan geen verbinding maken met de opgegeven host en poort
            }

            runOnUiThread {
                if (isReachable) {
                    layoutOnline.visibility = View.VISIBLE
                    layoutOffline.visibility = View.GONE
                } else {
                    layoutOnline.visibility = View.GONE
                    layoutOffline.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkdate() {
        val products = this.productList.products
        val current = LocalDate.now()

        for (product in products) {
            val daysUntilExpiry = ChronoUnit.DAYS.between(current, product.value.expirationDate)
            if (daysUntilExpiry < 3) {
                sendNotification(product.key)
            }
        }
    }
}


