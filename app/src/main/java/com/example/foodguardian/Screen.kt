package com.example.foodguardian

// Imports
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
import android.graphics.Color
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

// Home pagina scherm klasse
class Screen : AppCompatActivity() {

    // Layout variabelen voor home pagina
    private lateinit var cld: ConnectionCheck

    private lateinit var layoutToolBarWithNetwork: ConstraintLayout
    private lateinit var layoutToolBarWithNoConnectionWithModule: ConstraintLayout
    private lateinit var layoutToolBarWithNoNetwork: ConstraintLayout
    private lateinit var layoutCredits: ConstraintLayout
    private lateinit var layoutOnline: TextView
    private lateinit var layoutOffline: TextView
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    // Initieer productenlijst
    private var productList = ProductList(this)
    private var Channel_ID = "Channel_ID_Test"
    private var notifications = arrayListOf<Int>()
    private var isReachable = false
    private lateinit var sharedPreferences: SharedPreferences
    private var savedDarkmode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)
        // Initieer verschillende layouts
        layoutToolBarWithNetwork = findViewById(R.id.layoutToolBarWithNetwork)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)
        layoutCredits = findViewById(R.id.layoutCredits)
        layoutToolBarWithNoConnectionWithModule =
            findViewById(R.id.layoutToolBarWithNoConnectionWithModule)
        layoutOnline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOnline)
        layoutOffline = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
            .findViewById<TextView>(R.id.layoutOffline)
        checkNetworkConnection()

        // Zet donkere modus wanneer dit aangezet is
        sharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        savedDarkmode = sharedPreferences.getBoolean("darkmodeSwitch", false)

        if (savedDarkmode) {
            setDarkMode()
        }

        // Verzoek om permissies wanneer de app voor het eerst opstart
        val preferences = getSharedPreferences("com.example.foodguardian", MODE_PRIVATE)
        if (preferences.getBoolean("firstrun", true)) {
            ensurePermissions()
            preferences.edit().putBoolean("firstrun", false).apply()
        }
        createNotificationChannel()
        checkStatusChangeStatus()

        // Initieer drawerlayout
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Ga naar andere pagina wanneer er op een item wordt geklikt in de drawerlayout
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

        // Stel alarmmanager in om elke minuut te checken voor nieuwe notificaties
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmClass = AlarmReceiver()
        val intent = Intent(this, alarmClass::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60000,
            pendingIntent
        )

        // Loop om te checken of een product over de datum is
        dateChecker()

        // Synchroniseer producten met de database
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            this.productList.syncProducts()
        }
    }

    // Zet de juiste layout op basis van of er een verbinding is
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

    // Vraag om permissies om push notificaties te sturen
    private fun ensurePermissions() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    // Maak een nieuw kanaal voor het versturen van notificaties
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

    // Stuur een notificatie voor een specifiek product dat deze bijna over de datum gaat
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
            val preferences = getSharedPreferences("com.example.foodguardian", MODE_PRIVATE)
            if (!preferences.getBoolean("hasNotified_${product?.productId}", false)) {
                notify(rnds, builder.build())
                preferences.edit().putBoolean("hasNotified_${product?.productId}", true).apply()
            }
        }
    }

    // Zet de juiste layout op basis van of de database bereikbaar is
    private fun checkStatusChangeStatus() {
        Thread {
            val host = Constants.baseIp
            val port = 3306
            runOnUiThread {
                findViewById<SwipeRefreshLayout>(R.id.refreshLayout).isRefreshing = true
            }
            try {
                // Controleer verbinding met de database
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 5000)
                this.isReachable = true
                socket.close()
            } catch (e: IOException) {
                // Kan geen verbinding maken met de opgegeven host en poort
                this.isReachable = false
            }
            // Synchroniseer de producten
            this.productList.syncProducts()
            runOnUiThread {
                // Zet de correcte layout
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

    // Check constant of er een product bijna over de datum gaat en stuur vervolgens een notificatie
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
                                val sharedPreferences: SharedPreferences =
                                    getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
                                val savedNotification =
                                    sharedPreferences.getBoolean("notificationSwitch", false)
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

    // Achtergrond functie om notificaties op de achtergrond te sturen
    inner class AlarmReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Thread {
                try {
                    // Vraag alle producten op
                    val connection =
                        URL("${Constants.baseUrl}/fetch").openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.connect()
                    val streamReader = InputStreamReader(connection.inputStream)
                    val bufferReader = BufferedReader(streamReader)
                    val sharedPreferences: SharedPreferences =
                        context.getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
                    val savedNotification =
                        sharedPreferences.getBoolean("notificationSwitch", false)
                    val products = JSONArray(bufferReader.readText())
                    bufferReader.close()
                    streamReader.close()
                    // Check voor elk product of deze bijna over de datum gaat
                    for (i in 0 until products.length()) {
                        val product = products[i] as JSONObject
                        val expiration = product.getJSONObject("expiration")
                        val date = LocalDate.of(
                            expiration.getInt("year"),
                            expiration.getInt("month"),
                            expiration.getInt("day")
                        )
                        if (ChronoUnit.DAYS.between(LocalDate.now(), date) < 3) {
                            val formattedDate =
                                date.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                            val pendingIntent: PendingIntent =
                                PendingIntent.getActivity(
                                    context,
                                    0,
                                    Intent(),
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            val builder = NotificationCompat.Builder(context, "Channel_ID_Test")
                                .setSmallIcon(R.drawable.ifridge)
                                .setContentTitle("Houdbaarheid ${product.getString("productName")}")
                                .setContentText("Het volgende product is bijna overdatum: ${product.getString("productName")} de houdbaarheidsdatum is $formattedDate")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent)
                            with(NotificationManagerCompat.from(context)) {
                                val preferences = context.getSharedPreferences("com.example.foodguardian", MODE_PRIVATE)
                                if (!preferences.getBoolean("hasNotified_${product.getString("productId")}", false)) {
                                    // Stuur een notificatie wanneer het product bijna over de datum gaat
                                    if (savedNotification) {
                                        notify(i, builder.build())
                                    }
                                    preferences.edit().putBoolean("hasNotified_${product.getString("productId")}", true).apply()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Geen verbinding kunnen maken met het apparaat
                }
            }.start()
        }
    }

    // Stel donkere modus in
    private fun setDarkMode() {
        val layout = findViewById<ConstraintLayout>(R.id.layoutToolBarWithNetwork)
        val header = findViewById<LinearLayout>(R.id.header)

        layout.setBackgroundColor(Color.DKGRAY)
        header.setBackgroundColor(Color.BLACK)
    }
}
