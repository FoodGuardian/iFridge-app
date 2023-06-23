package com.example.foodguardian

// Imports
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Klasse voor producten
class Product(var productId: Int, var productCode: String, var brandName: String, var productName: String, var expirationDate: LocalDate, var hasNotified: Boolean = false)

class ProductList(private val context: Screen) {
    // Lijst met paren van views en producten om alle producten in op te slaan
    var products = mutableMapOf<LinearLayout, Product>()

    // Synchroniseer alle producten met de database op de iFridge
    fun syncProducts() {
        Thread {
            val refreshLayout = this.context.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            try {
                this.context.runOnUiThread {
                    // Zet de herlaad-spinner aan
                    refreshLayout.isRefreshing = true
                }
                val iterator = this.products.iterator()
                // Verwijder alle oude producten
                while (iterator.hasNext()) {
                    val product = iterator.next()
                    this.context.runOnUiThread {
                        this.context.findViewById<LinearLayout>(R.id.productList).removeView(product.key)
                    }
                    iterator.remove()
                }
                // Stuur een POST verzoek naar het eindpunt /fetch om alle producten op te vragen
                val connection = URL(Constants.baseUrl + "/fetch").openConnection() as HttpURLConnection
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
                        // Parseer alle json-objecten voor alle producten
                        val product = products[i] as JSONObject
                        val expiration = product.getJSONObject("expiration")
                        val expirationDate = LocalDate.of(expiration.getInt("year"), expiration.getInt("month"), expiration.getInt("day"))
                        // Voeg het product toe
                        this.addProduct(product.getInt("productId"), product.getString("productCode"), product.getString("brandName"), product.getString("productName"), expirationDate)
                    }
                    // Zet de herlaad-spinner uit
                    refreshLayout.isRefreshing = false
                }
            } catch (_: Exception) {
                // Wanneer er iets fout gaat, zet de herlaad-spinner uit om oneindig laden te voorkomen
                this.context.runOnUiThread {
                    refreshLayout.isRefreshing = false
                }
            }
        }.start()
    }

    // Functie voor het toevoegen van een product
    private fun addProduct(productId: Int, productCode: String, brandName: String, productName: String, expirationDate: LocalDate): LinearLayout {
        // Vraag de productlijst-view op om het product aan toe te voegen
        val productList = this.context.findViewById<LinearLayout>(R.id.productList)
        // Maak een nieuwe linearlayout aan waar de gegevens van het product in worden weergegeven
        val linearLayout = LinearLayout(this.context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400)
        params.bottomMargin = 40
        linearLayout.layoutParams = params
        linearLayout.setBackgroundResource(R.drawable.product)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = View.generateViewId()
        // Voeg de linearlayout toe aan de productlijst
        productList.addView(linearLayout)
        // Maak een nieuwe contraintlayout aan waarin de afbeelding wordt weergegeven
        val constraintLayout = ConstraintLayout(this.context)
        val params2 = LinearLayout.LayoutParams(400, 400)
        constraintLayout.layoutParams = params2
        constraintLayout.setPadding(40)
        constraintLayout.id = View.generateViewId()
        // Voeg de constraintlayout toe aan het product
        linearLayout.addView(constraintLayout)
        // Maak een nieuwe imageview met een placeholder afbeelding die wordt weergegeven wanneer de afbeelding
        val imageView = ImageView(this.context)
        val params3 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
        imageView.layoutParams = params3
        imageView.setImageDrawable(ResourcesCompat.getDrawable(this.context.resources, R.drawable.ifridgeoutline, null))
        imageView.id = View.generateViewId()
        // Voeg de afbeelding toe aan de constraintlayout
        constraintLayout.addView(imageView)
        // Zet de uitlijning van de afbeelding
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(imageView.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 0)
        constraintSet.connect(imageView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 0)
        constraintSet.applyTo(constraintLayout)
        // Maak een nieuwe linearlayout om de texten in te zetten
        val linearLayout2 = LinearLayout(this.context)
        val params4 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout2.layoutParams = params4
        linearLayout2.setPadding(0, 40, 40, 40)
        linearLayout2.orientation = LinearLayout.VERTICAL
        linearLayout2.id = View.generateViewId()
        // Voeg de linearlayout toe aan het product
        linearLayout.addView(linearLayout2)
        // Maak een textview met de naam van het mark van het product
        val textView = TextView(this.context)
        val params5 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        textView.layoutParams = params5
        textView.text = brandName
        textView.isSingleLine = true
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView.textSize = 16.0f
        textView.id = View.generateViewId()
        // Voeg de tekst toe aan de linearlayout
        linearLayout2.addView(textView)
        // Maak een nieuwe textview aan met de naam van het product
        val textView2 = TextView(this.context)
        textView2.layoutParams = params5
        textView2.text = productName
        textView2.isSingleLine = true
        textView2.ellipsize = TextUtils.TruncateAt.END
        textView2.setTextColor(this.context.resources.getColor(R.color.white, null))
        textView2.textSize = 20.0f
        textView2.id = View.generateViewId()
        // Voeg de tekst toe aan de linearlayout
        linearLayout2.addView(textView2)
        // Maak een nieuwe textview aan met de houdbaarheidsdatum van het product
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
        // Voeg de tekst toe aan de linearlayout
        linearLayout2.addView(textView3)
        Thread {
            try {
                // Stuur een verzoek om gegevens van het product op te halen van de openfoodfacts database
                val connection = URL("https://world.openfoodfacts.org/api/v0/product/$productCode.json").openConnection()
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.connect()
                // Parseer het json-object en haal de url van de afbeelding van het product hieruit
                val streamReader = InputStreamReader(connection.inputStream)
                val bufferReader = BufferedReader(streamReader)
                val info = JSONObject(bufferReader.readText())
                bufferReader.close()
                streamReader.close()
                val stream = URL(info.getJSONObject("product").getString("image_url")).openStream()
                // Zet de url om in een bitmap afbeelding
                val bitmap = BitmapFactory.decodeStream(stream)
                this.context.runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (_: Exception) {}
        }.start()
        // Voer een actie wanneer er op het product wordt geklikt
        linearLayout.setOnClickListener {
            // Zet de recept-pagina op zichtbaar en de productlijst pagina op onzichtbaar
            this.context.findViewById<ConstraintLayout>(R.id.productLayout).visibility = View.VISIBLE
            productList.visibility = View.GONE
            // Vraag de view op voor de product pagina
            val productView = this.context.findViewById<LinearLayout>(R.id.productView)
            // Maak een nieuwe linearlayout aan om de onderdelen van de pagina in te zetten
            val linearLayout3 = LinearLayout(this.context)
            val params6 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            linearLayout3.layoutParams = params6
            linearLayout3.orientation = LinearLayout.HORIZONTAL
            linearLayout3.setPadding(40)
            linearLayout3.id = View.generateViewId()
            // Voeg de linearlayout toe aan de pagina
            productView.addView(linearLayout3)
            // Maak een nieuwe imageview aan voor de afbeelding van het product
            val imageView2 = ImageView(this.context)
            val params7 = LinearLayout.LayoutParams(400, 400)
            imageView2.layoutParams = params7
            imageView2.setImageDrawable(imageView.drawable)
            imageView2.id = View.generateViewId()
            // Voeg de afbeelding toe aan de linearlayout
            linearLayout3.addView(imageView2)
            // Maak een nieuwe linearlayout aan voor de teksten van het product
            val linearLayout4 = LinearLayout(this.context)
            val params8 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params8.weight = 1.0f
            params8.gravity = Gravity.CENTER_VERTICAL
            linearLayout4.layoutParams = params8
            linearLayout4.orientation = LinearLayout.VERTICAL
            linearLayout4.id = View.generateViewId()
            // Voeg de linearlayout toe aan de vorige linearlayout
            linearLayout3.addView(linearLayout4)
            // Maak een nieuwe textview aan voor de naam van het merk van het product
            val textView4 = TextView(this.context)
            val params9 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textView4.layoutParams = params9
            textView4.text = brandName
            textView4.textSize = 16.0f
            textView4.id = View.generateViewId()
            // Voeg de tekst toe aan de linearlayout
            linearLayout4.addView(textView4)
            // Maak een nieuwe textview aan met de naam van het product
            val textView5 = TextView(this.context)
            textView5.layoutParams = params9
            textView5.text = productName
            textView5.textSize = 20.0f
            textView5.id = View.generateViewId()
            // Voeg de tekst toe aan de linearlayout
            linearLayout4.addView(textView5)
            // Maak een nieuwe textview aan met de houdbaarheidsdatum van het product
            val textView6 = TextView(this.context)
            textView6.layoutParams = params9
            textView6.text = expiration
            textView6.textSize = 14.0f
            textView6.id = View.generateViewId()
            // Voeg de tekst toe aan de linearlayout
            linearLayout4.addView(textView6)
            // Maak een nieuwe button aan voor het genereren van recepten
            val button = Button(this.context)
            button.layoutParams = params9
            button.setBackgroundColor(this.context.resources.getColor(R.color.colorPrimary, null))
            val text = "Genereer recept"
            button.text = text
            button.setTextColor(this.context.resources.getColor(R.color.white, null))
            button.id = View.generateViewId()
            // Voeg de knop toe aan de product pagina
            productView.addView(button)
            // Bewaar het oude recept voor wanneer het recept vervangen moet worden
            var oldRecipe: TextView? = null
            // Maak een laad-spinner wanneer er op de knop wordt gedrukt
            button.setOnClickListener {
                val spinner = ProgressBar(this.context)
                val params11 = LinearLayout.LayoutParams(100, 100)
                params11.setMargins(0, 30, 0, 0)
                params11.gravity = Gravity.CENTER_HORIZONTAL
                spinner.layoutParams = params11
                spinner.id = View.generateViewId()
                // Verwijder het oude recept wanneer deze bestaat
                if (oldRecipe != null)
                {
                    productView.removeView(oldRecipe)
                }
                // Voeg de spinner toe aan de product pagina
                productView.addView(spinner)
                Thread {
                    try {
                        // Vraag een recept op met behulp van de producten in de koelkast
                        val connection = URL(Constants.baseUrl + "/recipe").openConnection() as HttpURLConnection
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
                        val outputStream = connection.outputStream
                        outputStream.write(body.toByteArray())
                        outputStream.close()
                        connection.connect()
                        // Parseer de gegevens van het recept
                        val streamReader = InputStreamReader(connection.inputStream)
                        val bufferReader = BufferedReader(streamReader)
                        val info = JSONObject(bufferReader.readText())
                        var txt = "${info.getString("prefix")}\n\nIngrediënten:\n"
                        val ingredients = info.getJSONArray("ingredients")
                        // Voeg de lijst met ingrediënten toe aan het recept
                        for (i in 0 until ingredients.length())
                        {
                            txt += "- ${ingredients.getString(i)}\n"
                        }
                        txt += "\nInstructies:\n\n"
                        val instructions = info.getJSONArray("instructions")
                        // Voeg de instructies toe aan het recept
                        for (i in 0 until instructions.length())
                        {
                            txt += "${i + 1}. ${instructions.getString(i)}\n"
                        }
                        txt += "\n${info.getString("suffix")}"
                        bufferReader.close()
                        streamReader.close()
                        this.context.runOnUiThread {
                            // Verwijder de spinner en voeg het recept toe aan de product pagina
                            productView.removeView(spinner)
                            val textView7 = TextView(this.context)
                            textView7.layoutParams = params9
                            textView7.setPadding(0, 20, 0, 0)
                            textView7.text = txt
                            textView7.id = View.generateViewId()
                            productView.addView(textView7)
                            // Zet het oude recept naar het huidige recept
                            oldRecipe = textView7
                        }
                    } catch (_: Exception) {
                        this.context.runOnUiThread {
                            // Geef waar dat een recept genereren is mislukt en verwijder de spinner
                            productView.removeView(spinner)
                            val textView7 = TextView(this.context)
                            textView7.layoutParams = params9
                            textView7.setPadding(0, 20, 0, 0)
                            val text2 = "Recept genereren mislukt."
                            textView7.text = text2
                            textView7.id = View.generateViewId()
                            productView.addView(textView7)
                            oldRecipe = textView7
                        }
                    }
                }.start()
            }
        }
        // Voeg het product toe aan de productenlijst
        this.products[linearLayout] = Product(productId, productCode, brandName, productName, expirationDate)
        return linearLayout
    }

    // Functie om een product op te vragen
    fun getProduct(product: LinearLayout): Product? {
        return this.products[product]
    }
}