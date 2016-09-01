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
package fr.bmartel.fadecandy.listener;

import fr.bmartel.android.fadecandy.model.UsbItem;

/**
 * Listener used for interface between activity & singleton.
 *
 * @author Bertrand Martel
 */
public interface ISingletonListener {

    /**
     * called when server is started.
     */
    void onServerStart();

    /**
     * called when server is closed.
     */
    void onServerClose();

    /**
     * called when a Fadecandy USB device is attached.
     *
     * @param usbItem
     */
    void onUsbDeviceAttached(UsbItem usbItem);

    /**
     * called when a Fadecandy USB device is detached.
     *
     * @param usbItem
     */
    void onUsbDeviceDetached(UsbItem usbItem);

}
