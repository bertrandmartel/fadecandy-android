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
package fr.bmartel.fadecandy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.fadecandy.client.FadecandyClient;
import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.inter.IFadecandyListener;
import fr.bmartel.android.fadecandy.inter.IUsbListener;
import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.android.fadecandy.model.ServerError;
import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;
import fr.bmartel.fadecandy.listener.ISingletonListener;

/**
 * Singleton used to bind service and wrap around it.
 *
 * @author Bertrand Martel
 */
public class FadecandySingleton {

    /**
     * Singleton static instance.
     */
    private static FadecandySingleton mInstance;

    /**
     * an executor to launch task on a new thread.
     */
    private ExecutorService mExecutorService;

    /**
     * fadecandy client used to wrap Fadecandy service interface.
     */
    private FadecandyClient mFadecandyClient;

    /**
     * Android  application context.
     */
    private Context mContext;

    /**
     * list of listeners added by activities.
     */
    private List<ISingletonListener> mListeners = new ArrayList<>();

    private final static String TAG = FadecandySingleton.class.getSimpleName();

    /**
     * worker thread used for Led animations.
     */
    private Thread workerThread = null;

    /**
     * led animation control loop.
     */
    private boolean controlLoop = true;

    /**
     * led brightness current config.
     */
    private int mCurrentBrightness = -1;

    /**
     * led color current config.
     */
    private int mColor = -1;

    /**
     * spark speed current config.
     */
    private int mCurrentSpeed = -1;

    /**
     * led temperature current config.
     */
    private int mTemperature = -1;

    /**
     * default number of led is one full strand.
     */
    private final static int DEFAULT_LED_COUNT = 64;

    /**
     * default speed for spark animation.
     */
    public final static int DEFAULT_SPARK_SPEED = 60;

    /**
     * preference led count field name.
     */
    private final static String PREFERENCE_FIELD_LEDCOUNT = "ledCount";

    /**
     * current number of led.
     */
    private int mLedCount;

    /**
     * shared preferences.
     */
    private SharedPreferences prefs;

    /**
     * Get the static singleton instance.
     *
     * @param context Android application context.
     * @return singleton instance
     */
    public static FadecandySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FadecandySingleton(context);
        }
        return mInstance;
    }

    /**
     * add a singleton listener.
     *
     * @param listener
     */
    public void addListener(ISingletonListener listener) {
        mListeners.add(listener);
    }

    /**
     * remove a singleton listener.
     *
     * @param listener
     */
    public void removeListener(ISingletonListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Build the singleton.
     *
     * @param context
     */
    public FadecandySingleton(Context context) {

        prefs = context.getSharedPreferences(Constants.PREFERENCE_PREFS, Context.MODE_PRIVATE);
        mLedCount = prefs.getInt(PREFERENCE_FIELD_LEDCOUNT, DEFAULT_LED_COUNT);

        mExecutorService = Executors.newSingleThreadExecutor();

        mContext = context;

        //build Fadecandy client to bind Fadecandy service & start server

        mFadecandyClient = new FadecandyClient(mContext, new IFadecandyListener() {

            @Override
            public void onServerStart() {

                Log.v(TAG, "onServerStart");

                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerStart();
                }

            }

            @Override
            public void onServerClose() {

                Log.v(TAG, "onServerClose");

                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerClose();
                }
            }

            @Override
            public void onServerError(ServerError error) {

            }

        }, new IUsbListener() {
            @Override
            public void onUsbDeviceAttached(UsbItem usbItem) {
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onUsbDeviceAttached(usbItem);
                }
            }

            @Override
            public void onUsbDeviceDetached(UsbItem usbItem) {
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onUsbDeviceDetached(usbItem);
                }
            }
        }, "fr.bmartel.fadecandy/.activity.MainActivity");


        mFadecandyClient.startServer();
    }

    /**
     * Clear all led.
     */
    public void clearPixel() {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                checkJoinThread();
                ColorUtils.clear(getIpAddress(), getServerPort(), mLedCount);
            }
        });
    }

    /**
     * Wait for led animation thread to join.
     */
    public void checkJoinThread() {
        if (workerThread != null) {
            Spark.CONTROL = false;
            controlLoop = false;
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * set all pixel color.
     *
     * @param color
     */
    public void setFullColor(final int color) {

        mColor = color;

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                checkJoinThread();
                ColorUtils.setFullColor(getIpAddress(), getServerPort(), mLedCount, color);
            }
        });
    }

    /**
     * get current color config.
     *
     * @return
     */
    public int getColor() {
        return mColor;
    }

    /**
     * define if the server is running or not.
     *
     * @return
     */
    public boolean isServerRunning() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.isServerRunning();
        }
        return false;
    }

    /**
     * Retrieve service type (persistent or non persistent)
     *
     * @return
     */
    public ServiceType getServiceType() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getServiceType();
        }
        return null;
    }

    /**
     * Retrieve Fadecandy configuration.
     *
     * @return
     */
    public FadecandyConfig getConfig() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getConfig();
        }
        return null;
    }

    /**
     * set service type.
     *
     * @param serviceType
     */
    public void setServiceType(ServiceType serviceType) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServiceType(serviceType);
        }
    }

    /**
     * Retrieve server port.
     *
     * @return
     */
    public int getServerPort() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getServerPort();
        }
        return 0;
    }

    /**
     * Retrieve server address.
     *
     * @return
     */
    public String getIpAddress() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getIpAddress();
        }
        return "";
    }

    /**
     * Set server port.
     *
     * @param port
     */
    public void setServerPort(int port) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServerPort(port);
        }
    }

    /**
     * Set server address.
     *
     * @param ip
     */
    public void setServerAddress(String ip) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServerAddress(ip);
        }
    }

    /**
     * unbind service & unregister receiver.
     */
    public void disconnect() {
        if (mFadecandyClient != null) {
            mFadecandyClient.disconnect();
        }
    }

    /**
     * stop Fadecandy server.
     */
    public void closeServer() {
        if (mFadecandyClient != null) {
            mFadecandyClient.closeServer();
        }
    }

    /**
     * start Fadecandy server.
     */
    public void startServer() {
        if (mFadecandyClient != null) {
            mFadecandyClient.startServer();
        }
    }

    /**
     * bind service & register receiver.
     */
    public void connect() {
        if (mFadecandyClient != null) {
            mFadecandyClient.connect();
        }
    }

    /**
     * Retrieve current brightness.
     *
     * @return
     */
    public int getCurrentColorCorrection() {
        return mCurrentBrightness;
    }

    /**
     * set brightness (same color correction for RGB)
     *
     * @param value
     */
    public void setColorCorrection(final int value) {

        mCurrentBrightness = value;

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                checkJoinThread();
                ColorUtils.setBrightness(getIpAddress(), getServerPort(), (value / 100f));
            }
        });
    }

    /**
     * set color correction during spark animation.
     *
     * @param value
     */
    public void setColorCorrectionSpark(final int value) {

        mCurrentBrightness = value;

        Spark.COLOR_CORRECTION = (value / 100f);
    }

    /**
     * start Spark animation
     *
     * @param color color to be used when running spark animation
     */
    public void spark(final int color) {

        mColor = color;

        checkJoinThread();
        Spark.CONTROL = true;
        controlLoop = true;
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (controlLoop) {
                    Spark.draw(getIpAddress(), getServerPort(), mLedCount, color);
                }
            }
        });
        workerThread.start();
    }

    /**
     * Set spark animation speed.
     *
     * @param speed
     */
    public void setSpeed(int speed) {

        mCurrentSpeed = speed;

        Spark.setSpeed(speed);
    }

    /**
     * Retrieve spark animation speed.
     *
     * @return
     */
    public int getSpeed() {
        return mCurrentSpeed;
    }

    /**
     * Set led temperature value.
     *
     * @param temperature
     */
    public void setTemperature(int temperature) {
        mTemperature = temperature;
        setFullColor(temperature);
    }

    /**
     * Retrieve current value of led temperature.
     *
     * @return
     */
    public int getTemperature() {
        return mTemperature;
    }

    /**
     * Retrieve list of Fadecandy USB device attached.
     *
     * @return
     */
    public HashMap<Integer, UsbItem> getUsbDevices() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getUsbDeviceMap();
        }
        return new HashMap<>();
    }

    /**
     * Retrieve led count
     *
     * @return
     */
    public int getLedCount() {
        return mLedCount;
    }

    /**
     * Set led count.
     *
     * @param ledCount
     */
    public void setLedCount(int ledCount) {
        this.mLedCount = ledCount;
        prefs.edit().putInt(PREFERENCE_FIELD_LEDCOUNT, mLedCount).apply();
    }

    /**
     * Restart Fadecandy Server.
     */
    public void restartServer() {

        if (mFadecandyClient != null) {
            mFadecandyClient.startServer();
        }
    }

}
