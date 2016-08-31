package fr.bmartel.fadecandy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.fadecandy.FadecandyClient;
import fr.bmartel.android.fadecandy.IFadecandyListener;
import fr.bmartel.android.fadecandy.ServerError;
import fr.bmartel.android.fadecandy.ServiceType;
import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.inter.IUsbListener;
import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;
import fr.bmartel.fadecandy.listener.ISingletonListener;

/**
 * Created by akinaru on 31/08/16.
 */
public class FadecandySingleton {

    private static FadecandySingleton mInstance;

    private ExecutorService mExecutorService;

    private FadecandyClient mFadecandyClient;

    private Context mContext;

    private List<ISingletonListener> mListeners = new ArrayList<>();

    private final static String TAG = FadecandySingleton.class.getSimpleName();

    private Thread workerThread = null;

    private boolean controlLoop = true;

    private int mCurrentBrightness = -1;

    private int mColor = -1;

    private int mCurrentSpeed = -1;

    private int mTemperature = -1;

    private final static int DEFAULT_LED_COUNT = 64;

    public final static int DEFAULT_SPARK_SPEED = 60;

    private final static String PREFERENCE_FIELD_LEDCOUNT = "ledCount";

    private int mLedCount;

    private SharedPreferences prefs;

    public static FadecandySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FadecandySingleton(context);
        }
        return mInstance;
    }

    public void addListener(ISingletonListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(ISingletonListener listener) {
        mListeners.remove(listener);
    }

    public FadecandySingleton(Context context) {

        prefs = context.getSharedPreferences(Constants.PREFERENCE_PREFS, Context.MODE_PRIVATE);
        mLedCount = prefs.getInt(PREFERENCE_FIELD_LEDCOUNT, DEFAULT_LED_COUNT);

        mExecutorService = Executors.newSingleThreadExecutor();

        mContext = context;

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

    public void clearPixel() {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                checkJoinThread();
                ColorUtils.clear(getIpAddress(), getServerPort(), mLedCount);
            }
        });
    }

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

    public int getColor() {
        return mColor;
    }

    public boolean isServerRunning() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.isServerRunning();
        }
        return false;
    }

    public void switchServerStatus() {

        if (mFadecandyClient != null) {

            if (mFadecandyClient.isServerRunning()) {
                mFadecandyClient.closeServer();
            } else {
                mFadecandyClient.startServer();
            }
        }
    }

    public ServiceType getServiceType() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getServiceType();
        }
        return null;
    }

    public FadecandyConfig getConfig() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getConfig();
        }
        return null;
    }

    public void setServiceType(ServiceType serviceType) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServiceType(serviceType);
        }
    }

    public int getServerPort() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getServerPort();
        }
        return 0;
    }

    public String getIpAddress() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getIpAddress();
        }
        return "";
    }

    public void setServerPort(int port) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServerPort(port);
        }
    }

    public void setServerAddress(String ip) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServerAddress(ip);
        }
    }

    public void disconnect() {
        if (mFadecandyClient != null) {
            mFadecandyClient.disconnect();
        }
    }

    public void closeServer() {
        if (mFadecandyClient != null) {
            mFadecandyClient.closeServer();
        }
    }

    public void startServer() {
        if (mFadecandyClient != null) {
            mFadecandyClient.startServer();
        }
    }

    public void connect() {
        if (mFadecandyClient != null) {
            mFadecandyClient.connect();
        }
    }

    public int getCurrentColorCorrection() {
        return mCurrentBrightness;
    }

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

    public void setColorCorrectionSpark(final int value) {

        mCurrentBrightness = value;

        Spark.COLOR_CORRECTION = (value / 100f);
    }

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

    public void setSpeed(int speed) {

        mCurrentSpeed = speed;

        Spark.setSpeed(speed);
    }

    public int getSpeed() {
        return mCurrentSpeed;
    }

    public void setTemperature(int temperature) {
        mTemperature = temperature;
        setFullColor(temperature);
    }

    public int getTemperature() {
        return mTemperature;
    }

    public HashMap<Integer, UsbItem> getUsbDevices() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getUsbDeviceMap();
        }
        return new HashMap<>();
    }

    public int getLedCount() {
        return mLedCount;
    }

    public void setLedCount(int ledCount) {
        this.mLedCount = ledCount;
        prefs.edit().putInt(PREFERENCE_FIELD_LEDCOUNT, mLedCount).apply();
    }
}
