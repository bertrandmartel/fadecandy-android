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

import android.graphics.Color;

import com.github.akinaru.OpcClient;
import com.github.akinaru.OpcDevice;
import com.github.akinaru.PixelStrip;

import fr.bmartel.fadecandy.FadecandySingleton;

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
    private static void mix(boolean descending, int stripCount, PixelStrip strip, int r, int g, int b, byte selectedColor, FadecandySingleton singleton, OpcClient server) {

        int color;

        if (descending) {
            color = 255;
        } else {
            color = 0;
        }

        for (int j = 0; j < 255; j++) {

            for (int i = 0; i < stripCount; i++) {
                switch (selectedColor) {
                    case 0: {
                        strip.setPixelColor(i, Color.rgb(color, g, b) & 0x00FFFFFF);
                        break;
                    }
                    case 1: {
                        strip.setPixelColor(i, Color.rgb(r, color, b) & 0x00FFFFFF);
                        break;
                    }
                    case 2: {
                        strip.setPixelColor(i, Color.rgb(r, g, color) & 0x00FFFFFF);
                        break;
                    }
                }
            }
            server.show();
            if (descending) {
                color--;
            } else {
                color++;
            }
            if (singleton.isAnimating()) {
                try {
                    Thread.sleep(singleton.getMixerDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                return;
            }
        }

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
    private static void mixGreen(boolean descending, int stripCount, PixelStrip strip, int initRed, int initBlue, FadecandySingleton singleton, OpcClient server) {

        mix(descending, stripCount, strip, initRed, 0, initBlue, (byte) 0x01, singleton, server);

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
    private static void mixBlue(boolean descending, int stripCount, PixelStrip strip, int initRed, int initGreen, FadecandySingleton singleton, OpcClient server) {

        mix(descending, stripCount, strip, initRed, initGreen, 0, (byte) 0x02, singleton, server);

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
    private static void mixRed(boolean descending, int stripCount, PixelStrip strip, int initGreen, int initBlue, FadecandySingleton singleton, OpcClient server) {

        mix(descending, stripCount, strip, 0, initGreen, initBlue, (byte) 0x00, singleton, server);

    }

    /**
     * create a mixer effect.
     *
     * @param host       open pixel control server address
     * @param port       open pixel control server port
     * @param stripCount led count
     * @param delay      delay between each color change
     * @param singleton  singleton for vars
     */
    public static void mixer(String host, int port, int stripCount, int delay, FadecandySingleton singleton) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        while (singleton.isAnimating()) {

            mixGreen(true, stripCount, strip, 255, 0, singleton, server);
            mixBlue(false, stripCount, strip, 255, 0, singleton, server);
            mixRed(true, stripCount, strip, 0, 255, singleton, server);
            mixGreen(false, stripCount, strip, 0, 255, singleton, server);
            mixBlue(true, stripCount, strip, 0, 255, singleton, server);
            mixRed(false, stripCount, strip, 255, 0, singleton, server);
        }

        server.close();
    }

    /**
     * create a pulse effect
     *
     * @param host       open pixel control server address
     * @param port       open pixel control server port
     * @param stripCount led count  (0-512)
     * @param singleton  singleton to get vars
     */
    public static void pulse(String host, int port, int stripCount, FadecandySingleton singleton) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        if (singleton.isAnimating()) {
            fadecandy.setColorCorrection(2.5f, 0.0f, 0.0f, 0.0f);
            server.show();
        }

        while (singleton.isAnimating()) {

            for (int i = 0; i < stripCount; i++) {
                strip.setPixelColor(i, singleton.getColor());
            }

            if (!graduateColorCorrection(true, server, fadecandy, singleton)) {
                return;
            }
            if (!graduateColorCorrection(false, server, fadecandy, singleton)) {
                return;
            }

            if (singleton.isAnimating()) {
                try {
                    Thread.sleep(singleton.getPulsePause());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                server.close();
                return;
            }
        }

        server.close();
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
    private static boolean graduateColorCorrection(boolean ascending,
                                                   OpcClient server,
                                                   OpcDevice fadecandy,
                                                   FadecandySingleton singleton) {

        float bright = 0f;

        if (!ascending) {
            bright = 1f;
        }

        for (int i = 0; i < 10; i++) {

            fadecandy.setColorCorrection(2.5f, bright, bright, bright);
            server.show();

            if (ascending) {
                bright += 0.1;
            } else {
                bright -= 0.1;
            }

            if (singleton.isAnimating()) {
                try {
                    Thread.sleep(singleton.getPulseDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                server.close();
                return false;
            }
        }

        if (ascending) {
            fadecandy.setColorCorrection(2.5f, 1, 1, 1);
        } else {
            fadecandy.setColorCorrection(2.5f, 0, 0, 0);
        }

        server.show();

        return true;
    }
}
