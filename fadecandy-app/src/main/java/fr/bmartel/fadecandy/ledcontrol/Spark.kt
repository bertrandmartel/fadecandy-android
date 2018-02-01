package fr.bmartel.fadecandy.ledcontrol

import android.graphics.Color

import fr.bmartel.opc.Animation
import fr.bmartel.opc.PixelStrip

/**
 * Display a moving white pixel with trailing orange/red flames
 * This looks pretty good with a ring of pixels.
 */
class Spark(colors: IntArray) : Animation() {

    var color: IntArray
    private var currentPixel: Int = 0
    private var numPixels: Int = 0

    init {
        this.color = colors
    }

    override fun reset(strip: PixelStrip) {
        currentPixel = 0
        numPixels = strip.pixelCount
    }

    override fun draw(strip: PixelStrip): Boolean {
        for (i in color.indices) {
            strip.setPixelColor(pixNum(currentPixel, i), color[i])
        }

        currentPixel = pixNum(currentPixel + 1, 0)
        return true
    }

    /**
     * Return the pixel number that is i steps behind number p.
     */
    private fun pixNum(p: Int, i: Int): Int {
        return (p + numPixels - i) % numPixels
    }

    fun updateColor(mColor: Int, sparkSpan: Int) {
        color = buildColors(mColor, sparkSpan)
    }

    companion object {

        fun convertSpeed(speed: Int): Int {
            return if (speed == 100) {
                5
            } else {
                100 - speed
            }
        }

        fun buildColors(color: Int, sparkSpan: Int): IntArray {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val fullColor = Animation.makeColor(red, green, blue)
            val noColor = Animation.makeColor(0, 0, 0)

            val colors = IntArray(sparkSpan + 1)

            for (i in 0 until colors.size - 1) {
                colors[i] = fullColor
            }
            colors[colors.size - 1] = noColor

            return colors
        }
    }
}