package fr.bmartel.fadecandy.ledcontrol;


import android.graphics.Color;

import fr.bmartel.opc.Animation;
import fr.bmartel.opc.PixelStrip;

/**
 * Display a moving white pixel with trailing orange/red flames
 * This looks pretty good with a ring of pixels.
 */
public class Spark extends Animation {

    int color[] = null;

    int currentPixel;
    int numPixels;

    public Spark(int[] colors) {
        this.color = colors;
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

    public static int convertSpeed(int speed) {
        if (speed == 100) {
            return 5;
        } else {
            return (100 - speed);
        }
    }

    /**
     * Return the pixel number that is i steps behind number p.
     */
    int pixNum(int p, int i) {
        return (p + numPixels - i) % numPixels;
    }

    public static int[] buildColors(int color, int sparkSpan) {
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

    public void updateColor(int mColor, int sparkSpan) {
        color = buildColors(mColor, sparkSpan);
    }
}