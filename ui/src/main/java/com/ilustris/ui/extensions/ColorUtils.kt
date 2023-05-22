package com.ilustris.ui.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import java.util.Collections


val ERROR_COLOR = com.github.mcginty.R.color.material_red500
val SUCCESS_COLOR = com.github.mcginty.R.color.material_green500
val WARNING_COLOR = com.github.mcginty.R.color.material_yellow500
val INFO_COLOR = com.github.mcginty.R.color.material_blue500

fun getColors(context: Context): List<String> {
    val colors = ArrayList<String>()
    try {
        val fields = Class.forName("com.github.mcginty" + ".R\$color").declaredFields
        fields.forEach {
            if (it.getInt(null) != Color.TRANSPARENT) {
                val colorId = it.getInt(null)
                val color = ContextCompat.getColor(context, colorId)
                colors.add(color.toHexCode())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("[Color Extensions]", "Error fetching colors ${e.message}")
    }
    return colors
}

fun Int.toHexCode(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}

fun Bitmap.getDominantColor(): Int? {
    val swatchesTemp: List<Palette.Swatch> = Palette.from(this).generate().swatches
    val swatches: List<Palette.Swatch> = ArrayList(swatchesTemp)
    Collections.sort(
        swatches
    ) { swatch1, swatch2 -> swatch2.population - swatch1.population }
    return swatches.firstOrNull()?.rgb
}

fun Int.lighten(fraction: Double): Int {
    var red = Color.red(this)
    var green = Color.green(this)
    var blue = Color.blue(this)
    red = lightenColor(red, fraction)
    green = lightenColor(green, fraction)
    blue = lightenColor(blue, fraction)
    val alpha = Color.alpha(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.darken(fraction: Double): Int {
    var red = Color.red(this)
    var green = Color.green(this)
    var blue = Color.blue(this)
    red = darkenColor(red, fraction)
    green = darkenColor(green, fraction)
    blue = darkenColor(blue, fraction)
    val alpha = Color.alpha(this)

    return Color.argb(alpha, red, green, blue)
}

fun darkenColor(color: Int, fraction: Double): Int {
    return (color - color * fraction).coerceAtLeast(0.0).toInt()
}

fun lightenColor(color: Int, fraction: Double): Int {
    return (color + color * fraction).coerceAtMost(255.0).toInt()
}

fun Int.setAlpha(ratio: Float) = Color.argb(
    Math.round(Color.alpha(this) * ratio),
    Color.red(this),
    Color.green(this),
    Color.blue(this)
)

fun getRandomMaterialColor(context: Context): String {
    val colors = getColors(context)
    return colors.random()
}


