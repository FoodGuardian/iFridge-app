package com.example.foodguardian

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class Screen : AppCompatActivity() {

    private lateinit var cld: ConnectionCheck

    private lateinit var layoutToolBarWithNetwork: ConstraintLayout
    private lateinit var layoutToolBarWithNoConnectionWithModule : ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork: ConstraintLayout
    private lateinit var layoutCredits : ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView

    private var productList = ProductList(this)
    private var Channel_ID = "Channel_ID_Test"
    private var notifications = arrayListOf<Int>()
    var isReachable = false

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)
        layoutToolBarWithNetwork = findViewById(R.id.layoutToolBarWithNetwork)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)
        layoutCredits = findViewById(R.id.layoutCredits)
        layoutToolBarWithNoConnectionWithModule = findViewById(R.id.layoutToolBarWithNoConnectionWithModule)
        layoutOnline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOnline)
        layoutOffline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOffline)
        checkNetworkConnection()
        ensureNotificationPermission()
        createNotificationChannel()
        checkStatusChangeStatus()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuProductList -> {
                    val intent = Intent(this, this.productList::class.java)
                    startActivity(intent)
                }

                R.id.menuSettings -> {
                    val intent = Intent(this, this.productList::class.java)
                    startActivity(intent)
                }

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            this.productList.syncProducts()
        }

        dateChecker()
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
                layoutToolBarWithNoConnectionWithModule.visibility = View.GONE
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
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(product: LinearLayout) {
        val product = this.productList.getProduct(product)
        val date = product?.expirationDate?.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, Channel_ID)
            .setSmallIcon(R.drawable.ifridge)
            .setContentTitle("Houdbaarheid ${product?.productName}")
            .setContentText("Het volgende product is bijna overdatum: ${product?.productName} de houdbaarheidsdatum is ${date}")
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
            runOnUiThread {
                findViewById<SwipeRefreshLayout>(R.id.refreshLayout).isRefreshing = true
            }
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 5000)
                this.isReachable = true
                socket.close()
            } catch (_: IOException) {
                // Kan geen verbinding maken met de opgegeven host en poort
                this.isReachable = false
            }
            this.productList.syncProducts()
            runOnUiThread {
                if (this.isReachable) {
                    layoutOnline.visibility = View.VISIBLE
                    layoutOffline.visibility = View.GONE
                    layoutToolBarWithNoConnectionWithModule.visibility = View.GONE
                } else {
                    layoutOnline.visibility = View.GONE
                    layoutOffline.visibility = View.VISIBLE
                    layoutToolBarWithNoConnectionWithModule.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun dateChecker() {
        Thread {
            while (true) {
                var products: MutableMap<LinearLayout, Product>? = null
                while (products == null) {
                    try {
                        products = this.productList.products.toMutableMap()
                        break
                    } catch (_: Exception) {}
                }
                val current = LocalDate.now()
                if (products != null) {
                    for (product in products) {
                        if (!product.value.hasNotified) {
                            val daysUntilExpiry =
                                ChronoUnit.DAYS.between(current, product.value.expirationDate)
                            if (daysUntilExpiry < 3) {
                                this.productList.getProduct(product.key)?.hasNotified = true
                                sendNotification(product.key)
                            }
                        }
                    }
                }
            }
        }.start()
    }
}


