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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class Screen : AppCompatActivity() {

    private lateinit var cld : ConnectionCheck

    private lateinit var layoutToolBarWithNetwork : ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork : ConstraintLayout
    private var productList = ProductList(this)
    private var Channel_ID = "Channel_ID_Test"
    private var Notification_ID = 1304382

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)
        layoutToolBarWithNetwork = findViewById(R.id.layoutToolBarWithNetwork)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)
        checkNetworkConnection()
        ensureNotificationPermission()
        createNotificationChannel()

        productList.addProduct(
            "https://static.ah.nl/dam/product/AHI_43545239393038353931?revLabel=1&rendition=800x800_JPG_Q90&fileType=binary",
            "AH",
            "Kaasblokjes",
            "01/01/2024"
        ).setOnClickListener {
            sendNotification(it as LinearLayout)
        }

        productList.addProduct(
            "https://partyverhuren.nl/wp-content/uploads/2017/03/4400-foto-1.jpg",
            "Coca Cola",
            "1L fles",
            "01/01/2024"
        )

        productList.addProduct(
            "https://static.ah.nl/dam/product/AHI_43545237303539303936?revLabel=4&rendition=800x800_JPG_Q90&fileType=binary",
            "Hergo",
            "Filet Americain",
            "01/01/2024"
        )

        productList.addProduct(
            "https://static-images.jumbo.com/product_images/144368ZK-2_360x360_2.png",
            "Jumbo",
            "Zoete kleine appeltjes",
            "01/01/2024"
        )

        productList.addProduct(
            "https://static-images.jumbo.com/product_images/142710FLS-1_360x360_2.png",
            "Jumbo",
            "Joppiesaus",
            "01/01/2024"
        )

        productList.addProduct(
            "https://kips.nl/wp-content/uploads/2020/03/kips-snijleverworst-125g.jpg",
            "Kips",
            "Snijleverworst",
            "01/01/2024"
        )

        productList.addProduct(
            "https://www.drinks4you.eu/wp-content/uploads/2021/05/022701..jpg",
            "Hertog Jan",
            "Pils",
            "01/01/2024"
        )

        productList.addProduct(
            "https://www.deprijshamer.nl/data/temp/monster-energy.93f98182d2e7772efdf5c95b0ad3b81d.jpg",
            "Monster",
            "Energy Drink",
            "01/01/2024"
        )

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

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(product: LinearLayout) {
        var product = this.productList.getProduct(product)
        var pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        var builder = NotificationCompat.Builder(this, Channel_ID)
            .setSmallIcon(R.drawable.ifridge)
            .setContentTitle("Overdatum")
            .setContentText("Het volgende product is bijna overdatum ${product?.productName}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)){
            notify(Notification_ID, builder.build())
        }
    }
}