package fr.bmartel.fadecandy.ledcontrol;


import android.graphics.Color;

import com.github.akinaru.Animation;
import com.github.akinaru.OpcClient;
import com.github.akinaru.OpcDevice;
import com.github.akinaru.PixelStrip;

/**
 * Display a moving white pixel with trailing orange/red flames
 * This looks pretty good with a ring of pixels.
 */
public class Spark extends Animation {

    public static boolean CONTROL = false;

    int color[] = null;

    int currentPixel;
    int numPixels;
    private static int speed = 25;

    public Spark(int[] colors) {
        this.color = colors;
    }

    public static void setSpeed(int speed) {
        if (speed == 100) {
            Spark.speed = 5;
        } else {
            Spark.speed = 100 - speed;
        }
    }

    @Override
    public void reset(PixelStrip strip) {
        currentPixel = 0;
        numPixels = strip.getPixelCount();
    }

    @Override
    public boolean draw(PixelStrip strip) {
        for (int i = 0; i < color.length; i++) {
            strip.setPixelColor(pixNum(currentPixel, i), color[i]);
        }
        currentPixel = pixNum(currentPixel + 1, 0);
        return true;
    }

    /**
     * Return the pixel number that is i steps behind number p.
     */
    int pixNum(int p, int i) {
        return (p + numPixels - i) % numPixels;
    }

    public static void draw(String host, int port, int stripCount, int color) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadeCandy = server.addDevice();
        PixelStrip strip1 = fadeCandy.addPixelStrip(0, stripCount);

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int[] colors = {
                makeColor(red, green, blue),
                makeColor(red, green, blue),
                makeColor(red, green, blue),
                makeColor(0, 0, 0),
                makeColor(0, 0, 0),
                makeColor(0, 0, 0),
                makeColor(0, 0, 0)
        };

        strip1.setAnimation(new Spark(colors));

        for (int i = 0; i < 1000 && Spark.CONTROL; i++) {
            server.animate();
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        server.close();
    }
}