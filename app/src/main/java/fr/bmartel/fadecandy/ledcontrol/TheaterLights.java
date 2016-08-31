package fr.bmartel.fadecandy.ledcontrol;

import com.github.akinaru.Animation;
import com.github.akinaru.OpcClient;
import com.github.akinaru.OpcDevice;
import com.github.akinaru.PixelStrip;

/**
 * Pairs of lights traveling down the strip.
 */
public class TheaterLights extends Animation {

    protected static final int FAST = 20; // twenty pixels per second
    protected static final int SLOW = 1000; // one pixel per second

    int N = 2;
    int state;
    long timePerCycle = 100L;
    int color;

    /**
     * Time for the next state change.
     */
    long changeTime;

    public TheaterLights(int c) {
        color = c;
    }

    @Override
    public void reset(PixelStrip strip) {
        state = 0;
        changeTime = millis();
    }

    @Override
    public boolean draw(PixelStrip strip) {
        if (millis() < changeTime) {
            return false;
        }

        state = (state + 1) % (N * 2);
        for (int i = 0; i < strip.getPixelCount(); i++) {
            int j = (i + state) % (N * 2);
            strip.setPixelColor(i, j >= N ? color : BLACK);
        }

        changeTime = millis() + timePerCycle;
        return true;
    }

    /**
     * @param n value between -1.0 and 1.0;
     */
    @Override
    public void setValue(double n) {
        timePerCycle = Math.round(SLOW - (SLOW - FAST) * Math.abs(n));
        timePerCycle = Math.min(Math.max(FAST, timePerCycle), SLOW);
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // This is just test code.  Write your own code that sets up your pixel configuratio,
    // animation combinations, and interactions.

    public static void draw(String host, int port, int stripCount) {

        OpcClient server = new OpcClient(host, port);
        OpcDevice fadeCandy = server.addDevice();
        PixelStrip strip1 = fadeCandy.addPixelStrip(0, stripCount);
        System.out.println(server.getConfig());

        TheaterLights a = new TheaterLights(0x0000DD);
        a.setValue(0.8);
        strip1.setAnimation(a);

        for (int i = 0; i < 1000; i++) {
            server.animate();
            try {
                Thread.sleep(FAST / 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        strip1.clear();
        server.show();
        server.close();
    }

}