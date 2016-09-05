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
import fr.bmartel.fadecandy.constant.AppConstants;

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
    public static int setFullColor(String host, int port, int stripCount, int color) {

        OpcClient server = new OpcClient(host, port);
        server.setSoTimeout(AppConstants.SOCKET_TIMEOUT);
        server.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT);

        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        for (int i = 0; i < stripCount; i++) {
            strip.setPixelColor(i, color);
        }

        int status = server.show();        // Display the pixel changes
        server.close();

        return status;
    }

    /**
     * set brightness (color correction) : will set the same correction for R, G & B
     *
     * @param host       server address
     * @param port       server port
     * @param correction color correction
     */
    public static int setBrightness(String host, int port, float correction) {

        OpcClient server = new OpcClient(host, port);
        server.setSoTimeout(AppConstants.SOCKET_TIMEOUT);
        server.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT);
        OpcDevice fadecandy = server.addDevice();
        fadecandy.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, correction, correction, correction);

        int status = server.show();
        server.close();

        return status;
    }

    /**
     * clear all led
     *
     * @param host       server address.
     * @param port       server port.
     * @param stripCount number of led to clear.
     */
    public static int clear(String host, int port, int stripCount) {

        OpcClient server = new OpcClient(host, port);
        server.setSoTimeout(AppConstants.SOCKET_TIMEOUT);
        server.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        strip.clear();
        int status = server.show();        // Display the pixel changes
        server.close();

        return status;
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
    private static int mix(boolean descending, int stripCount, PixelStrip strip, int r, int g, int b, byte selectedColor, FadecandySingleton singleton, OpcClient server) {

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

            int status = server.show();
            if (status == -1) {
                return -1;
            }

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
                return 0;
            }
        }

        return 0;
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
    private static int mixGreen(boolean descending, int stripCount, PixelStrip strip, int initRed, int initBlue, FadecandySingleton singleton, OpcClient server) {

        return mix(descending, stripCount, strip, initRed, 0, initBlue, (byte) 0x01, singleton, server);

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
    private static int mixBlue(boolean descending, int stripCount, PixelStrip strip, int initRed, int initGreen, FadecandySingleton singleton, OpcClient server) {

        return mix(descending, stripCount, strip, initRed, initGreen, 0, (byte) 0x02, singleton, server);

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
    private static int mixRed(boolean descending, int stripCount, PixelStrip strip, int initGreen, int initBlue, FadecandySingleton singleton, OpcClient server) {

        return mix(descending, stripCount, strip, 0, initGreen, initBlue, (byte) 0x00, singleton, server);

    }

    /**
     * create a mixer effect.
     *
     * @param host       open pixel control server address
     * @param port       open pixel control server port
     * @param stripCount led count
     * @param singleton  singleton for vars
     */
    public static int mixer(String host, int port, int stripCount, FadecandySingleton singleton) {

        OpcClient server = new OpcClient(host, port);
        server.setSoTimeout(AppConstants.SOCKET_TIMEOUT);
        server.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);
        int status = 0;

        while (singleton.isAnimating()) {

            status = mixGreen(true, stripCount, strip, 255, 0, singleton, server);
            if (status == -1) {
                return -1;
            }
            status = mixBlue(false, stripCount, strip, 255, 0, singleton, server);
            if (status == -1) {
                return -1;
            }
            status = mixRed(true, stripCount, strip, 0, 255, singleton, server);
            if (status == -1) {
                return -1;
            }
            status = mixGreen(false, stripCount, strip, 0, 255, singleton, server);
            if (status == -1) {
                return -1;
            }
            status = mixBlue(true, stripCount, strip, 0, 255, singleton, server);
            if (status == -1) {
                return -1;
            }
            status = mixRed(false, stripCount, strip, 255, 0, singleton, server);
            if (status == -1) {
                return -1;
            }
        }

        server.close();

        return 0;
    }

    /**
     * create a pulse effect
     *
     * @param host       open pixel control server address
     * @param port       open pixel control server port
     * @param stripCount led count  (0-512)
     * @param singleton  singleton to get vars
     */
    public static int pulse(String host, int port, int stripCount, FadecandySingleton singleton) {

        OpcClient server = new OpcClient(host, port);
        server.setSoTimeout(AppConstants.SOCKET_TIMEOUT);
        server.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        int status = 0;

        if (singleton.isAnimating()) {
            fadecandy.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, 0.0f, 0.0f, 0.0f);
            status = server.show();
            if (status == -1) {
                return -1;
            }
        }

        while (singleton.isAnimating()) {

            for (int i = 0; i < stripCount; i++) {
                strip.setPixelColor(i, singleton.getColor());
            }

            status = graduateColorCorrection(true, server, fadecandy, singleton);
            if (status == -1) {
                return -1;
            } else if (status == 1) {
                return 0;
            }

            status = graduateColorCorrection(false, server, fadecandy, singleton);
            if (status == -1) {
                return -1;
            } else if (status == 1) {
                return 0;
            }

            if (singleton.isAnimating()) {
                try {
                    Thread.sleep(singleton.getPulsePause());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                server.close();
                return 0;
            }
        }

        server.close();
        return 0;
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
    private static int graduateColorCorrection(boolean ascending,
                                               OpcClient server,
                                               OpcDevice fadecandy,
                                               FadecandySingleton singleton) {

        float bright = 0f;

        if (!ascending) {
            bright = 1f;
        }

        for (int i = 0; i < 10; i++) {

            fadecandy.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, bright, bright, bright);
            int status = server.show();

            if (status == -1) {
                return -1;
            }

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
                return 1;
            }
        }

        if (ascending) {
            fadecandy.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, 1, 1, 1);
        } else {
            fadecandy.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION, 0, 0, 0);
        }

        server.show();

        return 0;
    }
}
