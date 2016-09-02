package fr.bmartel.fadecandy.ledcontrol;


import android.graphics.Color;
import android.util.Log;

import com.github.akinaru.Animation;
import com.github.akinaru.OpcClient;
import com.github.akinaru.OpcDevice;
import com.github.akinaru.PixelStrip;

/**
 * Display a moving white pixel with trailing orange/red flames
 * This looks pretty good with a ring of pixels.
 */
public class Spark extends Animation {

    private static final String TAG = Spark.class.getSimpleName();
    public static int SPAN = -1;

    public static float COLOR_CORRECTION = -1f;
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

    public static void draw(String host, int port, int stripCount, int color, int sparkSpan) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadeCandy = server.addDevice();
        PixelStrip strip = fadeCandy.addPixelStrip(0, stripCount);

        strip.setAnimation(new Spark(buildColors(color, sparkSpan)));

        while (Spark.CONTROL) {

            server.animate();

            if (COLOR_CORRECTION != -1) {
                fadeCandy.setColorCorrection(2.5f, COLOR_CORRECTION, COLOR_CORRECTION, COLOR_CORRECTION);
                COLOR_CORRECTION = -1;
            }

            if (SPAN != -1) {
                sparkSpan = SPAN;
                Log.i(TAG, "setAnimation2" + sparkSpan);
                strip.clear();
                strip.setAnimation(new Spark(buildColors(color, sparkSpan)));
                SPAN = -1;
            }

            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        server.close();
    }

    private static int[] buildColors(int color, int sparkSpan) {

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int fullColor = makeColor(red, green, blue);
        int noColor = makeColor(0, 0, 0);

        int[] colors = new int[sparkSpan + 1];

        for (int i = 0; i < colors.length - 1; i++) {
            colors[i] = fullColor;
        }
        colors[colors.length - 1] = noColor;

        return colors;
    }
}