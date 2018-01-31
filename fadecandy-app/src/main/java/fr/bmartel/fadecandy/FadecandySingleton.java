/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016-2018 Bertrand Martel
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
import fr.bmartel.android.fadecandy.inter.IFcServerEventListener;
import fr.bmartel.android.fadecandy.inter.IUsbEventListener;
import fr.bmartel.android.fadecandy.model.ServerError;
import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.fadecandy.constant.AppConstants;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;
import fr.bmartel.fadecandy.listener.ISingletonListener;
import fr.bmartel.fadecandy.utils.ManualResetEvent;
import fr.bmartel.opc.ISocketListener;
import fr.bmartel.opc.OpcClient;
import fr.bmartel.opc.OpcDevice;
import fr.bmartel.opc.PixelStrip;

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
    private int mCurrentBrightness;

    /**
     * led color current config.
     */
    private int mColor;

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
     * define if sparkling animation is running.
     */
    private boolean mIsSparkling;

    /**
     * define if mix animation is running.
     */
    private boolean mIsMixing;

    /**
     * define if animation brightness should be updated.
     */
    private boolean mIsBrightnessUpdate;

    /**
     * define if open pixel control request is pending
     */
    private boolean mIsSendingRequest;

    /**
     * define if spark color shoudl be updated.
     */
    private boolean mIsSparkColorUpdate;

    /**
     * monitoring object used to wait for server close before starting server if already started.
     */
    private ManualResetEvent eventManager = new ManualResetEvent(false);

    /**
     * define  if websocket will be closed manually so we dont dispatch error listener when this is set to true.
     */
    private boolean mWebsocketClose = false;

    /**
     * define if span should be updated.
     */
    private boolean mIsSpanUpdate;

    private OpcClient mOpcClient;

    private OpcDevice mOpcDevice;

    private PixelStrip mPixelStrip;

    private boolean mRestartAnimation;

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
        mCurrentBrightness = prefs.getInt(AppConstants.PREFERENCE_FIELD_BRIGHTNESS, AppConstants.DEFAULT_BRIGHTNESS);
        mColor = prefs.getInt(AppConstants.PREFERENCE_FIELD_COLOR, AppConstants.DEFAULT_COLOR);

        mExecutorService = Executors.newSingleThreadScheduledExecutor();

        mContext = context;

        mIsBrightnessUpdate = false;
        mIsSpanUpdate = false;

        //build Fadecandy client to bind Fadecandy service & start server

        mFadecandyClient = new FadecandyClient(mContext, new IFcServerEventListener() {

            @Override
            public void onServerStart() {
                Log.v(TAG, "onServerStart");
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerStart();
                }

                initOpcClient();
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
                Log.v(TAG, "onServerError");
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerError();
                }
            }

        }, new IUsbEventListener() {
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

        initOpcClient();
    }

    public boolean isBrightnessUpdate() {
        return mIsBrightnessUpdate;
    }

    public void setBrightnessUpdate(boolean brightnessUpdate) {
        mIsBrightnessUpdate = brightnessUpdate;
    }

    public boolean isSpanUpdate() {
        return mIsSpanUpdate;
    }

    public void setSpanUpdate(boolean spanUpdate) {
        mIsSpanUpdate = spanUpdate;
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

                    if (ColorUtils.clear(FadecandySingleton.this) == -1) {
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
    public void setFullColor(final int color, boolean storeColorVal, boolean force) {

        mColor = color;
        if (storeColorVal) {
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_COLOR, mColor).apply();
        }

        if (!mIsSendingRequest || force) {

            mIsSendingRequest = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();

                    if (ColorUtils.setFullColor(FadecandySingleton.this) == -1) {
                        Log.e(TAG, "error occured");
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
    public String getConfig() {
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
     * Set Fadecandy configuration.
     *
     * @param config
     */
    public void setConfig(String config) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setConfig(config);
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
    public void setColorCorrection(final int value, boolean force) {

        mCurrentBrightness = value;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_BRIGHTNESS, mCurrentBrightness).apply();

        if (!mIsSendingRequest || force) {

            mIsSendingRequest = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    if (ColorUtils.setBrightness(FadecandySingleton.this) == -1) {
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
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_BRIGHTNESS, mCurrentBrightness).apply();
        mIsBrightnessUpdate = true;
    }

    /**
     * start Spark animation
     *
     * @param color color to be used when running spark animation
     */
    public void spark(final int color) {

        mColor = color;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_COLOR, mColor).apply();
        mIsSparkColorUpdate = true;

        if (!mIsSparkling) {

            mIsSparkling = true;

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();
                    mAnimating = true;

                    workerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (spark() == -1) {
                                //error occured
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.get(i).onServerConnectionFailure();
                                }
                                mAnimating = false;
                                mIsSparkling = false;
                                return;
                            }

                            mAnimating = false;
                            mIsSparkling = false;

                            if (mRestartAnimation) {

                                mRestartAnimation = false;

                                initOpcClient();

                                mExecutorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        spark(mColor);
                                    }
                                });

                            }
                        }
                    });
                    workerThread.start();

                }
            });
        }
    }

    private int spark() {

        Spark spark = new Spark(Spark.buildColors(getColor(), (mLedCount * getSparkSpan() / 100)));

        mPixelStrip.setAnimation(spark);

        int status = 0;

        setSpanUpdate(false);
        setmIsSparkColorUpdate(false);

        while (isAnimating()) {

            status = mOpcClient.animate();
            if (status == -1) {
                return -1;
            }

            if (isBrightnessUpdate()) {

                status = mOpcClient.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION,
                        getCurrentColorCorrection() / 100f,
                        getCurrentColorCorrection() / 100f,
                        getCurrentColorCorrection() / 100f);

                if (status == -1) {
                    return -1;
                }
                setBrightnessUpdate(false);
            }

            if (isSpanUpdate()) {
                mPixelStrip.clear();
                spark = new Spark(Spark.buildColors(getColor(), (mLedCount * getSparkSpan() / 100)));
                mPixelStrip.setAnimation(spark);
                setSpanUpdate(false);
            }

            if (ismIsSparkColorUpdate()) {
                spark.updateColor(mColor, (mLedCount * getSparkSpan() / 100));
                setmIsSparkColorUpdate(false);
            }

            try {
                Thread.sleep(Spark.convertSpeed(getSpeed()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public boolean ismIsSparkColorUpdate() {
        return mIsSparkColorUpdate;
    }

    public void setmIsSparkColorUpdate(boolean sparkColorUpdate) {
        mIsSparkColorUpdate = sparkColorUpdate;
    }

    /**
     * Set spark animation speed.
     *
     * @param speed
     */
    public void setSpeed(int speed) {
        mSparkSpeed = speed;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_SPARK_SPEED, speed).apply();
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
        setFullColor(temperature, false, false);
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

        if (!mAnimating) {
            initOpcClient();
        } else {
            mRestartAnimation = true;
            mAnimating = false;
        }
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

                            if (messageJson.has("type")) {

                                String type = messageJson.getString("type");

                                if (type.equals("connected_devices_changed")) {

                                    JSONArray devices = (JSONArray) messageJson.get("devices");

                                    for (int i = 0; i < mListeners.size(); i++) {
                                        mListeners.get(i).onConnectedDeviceChanged(devices.length());
                                    }

                                } else if (type.equals("list_connected_devices")) {

                                    JSONArray devices = (JSONArray) messageJson.get("devices");

                                    for (int i = 0; i < mListeners.size(); i++) {
                                        mListeners.get(i).onConnectedDeviceChanged(devices.length());
                                    }
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

                        if (!mWebsocketClose) {
                            for (int i = 0; i < mListeners.size(); i++) {
                                mListeners.get(i).onServerConnectionClosed();
                            }
                        }
                        eventManager.set();
                    }
                });

                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerConnectionSuccess();
                }

                sendListConnectedDevices();
                initOpcClient();
            }
        };
        AsyncHttpClient asyncHttpClient = AsyncHttpClient.getDefaultInstance();

        mWebsocketFuture = asyncHttpClient.websocket("ws://" + getRemoteServerIp() + ":" + getRemoteServerPort() + "/", null, mWebSocketConnectCallback);


    }

    /**
     * get connected devices list from websocket connection.
     */
    private void sendListConnectedDevices() {
        try {
            JSONObject req = new JSONObject();
            req.put("type", "list_connected_devices");
            if (mWebsocket != null) {
                mWebsocket.send(req.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * close websocket connection.
     */
    private void closeWebsocket() {

        eventManager.reset();

        mWebsocketClose = true;

        if (mWebsocket != null) {
            mWebsocket.close();
        }

        try {
            eventManager.waitOne(AppConstants.STOP_WEBSOCKET_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mWebsocketClose = false;

        if (mWebsocketFuture != null) {
            mWebsocketFuture.cancel(true);
        }
    }

    /**
     * restart websocket connection.
     */
    public void restartRemoteConnection() {
        closeWebsocket();
        createWebsocketClient();
    }

    /**
     * get remote server address.
     *
     * @return
     */
    public String getRemoteServerIp() {
        return mRemoteServerIp;
    }

    /**
     * get remote server port.
     *
     * @return
     */
    public int getRemoteServerPort() {
        return mRemoteServerPort;
    }

    /**
     * set remote server address.
     *
     * @param ip
     */
    public void setRemoteServerIp(String ip) {
        mRemoteServerIp = ip;
        prefs.edit().putString(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_IP, ip).apply();
        initOpcClient();
    }

    /**
     * set remote server port.
     *
     * @param port
     */
    public void setRemoteServerPort(int port) {
        mRemoteServerPort = port;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_PORT, port).apply();
        initOpcClient();
    }

    public int getSparkSpan() {
        return mSparkSpan;
    }

    public void setSparkSpan(int sparkSpan) {
        mSparkSpan = sparkSpan;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_SPARK_SPAN, sparkSpan).apply();
        mIsSpanUpdate = true;
    }

    public void setMixerDelay(int mixerDelay) {
        mMixerDelay = mixerDelay;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_MIXER_DELAY, mixerDelay).apply();
    }

    public int getMixerDelay() {
        return mMixerDelay;
    }

    /**
     * mixer effect.
     */
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
                            if (ColorUtils.mixer(FadecandySingleton.this) == -1) {
                                //error occured
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.get(i).onServerConnectionFailure();
                                }
                            }
                            mAnimating = false;
                            mIsMixing = false;

                            if (mRestartAnimation) {

                                mRestartAnimation = false;

                                initOpcClient();

                                mExecutorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        mixer();
                                    }
                                });

                            }
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

    /**
     * pulse effect.
     *
     * @param color
     */
    public void pulse(int color) {

        mColor = color;
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_COLOR, mColor).apply();

        if (!mIsPulsing) {

            mIsPulsing = true;
            mIsSendingRequest = false;

            final int oldBrightness = getCurrentColorCorrection();

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    checkJoinThread();
                    workerThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            mAnimating = true;
                            if (ColorUtils.pulse(FadecandySingleton.this) == -1) {
                                //error occured
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.get(i).onServerConnectionFailure();
                                }
                            }
                            mIsPulsing = false;
                            mAnimating = false;

                            if (mRestartAnimation) {

                                mRestartAnimation = false;

                                initOpcClient();

                                mExecutorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        pulse(mColor);
                                    }
                                });

                            } else {
                                setColorCorrection(oldBrightness, true);

                            }
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

    public void initOpcClient() {

        if (mOpcClient != null) {
            mOpcClient.close();
        }

        mOpcClient = new OpcClient(getIpAddress(), getServerPort());

        mOpcClient.setReuseAddress(true);
        mOpcClient.setSoTimeout(AppConstants.SOCKET_TIMEOUT);
        mOpcClient.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT);

        mOpcClient.addSocketListener(new ISocketListener() {
            @Override
            public void onSocketError(Exception e) {
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onServerConnectionFailure();
                }
            }
        });
        mOpcDevice = mOpcClient.addDevice();
        mPixelStrip = mOpcDevice.addPixelStrip(0, mLedCount);
    }

    public OpcClient getOpcClient() {
        return mOpcClient;
    }

    public OpcDevice getOpcDevice() {
        return mOpcDevice;
    }

    public PixelStrip getPixelStrip() {
        return mPixelStrip;
    }

    public void updateConfig(String config) {
        mFadecandyClient.setConfig(config);
    }

    public String getDefaultConfig() {
        return mFadecandyClient.getDefaultConfig();
    }
}
