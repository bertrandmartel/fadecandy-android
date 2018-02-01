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
package fr.bmartel.fadecandy.constant

/**
 * Application constants.
 *
 * @author Bertrand Martel
 */
object AppConstants {

    /**
     * default number of led is one full strand.
     */
    const val DEFAULT_LED_COUNT = 64

    /**
     * default temperature color
     */
    const val DEFAULT_TEMPERATURE = 0xFFFFFF

    /**
     * default speed for spark animation.
     */
    const val DEFAULT_SPARK_SPEED = 60

    /**
     * preference led count field name.
     */
    const val PREFERENCE_FIELD_LEDCOUNT = "ledCount"

    /**
     * preference server mode field name.
     */
    const val PREFERENCE_FIELD_SERVER_MODE = "serverMode"

    /**
     * preference remote server port.
     */
    const val PREFERENCE_FIELD_REMOTE_SERVER_PORT = "remoteServerPort"

    /**
     * preference remote server IP.
     */
    const val PREFERENCE_FIELD_REMOTE_SERVER_IP = "remoteServerIp"

    /**
     * preferences for spark span.
     */
    const val PREFERENCE_FIELD_SPARK_SPAN = "sparkSpan"

    /**
     * preference for spark speed.
     */
    const val PREFERENCE_FIELD_SPARK_SPEED = "sparkSpeed"

    /**
     * preference for mixer delay.
     */
    const val PREFERENCE_FIELD_MIXER_DELAY = "mixerDelay"

    /**
     * preference pulse delay.
     */
    const val PREFERENCE_FIELD_PULSE_DELAY = "pulseDelay"

    /**
     * preference pulse pause.
     */
    const val PREFERENCE_FIELD_PULSE_PAUSE = "pulsePause"

    /**
     * preference temperature.
     */
    const val PREFERENCE_FIELD_TEMPERATURE = "temperature"

    /**
     * preference brightness.
     */
    const val PREFERENCE_FIELD_BRIGHTNESS = "brightness"

    /**
     * preference color.
     */
    const val PREFERENCE_FIELD_COLOR = "color"

    /**
     * default server mode is Android server mode.
     */
    const val DEFAULT_SERVER_MODE = true

    /**
     * default remote server ip.
     */
    const val DEFAULT_SERVER_IP = "127.0.0.1"

    /**
     * default remote server port.
     */
    const val DEFAULT_SERVER_PORT = 7890

    /**
     * default spark span.
     */
    const val DEFAULT_SPARK_SPAN = 5

    /**
     * default delay for mixer animation.
     */
    const val DEFAULT_MIXER_DELAY = 10

    /**
     * pulse animation default delay.
     */
    const val DEFAULT_PULSE_DELAY = 10

    /**
     * pulse animation default pause
     */
    const val DEFAULT_PULSE_PAUSE = 1000

    /**
     * socket timeout value for open pixel control.
     */
    const val SOCKET_TIMEOUT = 1000

    /**
     * socket connection timeout value for open pixel control.
     */
    const val SOCKET_CONNECTION_TIMEOUT = 1000

    /**
     * default gamma correction used in open pixel control operation.
     */
    const val DEFAULT_GAMMA_CORRECTION = 2.5f

    /**
     * websocket connection timeout.
     */
    const val WEBSOCKET_CONNECT_TIMEOUT = 1000

    /**
     * websocket disconnection timeout.
     */
    const val STOP_WEBSOCKET_TIMEOUT = 400

    /**
     * default brightness value.
     */
    const val DEFAULT_BRIGHTNESS = 100

    /**
     * default color value.
     */
    const val DEFAULT_COLOR = 0xFF0000
}
