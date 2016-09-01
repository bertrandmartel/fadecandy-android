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
package fr.bmartel.fadecandy.ledcontrol;

import com.github.akinaru.OpcClient;
import com.github.akinaru.OpcDevice;
import com.github.akinaru.PixelStrip;

/**
 * open pixel control functions.
 *
 * @author Bertrand Martel
 */
public class ColorUtils {

    /**
     * Set full color for all led
     *
     * @param host       server address
     * @param port       server port
     * @param stripCount number of led to be set
     * @param color      color to set
     */
    public static void setFullColor(String host, int port, int stripCount, int color) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        for (int i = 0; i < stripCount; i++) {
            strip.setPixelColor(i, color);
        }
        server.show();        // Display the pixel changes
        server.close();
    }

    /**
     * set brightness (color correction) : will set the same correction for R, G & B
     *
     * @param host       server address
     * @param port       server port
     * @param correction color correction
     */
    public static void setBrightness(String host, int port, float correction) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        fadecandy.setColorCorrection(2.5f, correction, correction, correction);
        server.show();
        server.close();
    }

    /**
     * clear all led
     *
     * @param host       server address.
     * @param port       server port.
     * @param stripCount number of led to clear.
     */
    public static void clear(String host, int port, int stripCount) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        strip.clear();
        server.show();        // Display the pixel changes
        server.close();
    }
}
