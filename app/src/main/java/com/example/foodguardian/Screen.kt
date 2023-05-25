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
    private var productList = ProductList(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scherm)
        layoutToolBarWithNetwork = findViewById(R.id.layoutToolBarWithNetwork)
        layoutToolBarWithNoNetwork = findViewById(R.id.layoutToolBarWithNoNetwork)
        checkNetworkConnection()

        productList.addProduct(
            "https://static.ah.nl/dam/product/AHI_43545239393038353931?revLabel=1&rendition=800x800_JPG_Q90&fileType=binary",
            "AH",
            "Kaasblokjes",
            "01/01/2024"
        )

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
}