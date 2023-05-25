package com.example.foodguardian

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setPadding
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class Product(val imageUrl: String, val brandName: String, val productName: String, val expirationDate: String)

class ProductList(private val context: AppCompatActivity) {
    var products = mutableMapOf<LinearLayout, Product>()

    fun syncProducts() {
        for (product in this.products) {
            this.removeProduct(product.key)
        }
    }

    fun addProduct(imageUrl: String, brandName: String, productName: String, expirationDate: String): LinearLayout? {
        var productList = this.context.findViewById<LinearLayout>(R.id.productList)
        var density = this.context.resources.displayMetrics.density.toInt()
        var linearLayout = LinearLayout(this.context)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200 * density)
        params.bottomMargin = 20 * density
        linearLayout.layoutParams = params
        linearLayout.setBackgroundResource(R.drawable.product)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = View.generateViewId()
        productList.addView(linearLayout)
        var constraintLayout = ConstraintLayout(this.context)
        var params2 = LinearLayout.LayoutParams(200 * density, 200 * density)
        constraintLayout.layoutParams = params2
        constraintLayout.setPadding(20 * density)
        constraintLayout.id = View.generateViewId()
        linearLayout.addView(constraintLayout)
        var imageView = ImageView(this.context)
        var params3 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
        imageView.layoutParams = params3
        imageView.id = View.generateViewId()
        constraintLayout.addView(imageView)
        var constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(imageView.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 0)
        constraintSet.connect(imageView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 0)
        constraintSet.applyTo(constraintLayout)
        var linearLayout2 = LinearLayout(this.context)
        var params4 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout2.layoutParams = params4
        linearLayout2.setPadding(0, 20 * density, 0, 20 * density)
        linearLayout2.orientation = LinearLayout.VERTICAL
        linearLayout2.id = View.generateViewId()
        linearLayout.addView(linearLayout2)
        var textView = TextView(this.context)
        var params5 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        textView.layoutParams = params5
        textView.text = brandName
        textView.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView.textSize = (8 * density).toFloat()
        textView.id = View.generateViewId()
        linearLayout2.addView(textView)
        var textView2 = TextView(this.context)
        textView2.layoutParams = params5
        textView2.text = productName
        textView2.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView2.textSize = (10 * density).toFloat()
        textView2.id = View.generateViewId()
        linearLayout2.addView(textView2)
        var textView3 = TextView(this.context)
        textView3.layoutParams = params5
        var expiration = "Houdbaar tot: $expirationDate"
        textView3.text = expiration
        textView3.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView3.textSize = (8 * density).toFloat()
        textView3.id = View.generateViewId()
        linearLayout2.addView(textView3)
        Thread {
            try {
                var connection = URL(imageUrl).openConnection()
                connection.connect()
                var bitmap = BitmapFactory.decodeStream(connection.getInputStream())
                this.context.runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (exc: Exception) {}
        }.start()
        this.products[linearLayout] = Product(imageUrl, brandName, productName, expirationDate)
        return linearLayout
    }

    fun removeProduct(product: LinearLayout) {
        this.context.findViewById<LinearLayout>(R.id.productList).removeView(product)
        this.products.remove(product)
    }
}