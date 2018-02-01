/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2016-2018 Bertrand Martel
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.ledcontrol

import android.graphics.Color

import fr.bmartel.fadecandy.FadecandySingleton
import fr.bmartel.fadecandy.constant.AppConstants
import fr.bmartel.opc.OpcClient
import fr.bmartel.opc.OpcDevice
import fr.bmartel.opc.PixelStrip

/**
 * open pixel control functions.
 *
 * @author Bertrand Martel
 */
object ColorUtils {

    /**
     * Set full color for all led
     */
    fun setFullColor(singleton: FadecandySingleton): Int? {
        for (i in 0 until singleton.ledCount) {
            singleton.pixelStrip?.setPixelColor(i, singleton.color)
        }
        return singleton.opcClient?.show()
    }

    /**
     * set brightness (color correction) : will set the same correction for R, G & B
     */
    fun setBrightness(singleton: FadecandySingleton): Int? {
        singleton.opcDevice?.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION,
                singleton.currentColorCorrection / 100f,
                singleton.currentColorCorrection / 100f,
                singleton.currentColorCorrection / 100f)
        return singleton.opcClient?.show()
    }

    /**
     * clear all led
     */
    fun clear(singleton: FadecandySingleton): Int? {
        singleton.pixelStrip?.clear()
        return singleton.opcClient?.show()
    }

    /**
     * change colors for mixing effect depending on which color will be changed while other will remain constants
     *
     * @param descending    define if we want to increase/decrease the @selectedColor
     * @param stripCount    led count
     * @param strip         pixel strip object
     * @param r             initial value for red
     * @param g             initial value for green
     * @param b             initial value for blue
     * @param selectedColor color that will be increased/decreased while the 2 others will remain constants (0 : r | 1 : g | 2 : b)
     * @param singleton     singleton for vars
     * @param server        open pixel control server
     */
    private fun mix(descending: Boolean, stripCount: Int, strip: PixelStrip?, r: Int, g: Int, b: Int, selectedColor: Byte, singleton: FadecandySingleton, server: OpcClient?): Int {
        var color: Int = if (descending) {
            255
        } else {
            0
        }
        for (j in 0..254) {
            for (i in 0 until stripCount) when (selectedColor.toInt()) {
                0 -> strip?.setPixelColor(i, Color.rgb(color, g, b) and 0x00FFFFFF)
                1 -> strip?.setPixelColor(i, Color.rgb(r, color, b) and 0x00FFFFFF)
                2 -> strip?.setPixelColor(i, Color.rgb(r, g, color) and 0x00FFFFFF)
            }

            val status = server?.show()
            if (status == -1) {
                return -1
            }

            if (descending) {
                color--
            } else {
                color++
            }
            if (singleton.isAnimating) {
                try {
                    Thread.sleep(singleton.mixerDelay.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            } else {
                return 0
            }
        }
        return 0
    }

    /**
     * increase/decrease green while red & blue remain constant
     *
     * @param descending define if we want to increase/decrease green
     * @param stripCount led count
     * @param strip      pixel strip object
     * @param initRed    red color constant
     * @param initBlue   blue color constant
     * @param singleton  singleton object  for vars
     * @param server     open pixel control server object
     */
    private fun mixGreen(descending: Boolean, stripCount: Int, strip: PixelStrip?, initRed: Int, initBlue: Int, singleton: FadecandySingleton, server: OpcClient?): Int {
        return mix(descending, stripCount, strip, initRed, 0, initBlue, 0x01.toByte(), singleton, server)
    }

    /**
     * increase/decrease blue while red & green remain constant
     *
     * @param descending define if we want to increase/decrease blue
     * @param stripCount led count
     * @param strip      pixel strip object
     * @param initRed    red color constant
     * @param initGreen  green color constant
     * @param singleton  singleton object  for vars
     * @param server     open pixel control server object
     */
    private fun mixBlue(descending: Boolean, stripCount: Int, strip: PixelStrip?, initRed: Int, initGreen: Int, singleton: FadecandySingleton, server: OpcClient?): Int {
        return mix(descending, stripCount, strip, initRed, initGreen, 0, 0x02.toByte(), singleton, server)
    }

    /**
     * increase/decrease red while green & blue remain constant
     *
     * @param descending define if we want to increase/decrease red
     * @param stripCount led count
     * @param strip      pixel strip object
     * @param initGreen  green color constant
     * @param initBlue   blue color constant
     * @param singleton  singleton object  for vars
     * @param server     open pixel control server object
     */
    private fun mixRed(descending: Boolean, stripCount: Int, strip: PixelStrip?, initGreen: Int, initBlue: Int, singleton: FadecandySingleton, server: OpcClient?): Int {
        return mix(descending, stripCount, strip, 0, initGreen, initBlue, 0x00.toByte(), singleton, server)
    }

    /**
     * create a mixer effect.
     *
     * @param singleton singleton for vars
     */
    fun mixer(singleton: FadecandySingleton): Int {
        var status: Int
        val ledCount = singleton.ledCount

        while (singleton.isAnimating) {
            status = mixGreen(true, ledCount, singleton.pixelStrip, 255, 0, singleton, singleton.opcClient)
            if (status == -1) {
                return -1
            }
            status = mixBlue(false, ledCount, singleton.pixelStrip, 255, 0, singleton, singleton.opcClient)
            if (status == -1) {
                return -1
            }
            status = mixRed(true, ledCount, singleton.pixelStrip, 0, 255, singleton, singleton.opcClient)
            if (status == -1) {
                return -1
            }
            status = mixGreen(false, ledCount, singleton.pixelStrip, 0, 255, singleton, singleton.opcClient)
            if (status == -1) {
                return -1
            }
            status = mixBlue(true, ledCount, singleton.pixelStrip, 0, 255, singleton, singleton.opcClient)
            if (status == -1) {
                return -1
            }
            status = mixRed(false, ledCount, singleton.pixelStrip, 255, 0, singleton, singleton.opcClient)
            if (status == -1) {
                return -1
            }
        }
        return 0
    }

    /**
     * create a pulse effect
     */
    fun pulse(singleton: FadecandySingleton): Int {
        var status: Int?
        val ledCount = singleton.ledCount
        if (singleton.isAnimating) {
            singleton.opcDevice?.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, 0.0f, 0.0f, 0.0f)
            status = singleton.opcClient?.show()
            if (status == -1) {
                return -1
            }
        }

        while (singleton.isAnimating) {
            for (i in 0 until ledCount) {
                singleton.pixelStrip?.setPixelColor(i, singleton.color)
            }

            status = graduateColorCorrection(true, singleton.opcClient, singleton.opcDevice, singleton)
            if (status == -1) {
                return -1
            } else if (status == 1) {
                return 0
            }

            status = graduateColorCorrection(false, singleton.opcClient, singleton.opcDevice, singleton)
            if (status == -1) {
                return -1
            } else if (status == 1) {
                return 0
            }

            if (singleton.isAnimating) {
                try {
                    Thread.sleep(singleton.pulsePause.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            } else {
                return 0
            }
        }
        return 0
    }

    /**
     * increase/decrease color correction for a pulse effect.
     *
     * @param ascending increase if true / decrease if false
     * @param server    open pixel control server
     * @param fadecandy fadecandy device
     * @param singleton singleton to get vars
     * @return true if continue / false to stop
     */
    private fun graduateColorCorrection(ascending: Boolean,
                                        server: OpcClient?,
                                        fadecandy: OpcDevice?,
                                        singleton: FadecandySingleton): Int {
        var bright = 0f
        if (!ascending) {
            bright = 1f
        }

        for (i in 0..9) {
            fadecandy?.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, bright, bright, bright)
            val status = server?.show()

            if (status == -1) {
                return -1
            }

            if (ascending) {
                bright += 0.1f
            } else {
                bright -= 0.1f
            }

            if (singleton.isAnimating) {
                try {
                    Thread.sleep(singleton.pulseDelay.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            } else {
                server?.close()
                return 1
            }
        }
        if (ascending) {
            fadecandy?.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, 1f, 1f, 1f)
        } else {
            fadecandy?.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, 0f, 0f, 0f)
        }
        server?.show()
        return 0
    }
}
