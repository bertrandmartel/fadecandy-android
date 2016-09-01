/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Bertrand Martel
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.inter;

import android.content.Context;

import fr.bmartel.android.fadecandy.model.ServiceType;

/**
 * Activity interface.
 *
 * @author Bertrand Martel
 */
public interface IFc {

    /**
     * start the server if stopped / stop the server if started.
     */
    void switchServerStatus();

    /**
     * Retrieve Fadecandy service type (persistent or non persistent).
     *
     * @return
     */
    ServiceType getServiceType();

    /**
     * set Fadecandy service type.
     *
     * @param serviceType
     */
    void setServiceType(ServiceType serviceType);

    /**
     * get Android context.
     *
     * @return
     */
    Context getContext();

    /**
     * get Fadecandy server port.
     *
     * @return
     */
    int getServerPort();

    /**
     * get Fadecandy server address.
     *
     * @return
     */
    String getIpAddress();

    /**
     * set Fadecandy server port.
     *
     * @param port
     */
    void setServerPort(int port);

    /**
     * set Fadecandy server address.
     *
     * @param ip
     */
    void setServerAddress(String ip);

    /**
     * Retrieve led count (default or from shared prefs)
     *
     * @return
     */
    int getLedCount();

    /**
     * set led count
     *
     * @param ledCount
     */
    void setLedCount(int ledCount);

    /**
     * restart Fadecandy server.
     */
    void restartServer();

    /**
     * define if local Fadecandy server is used or if the server is a local server on LAN
     *
     * @return
     */
    boolean isServerMode();

    /**
     * set the server mode (Android if true, LAN if false)
     *
     * @param androidServerMode
     */
    void setServerMode(boolean androidServerMode);
}
