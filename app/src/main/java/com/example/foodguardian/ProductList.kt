package com.example.foodguardian

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.ColorFilter
import android.opengl.Visibility
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Product(var productCode: String, var brandName: String, var productName: String, var expirationDate: LocalDate, var hasNotified: Boolean = false)

class ProductList(private val context: Screen) {
    var products = mutableMapOf<LinearLayout, Product>()

    fun syncProducts() {
        Thread {
            val refreshLayout = this.context.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            try {
                this.context.runOnUiThread {
                    refreshLayout.isRefreshing = true
                }
                val iterator = this.products.iterator()
                while (iterator.hasNext()) {
                    val product = iterator.next()
                    this.context.findViewById<LinearLayout>(R.id.productList).removeView(product.key)
                    iterator.remove()
                }
                val connection = URL("http://ifridge.local/fetch").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connect()
                val streamReader = InputStreamReader(connection.inputStream)
                val bufferReader = BufferedReader(streamReader)
                val products = JSONArray(bufferReader.readText())
                bufferReader.close()
                streamReader.close()
                this.context.runOnUiThread {
                    for (i in 0 until products.length()) {
                        val product = products[i] as JSONObject
                        val expiration = product.getJSONObject("expiration")
                        val expirationDate = LocalDate.of(expiration.getInt("year"), expiration.getInt("month"), expiration.getInt("day"))
                        this.addProduct(product.getString("productCode"), product.getString("brandName"), product.getString("productName"), expirationDate)
                    }
                    refreshLayout.isRefreshing = false
                }
            } catch (_: Exception) {
                this.context.runOnUiThread {
                    refreshLayout.isRefreshing = false
                }
            }
        }.start()
    }

    private fun addProduct(productCode: String, brandName: String, productName: String, expirationDate: LocalDate): LinearLayout {
        var productList = this.context.findViewById<LinearLayout>(R.id.productList)
        val linearLayout = LinearLayout(this.context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400)
        params.bottomMargin = 40
        linearLayout.layoutParams = params
        linearLayout.setBackgroundResource(R.drawable.product)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = View.generateViewId()
        productList.addView(linearLayout)
        val constraintLayout = ConstraintLayout(this.context)
        val params2 = LinearLayout.LayoutParams(400, 400)
        constraintLayout.layoutParams = params2
        constraintLayout.setPadding(40)
        constraintLayout.id = View.generateViewId()
        linearLayout.addView(constraintLayout)
        var imageView = ImageView(this.context)
        val params3 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
        imageView.layoutParams = params3
        imageView.id = View.generateViewId()
        constraintLayout.addView(imageView)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(imageView.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 0)
        constraintSet.connect(imageView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 0)
        constraintSet.applyTo(constraintLayout)
        val linearLayout2 = LinearLayout(this.context)
        val params4 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout2.layoutParams = params4
        linearLayout2.setPadding(0, 40, 40, 40)
        linearLayout2.orientation = LinearLayout.VERTICAL
        linearLayout2.id = View.generateViewId()
        linearLayout.addView(linearLayout2)
        val textView = TextView(this.context)
        val params5 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        textView.layoutParams = params5
        textView.text = brandName
        textView.isSingleLine = true
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView.textSize = 16.0f
        textView.id = View.generateViewId()
        linearLayout2.addView(textView)
        val textView2 = TextView(this.context)
        textView2.layoutParams = params5
        textView2.text = productName
        textView2.isSingleLine = true
        textView2.ellipsize = TextUtils.TruncateAt.END
        textView2.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView2.textSize = 20.0f
        textView2.id = View.generateViewId()
        linearLayout2.addView(textView2)
        val textView3 = TextView(this.context)
        textView3.layoutParams = params5
        val format = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        val expiration = "Houdbaar tot: ${expirationDate.format(format)}"
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
                connection.connectTimeout = 5000
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
        linearLayout.setOnClickListener {
            this.context.findViewById<ConstraintLayout>(R.id.productLayout).visibility = View.VISIBLE
            productList.visibility = View.GONE
            var productView = this.context.findViewById<LinearLayout>(R.id.productView)
            var linearLayout3 = LinearLayout(this.context)
            var params6 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            linearLayout3.layoutParams = params6
            linearLayout3.orientation = LinearLayout.HORIZONTAL
            linearLayout3.setPadding(40)
            linearLayout3.id = View.generateViewId()
            productView.addView(linearLayout3)
            var imageView2 = ImageView(this.context)
            var params7 = LinearLayout.LayoutParams(400, 400)
            imageView2.layoutParams = params7
            imageView2.setImageDrawable(imageView.drawable)
            imageView2.id = View.generateViewId()
            linearLayout3.addView(imageView2)
            var linearLayout4 = LinearLayout(this.context)
            var params8 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params8.weight = 1.0f
            params8.gravity = Gravity.CENTER_VERTICAL
            linearLayout4.layoutParams = params8
            linearLayout4.orientation = LinearLayout.VERTICAL
            linearLayout4.id = View.generateViewId()
            linearLayout3.addView(linearLayout4)
            var textView4 = TextView(this.context)
            var params9 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textView4.layoutParams = params9
            textView4.text = brandName
            textView4.textSize = 16.0f
            textView4.id = View.generateViewId()
            linearLayout4.addView(textView4)
            var textView5 = TextView(this.context)
            textView5.layoutParams = params9
            textView5.text = productName
            textView5.textSize = 20.0f
            textView5.id = View.generateViewId()
            linearLayout4.addView(textView5)
            var textView6 = TextView(this.context)
            textView6.layoutParams = params9
            textView6.text = expiration
            textView6.textSize = 14.0f
            textView6.id = View.generateViewId()
            linearLayout4.addView(textView6)
            var button = Button(this.context)
            button.layoutParams = params9
            button.setBackgroundColor(this.context.resources.getColor(R.color.colorPrimary, null))
            var text = "Genereer recept"
            button.text = text
            button.setTextColor(this.context.resources.getColor(R.color.white, null))
            button.id = View.generateViewId()
            productView.addView(button)
            button.setOnClickListener {
                var spinner = ProgressBar(this.context)
                var params11 = LinearLayout.LayoutParams(100, 100)
                params11.setMargins(0, 30, 0, 0)
                params11.gravity = Gravity.CENTER_HORIZONTAL
                spinner.layoutParams = params11
                spinner.id = View.generateViewId()
                productView.addView(spinner)
                Thread {
                    try {
                        var connection = URL("http://ifridge.local/recipe").openConnection() as HttpURLConnection
                        connection.doOutput = true
                        connection.requestMethod = "POST"
                        connection.connectTimeout = 5000
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        var body = "mainIngredient=${URLEncoder.encode(productName, "UTF-8")}"
                        for (product in this.products) {
                            if (product.key != linearLayout) {
                                body += "&ingredients=${URLEncoder.encode(product.value.productName, "UTF-8")}"
                            }
                        }
                        var outputStream = connection.outputStream
                        outputStream.write(body.toByteArray())
                        outputStream.close()
                        connection.connect()
                        var streamReader = InputStreamReader(connection.inputStream)
                        var bufferReader = BufferedReader(streamReader)
                        var info = JSONObject(bufferReader.readText())
                        var text = "${info.getString("prefix")}\n\nIngrediënten:\n"
                        var ingredients = info.getJSONArray("ingredients")
                        for (i in 0 until ingredients.length())
                        {
                            text += "- ${ingredients.getString(i)}\n"
                        }
                        text += "\nInstructies:\n\n"
                        var instructions = info.getJSONArray("instructions")
                        for (i in 0 until instructions.length())
                        {
                            text += "${i + 1}. ${instructions.getString(i)}\n"
                        }
                        text += "\n${info.getString("suffix")}"
                        bufferReader.close()
                        streamReader.close()
                        this.context.runOnUiThread {
                            productView.removeView(spinner)
                            var textView7 = TextView(this.context)
                            textView7.layoutParams = params9
                            textView7.setPadding(0, 20, 0, 0)
                            textView7.text = text
                            textView7.id = View.generateViewId()
                            productView.addView(textView7)
                        }
                    } catch (_: Exception) {
                        this.context.runOnUiThread {
                            productView.removeView(spinner)
                            var textView7 = TextView(this.context)
                            textView7.layoutParams = params9
                            textView7.setPadding(0, 20, 0, 0)
                            var text2 = "Recept genereren mislukt."
                            textView7.text = text2
                            textView7.id = View.generateViewId()
                            productView.addView(textView7)
                        }
                    }
                }.start()
            }
        }
        this.products[linearLayout] = Product(productCode, brandName, productName, expirationDate)
        return linearLayout
    }

    fun getProduct(product: LinearLayout): Product? {
        return this.products[product]
    }
}