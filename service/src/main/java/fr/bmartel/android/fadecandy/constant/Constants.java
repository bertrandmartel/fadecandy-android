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
package fr.bmartel.android.fadecandy.constant;

import fr.bmartel.android.fadecandy.ServiceType;

/**
 * @author Bertrand Martel
 */
public class Constants {

    public final static int DEFAULT_SERVER_PORT = 7890;
    public final static String DEFAULT_SERVER_ADDRESS = "127.0.0.1";

    public final static ServiceType DEFAULT_SERVICE_TYPE = ServiceType.PERSISTENT_SERVICE;

    public final static int FC_VENDOR = 7504;
    public final static int FC_PRODUCT = 24698;

    public final static String CONFIG_LISTEN = "listen";
    public final static String CONFIG_VERBOSE = "verbose";
    public final static String CONFIG_COLOR = "color";
    public final static String CONFIG_DEVICES = "devices";
    public final static String CONFIG_GAMMA = "gamma";
    public final static String CONFIG_WHITEPOINT = "whitepoint";
    public final static String CONFIG_TYPE = "type";
    public final static String CONFIG_MAP = "map";

    public final static String PREFERENCE_PREFS = "prefs";
    public final static String PREFERENCE_CONFIG = "config";
    public final static String PREFERENCE_SERVICE_TYPE = "serviceType";
    public final static String PREFERENCE_PORT = "port";
    public final static String PREFERENCE_IP_ADDRESS = "ipAddress";

    public final static String SERVICE_EXTRA_ACTIVITY = "activity";

    public static final int MAX_LED_PER_STRIP = 64;
}
