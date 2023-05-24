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
import java.net.URL

class ProductGenerator(private val context: AppCompatActivity) {
    fun addProduct(imageUrl: String, brandName: String, productName: String, expirationDate: String) {
        var density = context.resources.displayMetrics.density.toInt()
        var productList = context.findViewById<LinearLayout>(R.id.productList)
        var linearLayout = LinearLayout(context)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200 * density)
        params.bottomMargin = 20 * density
        linearLayout.layoutParams = params
        linearLayout.setBackgroundResource(R.drawable.product)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = View.generateViewId()
        productList.addView(linearLayout)
        var constraintLayout = ConstraintLayout(context)
        var params2 = LinearLayout.LayoutParams(200 * density, 200 * density)
        constraintLayout.layoutParams = params2
        constraintLayout.setPadding(20 * density)
        constraintLayout.id = View.generateViewId()
        linearLayout.addView(constraintLayout)
        var imageView = ImageView(context)
        var params3 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
        imageView.layoutParams = params3
        imageView.id = View.generateViewId()
        constraintLayout.addView(imageView)
        var constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(imageView.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 0)
        constraintSet.connect(imageView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 0)
        constraintSet.applyTo(constraintLayout)
        var linearLayout2 = LinearLayout(context)
        var params4 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout2.layoutParams = params4
        linearLayout2.setPadding(0, 20 * density, 0, 20 * density)
        linearLayout2.orientation = LinearLayout.VERTICAL
        linearLayout2.id = View.generateViewId()
        linearLayout.addView(linearLayout2)
        var textView = TextView(context)
        var params5 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        textView.layoutParams = params5
        textView.text = brandName
        textView.textSize = (8 * density).toFloat()
        textView.id = View.generateViewId()
        linearLayout2.addView(textView)
        var textView2 = TextView(context)
        textView2.layoutParams = params5
        textView2.text = productName
        textView2.textSize = (10 * density).toFloat()
        textView2.id = View.generateViewId()
        linearLayout2.addView(textView2)
        var textView3 = TextView(context)
        textView3.layoutParams = params5
        var expiration = "Houdbaar tot: $expirationDate"
        textView3.text = expiration
        textView3.textSize = (8 * density).toFloat()
        textView3.id = View.generateViewId()
        linearLayout2.addView(textView3)
        Thread {
            var connection = URL(imageUrl).openConnection()
            connection.connect()
            var bitmap = BitmapFactory.decodeStream(connection.getInputStream())

            context.runOnUiThread {
                imageView.setImageBitmap(bitmap)
            }
        }.start()
    }
}