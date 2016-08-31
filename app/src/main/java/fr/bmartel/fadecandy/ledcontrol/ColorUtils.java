package fr.bmartel.fadecandy.ledcontrol;

import com.github.akinaru.OpcClient;
import com.github.akinaru.OpcDevice;
import com.github.akinaru.PixelStrip;

/**
 * @author Bertrand Martel
 */
public class ColorUtils {

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

    public static void setBrightness(String host, int port, float correction) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        fadecandy.setColorCorrection(2.5f, correction, correction, correction);
        server.show();
        server.close();
    }


    public static void clear(String host, int port, int stripCount) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadecandy = server.addDevice();
        PixelStrip strip = fadecandy.addPixelStrip(0, stripCount);

        strip.clear();
        server.show();        // Display the pixel changes
        server.close();
    }
}
