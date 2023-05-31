package com.example.foodguardian

import android.graphics.BitmapFactory
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setPadding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Product(var productCode: String, var brandName: String, var productName: String, var expirationDate: String)

class ProductList(private val context: AppCompatActivity) {
    var products = mutableMapOf<LinearLayout, Product>()

    fun syncProducts() {
        Thread {
            try {
                var refreshLayout = this.context.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
                this.context.runOnUiThread {
                    refreshLayout.isRefreshing = true
                }
                var iterator = this.products.iterator()
                while (iterator.hasNext()) {
                    var product = iterator.next()
                    this.context.findViewById<LinearLayout>(R.id.productList).removeView(product.key)
                    iterator.remove()
                }
                var connection = URL("http://ifridge.local/fetch").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connect()
                var streamReader = InputStreamReader(connection.inputStream)
                var bufferReader = BufferedReader(streamReader)
                var products = JSONArray(bufferReader.readText())
                bufferReader.close()
                streamReader.close()
                this.context.runOnUiThread {
                    for (i in 0 until products.length()) {
                        var product = products[i] as JSONObject
                        this.addProduct(product.getString("productId"), product.getString("brandName"), product.getString("productName"), product.getString("expirationDate"))
                    }
                    refreshLayout.isRefreshing = false
                }
            } catch (exc: Exception) {}
        }.start()
    }

    private fun addProduct(productCode: String, brandName: String, productName: String, expirationDate: String): LinearLayout {
        var productList = this.context.findViewById<LinearLayout>(R.id.productList)
        var linearLayout = LinearLayout(this.context)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400)
        params.bottomMargin = 40
        linearLayout.layoutParams = params
        linearLayout.setBackgroundResource(R.drawable.product)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = View.generateViewId()
        productList.addView(linearLayout)
        var constraintLayout = ConstraintLayout(this.context)
        var params2 = LinearLayout.LayoutParams(400, 400)
        constraintLayout.layoutParams = params2
        constraintLayout.setPadding(40)
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
        linearLayout2.setPadding(0, 40, 40, 40)
        linearLayout2.orientation = LinearLayout.VERTICAL
        linearLayout2.id = View.generateViewId()
        linearLayout.addView(linearLayout2)
        var textView = TextView(this.context)
        var params5 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        textView.layoutParams = params5
        textView.text = brandName
        textView.isSingleLine = true
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView.textSize = 16.0f
        textView.id = View.generateViewId()
        linearLayout2.addView(textView)
        var textView2 = TextView(this.context)
        textView2.layoutParams = params5
        textView2.text = productName
        textView2.isSingleLine = true
        textView2.ellipsize = TextUtils.TruncateAt.END
        textView2.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView2.textSize = 20.0f
        textView2.id = View.generateViewId()
        linearLayout2.addView(textView2)
        var textView3 = TextView(this.context)
        textView3.layoutParams = params5
        var expiration = "Houdbaar tot: $expirationDate"
        textView3.text = expiration
        textView3.isSingleLine = true
        textView3.ellipsize = TextUtils.TruncateAt.END
        textView3.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView3.textSize = 14.0f
        textView3.id = View.generateViewId()
        linearLayout2.addView(textView3)
        Thread {
            try {
                var connection = URL("https://world.openfoodfacts.org/api/v0/product/$productCode.json").openConnection()
                connection.doOutput = true
                connection.connect()
                var streamReader = InputStreamReader(connection.inputStream)
                var bufferReader = BufferedReader(streamReader)
                var info = JSONObject(bufferReader.readText())
                bufferReader.close()
                streamReader.close()
                var stream = URL(info.getJSONObject("product").getString("image_url")).openStream()
                var bitmap = BitmapFactory.decodeStream(stream)
                this.context.runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (exc: Exception) {}
        }.start()
        this.products[linearLayout] = Product(productCode, brandName, productName, expirationDate)
        return linearLayout
    }

    fun getProduct(product: LinearLayout): Product? {
        return this.products[product]
    }
}