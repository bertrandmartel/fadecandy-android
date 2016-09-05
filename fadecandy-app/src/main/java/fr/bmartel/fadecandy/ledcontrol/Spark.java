package fr.bmartel.fadecandy.ledcontrol;


import android.graphics.Color;

import com.github.akinaru.Animation;
import com.github.akinaru.PixelStrip;

import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.constant.AppConstants;

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

    public static int draw(FadecandySingleton singleton) {

        singleton.getPixelStrip().setAnimation(new Spark(buildColors(singleton.getColor(), singleton.getSparkSpan())));

        int status = 0;

        singleton.setSpanUpdate(false);
        singleton.setmIsSparkColorUpdate(false);

        while (singleton.isAnimating()) {

            status = singleton.getOpcClient().animate();
            if (status == -1) {
                return -1;
            }

            if (singleton.isBrightnessUpdate()) {

                status = singleton.getOpcDevice().setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION,
                        singleton.getCurrentColorCorrection() / 100f,
                        singleton.getCurrentColorCorrection() / 100f,
                        singleton.getCurrentColorCorrection() / 100f);

                if (status == -1) {

                    return -1;
                }
                singleton.setBrightnessUpdate(false);
            }

            if (singleton.isSpanUpdate() || singleton.ismIsSparkColorUpdate()) {
                singleton.getPixelStrip().clear();
                singleton.getPixelStrip().setAnimation(new Spark(buildColors(singleton.getColor(), singleton.getSparkSpan())));
                singleton.setSpanUpdate(false);
                singleton.setmIsSparkColorUpdate(false);
            }

            try {
                Thread.sleep(convertSpeed(singleton.getSpeed()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return 0;
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