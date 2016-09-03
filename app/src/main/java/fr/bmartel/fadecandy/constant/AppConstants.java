/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.constant;

/**
 * Application constants.
 *
 * @author Bertrand Martel
 */
public class AppConstants {

    /**
     * default number of led is one full strand.
     */
    public final static int DEFAULT_LED_COUNT = 64;

    /**
     * default temperature color
     */
    public final static int DEFAULT_TEMPERATURE = 0xFFFFFF;

    /**
     * default speed for spark animation.
     */
    public final static int DEFAULT_SPARK_SPEED = 60;

    /**
     * preference led count field name.
     */
    public final static String PREFERENCE_FIELD_LEDCOUNT = "ledCount";

    /**
     * preference server mode field name.
     */
    public final static String PREFERENCE_FIELD_SERVER_MODE = "serverMode";

    /**
     * preference remote server port.
     */
    public final static String PREFERENCE_FIELD_REMOTE_SERVER_PORT = "remoteServerPort";

    /**
     * preference remote server IP.
     */
    public final static String PREFERENCE_FIELD_REMOTE_SERVER_IP = "remoteServerIp";

    /**
     * preferences for spark span.
     */
    public final static String PREFERENCE_FIELD_SPARK_SPAN = "sparkSpan";

    /**
     * preference for spark speed.
     */
    public final static String PREFERENCE_FIELD_SPARK_SPEED = "sparkSpeed";

    /**
     * preference for mixer delay.
     */
    public final static String PREFERENCE_FIELD_MIXER_DELAY = "mixerDelay";

    /**
     * preference pulse delay.
     */
    public final static String PREFERENCE_FIELD_PULSE_DELAY = "pulseDelay";

    /**
     * preference pulse pause.
     */
    public final static String PREFERENCE_FIELD_PULSE_PAUSE = "pulsePause";

    /**
     * preference temperature.
     */
    public final static String PREFERENCE_FIELD_TEMPERATURE = "temperature";


    /**
     * default server mode is Android server mode.
     */
    public final static boolean DEFAULT_SERVER_MODE = true;

    /**
     * default remote server ip.
     */
    public final static String DEFAULT_SERVER_IP = "127.0.0.1";

    /**
     * default remote server port.
     */
    public final static int DEFAULT_SERVER_PORT = 7890;

    /**
     * default spark span.
     */
    public final static int DEFAULT_SPARK_SPAN = 5;

    /**
     * default delay for mixer animation.
     */
    public final static int DEFAULT_MIXER_DELAY = 10;

    /**
     * pulse animation default delay.
     */
    public final static int DEFAULT_PULSE_DELAY = 10;

    /**
     * pulse animation default pause
     */
    public final static int DEFAULT_PULSE_PAUSE = 1000;

    /**
     * socket timeout value for open pixel control.
     */
    public final static int SOCKET_TIMEOUT = 1000;

    /**
     * socket connection timeout value for open pixel control.
     */
    public final static int SOCKET_CONNECTION_TIMEOUT = 1000;

    /**
     * default gamma correction used in open pixel control operation.
     */
    public final static float DEFAULT_GAMMA_CORRECTION = 2.5f;
}
