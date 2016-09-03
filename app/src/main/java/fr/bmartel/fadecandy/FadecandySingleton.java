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

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.bmartel.android.fadecandy.client.FadecandyClient;
import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.inter.IFadecandyListener;
import fr.bmartel.android.fadecandy.inter.IUsbListener;
import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.android.fadecandy.model.ServerError;
import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.fadecandy.constant.AppConstants;
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
    private ScheduledExecutorService mExecutorService;

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
    private int mSparkSpeed;

    /**
     * led temperature current config.
     */
    private int mTemperature;

    /**
     * define if Android server is used (true) or antoher Fadecandy server on LAN (false).
     */
    private boolean mServerMode;

    /**
     * current number of led.
     */
    private int mLedCount;

    /**
     * shared preferences.
     */
    private SharedPreferences prefs;

    /**
     * websocket client in host mode.
     */
    private Future<WebSocket> mWebsocketFuture;

    private WebSocket mWebsocket;

    /**
     * remote server ip.
     */
    private String mRemoteServerIp;

    /**
     * remote server port
     */
    private int mRemoteServerPort;

    /**
     * spark span.
     */
    private int mSparkSpan;

    /**
     * mixer delay.
     */
    private int mMixerDelay;

    /**
     * define if animation is running.
     */
    private boolean mAnimating = false;

    /**
     * pulse delay in ms.
     */
    private int mPulseDelay;

    /**
     * pulse pause between 2 pulses.
     */
    private int mPulsePause;

    /**
     * define if pulse animation is running.
     */
    private boolean mIsPulsing;

    /**
     * define if mix animation is running.
     */
    private boolean mIsMixing;

    /**
     * define if spark animation is running.
     */
    private boolean mIsSparking;

    /**
     * define if open pixel control request is pending
     */
    private boolean mIsSendingRequest;

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
        mLedCount = prefs.getInt(AppConstants.PREFERENCE_FIELD_LEDCOUNT, AppConstants.DEFAULT_LED_COUNT);
        mServerMode = prefs.getBoolean(AppConstants.PREFERENCE_FIELD_SERVER_MODE, AppConstants.DEFAULT_SERVER_MODE);
        mRemoteServerIp = prefs.getString(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_IP, AppConstants.DEFAULT_SERVER_IP);
        mRemoteServerPort = prefs.getInt(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_PORT, AppConstants.DEFAULT_SERVER_PORT);
        mSparkSpan = prefs.getInt(AppConstants.PREFERENCE_FIELD_SPARK_SPAN, AppConstants.DEFAULT_SPARK_SPAN);
        mSparkSpeed = prefs.getInt(AppConstants.PREFERENCE_FIELD_SPARK_SPEED, AppConstants.DEFAULT_SPARK_SPEED);
        mMixerDelay = prefs.getInt(AppConstants.PREFERENCE_FIELD_MIXER_DELAY, AppConstants.DEFAULT_MIXER_DELAY);
        mPulseDelay = prefs.getInt(AppConstants.PREFERENCE_FIELD_PULSE_DELAY, AppConstants.DEFAULT_PULSE_DELAY);
        mPulsePause = prefs.getInt(AppConstants.PREFERENCE_FIELD_PULSE_PAUSE, AppConstants.DEFAULT_PULSE_PAUSE);
        mTemperature = prefs.getInt(AppConstants.PREFERENCE_FIELD_TEMPERATURE, AppConstants.DEFAULT_TEMPERATURE);

        mExecutorService = Executors.newSingleThreadScheduledExecutor();

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

        if (mServerMode) {
            mFadecandyClient.startServer();
        } else {
            createWebsocketClient();
        }
    }

    /**
     * Clear all led.
     */
    public void clearPixel() {

        if (!mIsSendingRequest) {

            mIsSendingRequest = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    checkJoinThread();

                    if (ColorUtils.clear(getIpAddress(), getServerPort(), mLedCount) == -1) {
                        //error occured
                        for (int i = 0; i < mListeners.size(); i++) {
                            mListeners.get(i).onServerConnectionFailure();
                        }
                    }
                    mIsSendingRequest = false;
                }
            });
        }
    }

    /**
     * Wait for led animation thread to join.
     */
    public void checkJoinThread() {

        if (workerThread != null) {
            Spark.CONTROL = false;
            mAnimating = false;
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

        if (!mIsSendingRequest) {

            mIsSendingRequest = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();

                    if (ColorUtils.setFullColor(getIpAddress(), getServerPort(), mLedCount, color) == -1) {
                        //error occured
                        for (int i = 0; i < mListeners.size(); i++) {
                            mListeners.get(i).onServerConnectionFailure();
                        }
                    }
                    mIsSendingRequest = false;
                }
            });
        }
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

        if (mServerMode) {
            if (mFadecandyClient != null) {
                return mFadecandyClient.getServerPort();
            }
        } else {
            return mRemoteServerPort;
        }
        return FadecandyClient.DEFAULT_PORT;
    }

    /**
     * Retrieve server address.
     *
     * @return
     */
    public String getIpAddress() {

        if (mServerMode) {
            if (mFadecandyClient != null) {
                return mFadecandyClient.getIpAddress();
            }
        } else {
            return mRemoteServerIp;
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
            if (!mServerMode) {
                mFadecandyClient.connect(ServiceType.NON_PERSISTENT_SERVICE);
            } else {
                mFadecandyClient.connect();
            }
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

        if (!mIsSendingRequest) {

            mIsSendingRequest = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();

                    if (ColorUtils.setBrightness(getIpAddress(), getServerPort(), (value / 100f)) == -1) {
                        //error occured
                        for (int i = 0; i < mListeners.size(); i++) {
                            mListeners.get(i).onServerConnectionFailure();
                        }
                    }
                    mIsSendingRequest = false;
                }
            });
        }
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

        if (!mIsSparking) {

            mIsSparking = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();
                    Spark.CONTROL = true;
                    mAnimating = true;

                    workerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            while (mAnimating) {

                                if (Spark.draw(getIpAddress(), getServerPort(), mLedCount, color, mSparkSpan) == -1) {
                                    //error occured
                                    for (int i = 0; i < mListeners.size(); i++) {
                                        mListeners.get(i).onServerConnectionFailure();
                                    }
                                    Spark.CONTROL = false;
                                    mAnimating = false;
                                    mIsSparking = false;
                                    return;
                                }
                            }
                            mIsSparking = false;
                        }
                    });
                    workerThread.start();

                }
            });
        }
    }

    /**
     * Set spark animation speed.
     *
     * @param speed
     */
    public void setSpeed(int speed) {
        mSparkSpeed = speed;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_SPARK_SPEED, speed).apply();
        Spark.setSpeed(speed);
    }

    /**
     * Retrieve spark animation speed.
     *
     * @return
     */
    public int getSpeed() {
        return mSparkSpeed;
    }

    /**
     * Set led temperature value.
     *
     * @param temperature
     */
    public void setTemperature(int temperature) {
        mTemperature = temperature;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_TEMPERATURE, mTemperature).apply();
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
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_LEDCOUNT, mLedCount).apply();
    }

    /**
     * Restart Fadecandy Server.
     */
    public void restartServer() {

        if (mFadecandyClient != null) {
            mFadecandyClient.startServer();
        }
    }

    public boolean isServerMode() {
        return mServerMode;
    }

    public void setServerMode(boolean serverMode) {
        if (mServerMode && !serverMode) {
            mServerMode = serverMode;
            //stop server & unbind service
            closeServer();
            disconnect();
            mFadecandyClient.stopService();
            createWebsocketClient();
        } else if (!mServerMode && serverMode) {
            //bind server & start server
            closeWebsocket();
            mServerMode = serverMode;
            startServer();
        }
        prefs.edit().putBoolean(AppConstants.PREFERENCE_FIELD_SERVER_MODE, mServerMode).apply();
    }

    private void createWebsocketClient() {

        Log.v(TAG, "connecting to websocket server");

        final ScheduledFuture cancelTask = mExecutorService.schedule(new Runnable() {
            @Override
            public void run() {

                if (mWebsocketFuture != null) {
                    mWebsocketFuture.cancel(true);
                }
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerConnectionFailure();
                }
            }
        }, AppConstants.WEBSOCKET_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);

        AsyncHttpClient.WebSocketConnectCallback mWebSocketConnectCallback = new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {

                Log.v(TAG, "onCompleted");

                if (cancelTask != null) {
                    cancelTask.cancel(true);
                }

                mWebsocket = webSocket;

                if (ex != null) {
                    for (int i = 0; i < mListeners.size(); i++) {
                        mListeners.get(i).onServerConnectionFailure();
                    }
                    return;
                }

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String message) {

                        Log.v(TAG, "messageJson : " + message);

                        try {
                            JSONObject messageJson = new JSONObject(message);

                            if (messageJson.has("type") &&
                                    messageJson.getString("type").equals("connected_devices_changed") &&
                                    messageJson.has("devices")) {

                                JSONArray devices = (JSONArray) messageJson.get("devices");

                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.get(i).onConnectedDeviceChanged(devices.length());
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        for (int i = 0; i < mListeners.size(); i++) {
                            mListeners.get(i).onServerConnectionClosed();
                        }
                    }
                });

                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerConnectionSuccess();
                }
            }
        };
        AsyncHttpClient asyncHttpClient = AsyncHttpClient.getDefaultInstance();

        mWebsocketFuture = asyncHttpClient.websocket("ws://" + getRemoteServerIp() + ":" + getRemoteServerPort() + "/", null, mWebSocketConnectCallback);


    }

    private void closeWebsocket() {

        if (mWebsocket != null) {
            mWebsocket.close();
        }
        if (mWebsocketFuture != null) {
            mWebsocketFuture.cancel(true);
        }
    }

    public void restartRemoteConnection() {
        closeWebsocket();
        createWebsocketClient();
    }

    public String getRemoteServerIp() {
        return mRemoteServerIp;
    }

    public int getRemoteServerPort() {
        return mRemoteServerPort;
    }

    public void setRemoteServerIp(String ip) {
        mRemoteServerIp = ip;
        prefs.edit().putString(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_IP, ip).apply();
    }

    public void setRemoteServerPort(int port) {
        mRemoteServerPort = port;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_PORT, port).apply();
    }

    public int getSparkSpan() {
        return mSparkSpan;
    }

    public void setSparkSpan(int sparkSpan) {
        mSparkSpan = sparkSpan;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_SPARK_SPAN, sparkSpan).apply();
        Spark.SPAN = sparkSpan;
    }

    public void setMixerDelay(int mixerDelay) {
        mMixerDelay = mixerDelay;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_MIXER_DELAY, mixerDelay).apply();
    }

    public int getMixerDelay() {
        return mMixerDelay;
    }

    public void mixer() {

        if (!mIsMixing) {

            mIsMixing = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();

                    workerThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            mAnimating = true;
                            if (ColorUtils.mixer(getIpAddress(), getServerPort(), mLedCount, mMixerDelay, FadecandySingleton.this) == -1) {
                                //error occured
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.get(i).onServerConnectionFailure();
                                }
                            }
                            mAnimating = false;
                            mIsMixing = false;
                        }
                    });
                    workerThread.start();
                }
            });
        }
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    public void pulse(int color) {

        mColor = color;

        if (!mIsPulsing) {

            mIsPulsing = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();
                    workerThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            mAnimating = true;
                            if (ColorUtils.pulse(getIpAddress(), getServerPort(), mLedCount, FadecandySingleton.this) == -1) {
                                //error occured
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.get(i).onServerConnectionFailure();
                                }
                            }
                            mIsPulsing = false;
                            mAnimating = false;
                        }
                    });
                    workerThread.start();
                }
            });
        }

    }

    public int getPulseDelay() {
        return mPulseDelay;
    }

    public int getPulsePause() {
        return mPulsePause;
    }

    public void setPulseDelay(int pulseDelay) {
        mPulseDelay = pulseDelay;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_PULSE_DELAY, mPulseDelay).apply();
    }

    public void setPulsePause(int pulsePause) {
        mPulsePause = pulsePause;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_PULSE_PAUSE, mPulsePause).apply();
    }
}
