package com.example.foodguardian


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.SharedPreferences
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class Screen : AppCompatActivity() {

    private lateinit var cld: ConnectionCheck

    private lateinit var layoutToolBarWithNetwork: ConstraintLayout
    private lateinit var layoutToolBarWithNoConnectionWithModule : ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork: ConstraintLayout
    private lateinit var layoutCredits : ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

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
        val preferences = getSharedPreferences("com.example.foodguardian", MODE_PRIVATE)
        if (preferences.getBoolean("firstrun", true)) {
            ensurePermissions()
            preferences.edit().putBoolean("firstrun", false).apply()
        }
        createNotificationChannel()
        checkStatusChangeStatus()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuProductList -> {
                    val intent = Intent(this@Screen, Screen::class.java)
                    startActivity(intent)
                }

                R.id.menuSettings -> {
                    val intent = Intent(this@Screen, Settings::class.java)
                    startActivity(intent)
                }

                R.id.menuCredits -> {
                    val intent = Intent(this@Screen, Credits::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmClass = AlarmReceiver()
        val intent = Intent(this, alarmClass::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )

        dateChecker()

        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            this.productList.syncProducts()
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
                layoutToolBarWithNoConnectionWithModule.visibility = View.GONE
            }
        }
    }

    private fun ensurePermissions() {
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
            var preferences = getSharedPreferences("com.example.foodguardian", MODE_PRIVATE)
            if (!preferences.getBoolean("hasNotified_${product?.productId}", false)) {
                notify(rnds, builder.build())
                preferences.edit().putBoolean("hasNotified_${product?.productId}", true).apply()
            }
        }
    }

    private fun checkStatusChangeStatus() {
        Thread {
            val host = Constants.baseIp
            val port = 3306
            runOnUiThread {
                findViewById<SwipeRefreshLayout>(R.id.refreshLayout).isRefreshing = true
            }
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 5000)
                this.isReachable = true
                socket.close()
            } catch (e: IOException) {
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
                                this.productList.getProduct(product.key)?.hasNotified = false
                                val sharedPreferences: SharedPreferences =
                                    getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
                                val savedNotification = sharedPreferences.getBoolean("notificationSwitch", false)
                                if (savedNotification) {
                                    sendNotification(product.key)
                                }
                            }
                        }
                    }
                }
            }
        }.start()
    }
    class AlarmReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Thread {
                try
                {
                    val connection = URL("http://ifridge.local/fetch").openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.connect()
                    val streamReader = InputStreamReader(connection.inputStream)
                    val bufferReader = BufferedReader(streamReader)
                    val products = JSONArray(bufferReader.readText())
                    bufferReader.close()
                    streamReader.close()
                    for (i in 0 until products.length()) {
                        val product = products[i] as JSONObject
                        val expiration = product.getJSONObject("expiration")
                        val date = LocalDate.of(expiration.getInt("year"), expiration.getInt("month"), expiration.getInt("day"))
                        if (ChronoUnit.DAYS.between(LocalDate.now(), date) < 3)
                        {
                            val formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                            val pendingIntent: PendingIntent =
                                PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
                            val builder = NotificationCompat.Builder(context, "Channel_ID_Test")
                                .setSmallIcon(R.drawable.ifridge)
                                .setContentTitle("Houdbaarheid ${product.getString("productName")}")
                                .setContentText("Het volgende product is bijna overdatum: ${product.getString("productName")} de houdbaarheidsdatum is $formattedDate")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent)
                            with(NotificationManagerCompat.from(context)) {
                                var preferences = context.getSharedPreferences("com.example.foodguardian", MODE_PRIVATE)
                                if (!preferences.getBoolean("hasNotified_${product.getInt("productId")}", false)) {
                                    notify((0..10000).random(), builder.build())
                                    preferences.edit().putBoolean("hasNotified_${product.getInt("productId")}", true).apply()
                                }
                            }
                        }
                    }
                } catch (_: Exception)
                {
                }
            }.start()
        }
    }

}


