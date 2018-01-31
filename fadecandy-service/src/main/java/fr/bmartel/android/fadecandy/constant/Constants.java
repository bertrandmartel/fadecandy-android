/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016-2018 Bertrand Martel
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

import fr.bmartel.android.fadecandy.model.ServiceType;

/**
 * Fadecandy project constants
 *
 * @author Bertrand Martel
 */
public class Constants {

    /**
     * default server port.
     */
    public final static int DEFAULT_SERVER_PORT = 7890;

    /**
     * defaul server address.
     */
    public final static String DEFAULT_SERVER_ADDRESS = "127.0.0.1";

    /**
     * service is persistent by default.
     */
    public final static ServiceType DEFAULT_SERVICE_TYPE = ServiceType.PERSISTENT_SERVICE;

    /**
     * Fadecandy vendor id.
     */
    public final static int FC_VENDOR = 7504;

    /**
     * Fadecandy product id.
     */
    public final static int FC_PRODUCT = 24698;

    //shared preference field/variables
    public final static String PREFERENCE_PREFS = "prefs";
    public final static String PREFERENCE_CONFIG = "config";
    public final static String PREFERENCE_SERVICE_TYPE = "serviceType";
    public final static String PREFERENCE_PORT = "port";
    public final static String PREFERENCE_IP_ADDRESS = "ipAddress";

    /**
     * extra given to the intent in onStartCommand to match an activity to be launched when user click on notification.
     */
    public final static String SERVICE_EXTRA_ACTIVITY = "activity";

    /**
     * extra given to the intent in onStartCommand to override the service type.
     */
    public static final String SERVICE_EXTRA_SERVICE_TYPE = "serviceType";

    /**
     * number max of LED.
     */
    public static final int MAX_LED = 512;

}
