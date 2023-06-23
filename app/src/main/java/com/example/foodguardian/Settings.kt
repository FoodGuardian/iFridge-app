package com.example.foodguardian

// Imports
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

// Settings pagina klasse
class Settings : AppCompatActivity() {
    // Variabelen voor onderdelen van de layout
    private lateinit var layoutSettings: ConstraintLayout
    private lateinit var saveButton: Button
    private lateinit var notificationSwitch: Switch
    private lateinit var darkModeSwitch: Switch
    private lateinit var headerLayout: View
    private lateinit var textTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        // Initieer variabelen voor onderdelen van de layout
        layoutSettings = findViewById(R.id.layoutSettings)
        saveButton = findViewById(R.id.saveButton)
        notificationSwitch = findViewById(R.id.notifications_switch)
        darkModeSwitch = findViewById(R.id.dark_mode_switch)
        headerLayout = findViewById(R.id.header)
        textTitle = findViewById(R.id.textTitle)

        // Open de drawer wanneer er op geklikt wordt
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenudropdown).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Zet de switches op de ingestelde waarde
        loadData()

        // Sla waarde van switches op wanneer er op de save-knop wordt gedrukt
        saveButton.setOnClickListener{
            saveData()
        }

        // Ga naar andere pagina wanneer er op een optie in het drawer menu wordt geklikt
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

        // Zet de pagina in donkere modus wanneer er op de switch wordt gedrukt
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateTheme(isChecked)
        }
    }

    // Sla gegevens op in de sharedpreferences van de app
    private fun saveData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putBoolean("darkmodeSwitch", darkModeSwitch.isChecked)
        editor.putBoolean("notificationSwitch", notificationSwitch.isChecked)

        editor.apply()
        // Laat een toast zien die aangeeft dat de data is opgeslagen
        Toast.makeText(this, "Data opgeslagen", Toast.LENGTH_SHORT).show()
    }

    // Haal gegevens op uit de sharedpreferences van de app
    private fun loadData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("com.example.foodguardian", Context.MODE_PRIVATE)
        val savedNotification = sharedPreferences.getBoolean("notificationSwitch", true)
        val savedDarkmode = sharedPreferences.getBoolean("darkmodeSwitch", false)

        notificationSwitch.isChecked = savedNotification
        darkModeSwitch.isChecked = savedDarkmode
        // Pas het thema aan op basis van of donkere modus aan of uit staat
        updateTheme(savedDarkmode)
    }

    // Verander het kleurenthema naar donker of wit
    private fun updateTheme(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.dark_gray else android.R.color.white
        val textColor = if (isDarkMode) android.R.color.white else android.R.color.white
        val headerColor = if (isDarkMode) android.R.color.black else R.color.colorPrimary

        layoutSettings.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        textTitle.setTextColor(ContextCompat.getColor(this, textColor))
        headerLayout.setBackgroundColor(ContextCompat.getColor(this, headerColor))
    }
}
