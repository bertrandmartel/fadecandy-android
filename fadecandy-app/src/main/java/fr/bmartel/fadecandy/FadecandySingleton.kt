/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2016-2018 Bertrand Martel
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.koushikdutta.async.callback.CompletedCallback
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket
import fr.bmartel.android.fadecandy.client.FadecandyClient
import fr.bmartel.android.fadecandy.constant.Constants
import fr.bmartel.android.fadecandy.inter.IFcServerEventListener
import fr.bmartel.android.fadecandy.inter.IUsbEventListener
import fr.bmartel.android.fadecandy.model.ServerError
import fr.bmartel.android.fadecandy.model.ServiceType
import fr.bmartel.android.fadecandy.model.UsbItem
import fr.bmartel.fadecandy.constant.AppConstants
import fr.bmartel.fadecandy.ledcontrol.ColorUtils
import fr.bmartel.fadecandy.ledcontrol.Spark
import fr.bmartel.fadecandy.listener.ISingletonListener
import fr.bmartel.fadecandy.utils.ManualResetEvent
import fr.bmartel.opc.OpcClient
import fr.bmartel.opc.OpcDevice
import fr.bmartel.opc.PixelStrip
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

/**
 * Singleton used to bind service and wrap around it.
 *
 * @author Bertrand Martel
 */
class FadecandySingleton(mContext: Context) {

    /**
     * an executor to launch task on a new thread.
     */
    private var mExecutorService: ScheduledExecutorService

    /**
     * fadecandy client used to wrap Fadecandy service interface.
     */
    private val mFadecandyClient: FadecandyClient

    /**
     * list of listeners added by activities.
     */
    private val mListeners = ArrayList<ISingletonListener>()

    /**
     * worker thread used for Led animations.
     */
    private var workerThread: Thread? = null

    /**
     * led brightness current config.
     */
    /**
     * Retrieve current brightness.
     *
     * @return
     */
    var currentColorCorrection: Int = 0
        private set

    /**
     * led color current config.
     */
    /**
     * get current color config.
     *
     * @return
     */
    var color: Int = 0
        private set

    /**
     * spark speed current config.
     */
    private var mSparkSpeed: Int = 0

    /**
     * led temperature current config.
     */
    private var mTemperature: Int = 0

    /**
     * define if Android server is used (true) or antoher Fadecandy server on LAN (false).
     */
    private var mServerMode: Boolean = false

    /**
     * current number of led.
     */
    private var mLedCount: Int = 0

    /**
     * shared preferences.
     */
    private val prefs: SharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_PREFS, Context.MODE_PRIVATE)

    /**
     * websocket client in host mode.
     */
    private var mWebsocketFuture: Future<WebSocket>? = null

    private var mWebsocket: WebSocket? = null

    /**
     * remote server ip.
     */
    var mRemoteServerIp: String

    /**
     * remote server port
     */
    private var mRemoteServerPort: Int

    /**
     * spark span.
     */
    private var mSparkSpan: Int = 0

    /**
     * mixer delay.
     */
    private var mMixerDelay: Int = 0

    /**
     * define if animation is running.
     */
    var isAnimating = false
        private set

    /**
     * pulse delay in ms.
     */
    private var mPulseDelay: Int = 0

    /**
     * pulse pause between 2 pulses.
     */
    private var mPulsePause: Int = 0

    /**
     * define if pulse animation is running.
     */
    private var mIsPulsing: Boolean = false

    /**
     * define if sparkling animation is running.
     */
    private var mIsSparkling: Boolean = false

    /**
     * define if mix animation is running.
     */
    private var mIsMixing: Boolean = false

    /**
     * define if animation brightness should be updated.
     */
    var isBrightnessUpdate: Boolean = false

    /**
     * define if open pixel control request is pending
     */
    private var mIsSendingRequest: Boolean = false

    /**
     * define if spark color shoudl be updated.
     */
    private var mIsSparkColorUpdate: Boolean = false

    /**
     * monitoring object used to wait for server close before starting server if already started.
     */
    private val eventManager = ManualResetEvent(false)

    /**
     * define  if websocket will be closed manually so we dont dispatch error listener when this is set to true.
     */
    private var mWebsocketClose = false

    /**
     * define if span should be updated.
     */
    var isSpanUpdate: Boolean = false

    var opcClient: OpcClient? = null
        private set

    var opcDevice: OpcDevice? = null
        private set

    var pixelStrip: PixelStrip? = null
        private set

    private var mRestartAnimation: Boolean = false

    /**
     * define if the server is running or not.
     *
     * @return
     */
    val isServerRunning: Boolean
        get() = mFadecandyClient.isServerRunning

    /**
     * Retrieve service type (persistent or non persistent)
     *
     * @return
     */
    /**
     * set service type.
     *
     * @param serviceType
     */
    var serviceType: ServiceType?
        get() = mFadecandyClient.serviceType
        set(serviceType) {
            mFadecandyClient.serviceType = serviceType
        }

    /**
     * Retrieve Fadecandy configuration.
     *
     * @return
     */
    /**
     * Set Fadecandy configuration.
     *
     * @param config
     */
    var config: String?
        get() = mFadecandyClient.config
        set(config) {
            mFadecandyClient.config = config
        }

    /**
     * Retrieve server port.
     *
     * @return
     */
    /**
     * Set server port.
     *
     * @param port
     */
    var serverPort: Int
        get() {
            if (mServerMode) {
                return mFadecandyClient.serverPort
            } else {
                return mRemoteServerPort
            }
        }
        set(port) {
            mFadecandyClient.serverPort = port
        }

    /**
     * Retrieve server address.
     *
     * @return
     */
    /**
     * Set server address.
     *
     * @param ip
     */
    var ipAddress: String?
        get() {
            if (mServerMode) {
                return mFadecandyClient.ipAddress
            } else {
                return mRemoteServerIp
            }
        }
        set(ip) {
            mFadecandyClient.setServerAddress(ip)
        }

    /**
     * Retrieve spark animation speed.
     *
     * @return
     */
    /**
     * Set spark animation speed.
     *
     * @param speed
     */
    var speed: Int
        get() = mSparkSpeed
        set(speed) {
            mSparkSpeed = speed
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_SPARK_SPEED, speed).apply()
        }

    /**
     * Retrieve current value of led temperature.
     *
     * @return
     */
    /**
     * Set led temperature value.
     *
     * @param temperature
     */
    var temperature: Int
        get() = mTemperature
        set(temperature) {
            mTemperature = temperature
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_TEMPERATURE, mTemperature).apply()
            setFullColor(temperature, false, false)
        }

    /**
     * Retrieve list of Fadecandy USB device attached.
     *
     * @return
     */
    val usbDevices: HashMap<Int?, UsbItem?>
        get() = mFadecandyClient.usbDeviceMap

    /**
     * Retrieve led count
     *
     * @return
     */
    /**
     * Set led count.
     *
     * @param ledCount
     */
    var ledCount: Int
        get() = mLedCount
        set(ledCount) {
            this.mLedCount = ledCount
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_LEDCOUNT, mLedCount).apply()

            if (!isAnimating) {
                initOpcClient()
            } else {
                mRestartAnimation = true
                isAnimating = false
            }
        }

    //stop server & unbind service
    //bind server & start server
    var isServerMode: Boolean
        get() = mServerMode
        set(serverMode) {
            if (mServerMode && !serverMode) {
                mServerMode = serverMode
                closeServer()
                disconnect()
                mFadecandyClient.stopService()
                createWebsocketClient()
            } else if (!mServerMode && serverMode) {
                closeWebsocket()
                mServerMode = serverMode
                startServer()
            }
            prefs.edit().putBoolean(AppConstants.PREFERENCE_FIELD_SERVER_MODE, mServerMode).apply()
        }

    /**
     * get remote server address.
     *
     * @return
     */
    /**
     * set remote server address.
     *
     * @param ip
     */
    var remoteServerIp: String
        get() = mRemoteServerIp
        set(ip) {
            mRemoteServerIp = ip
            prefs.edit().putString(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_IP, ip).apply()
            initOpcClient()
        }

    /**
     * get remote server port.
     *
     * @return
     */
    /**
     * set remote server port.
     *
     * @param port
     */
    var remoteServerPort: Int
        get() = mRemoteServerPort
        set(port) {
            mRemoteServerPort = port
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_PORT, port).apply()
            initOpcClient()
        }

    var sparkSpan: Int
        get() = mSparkSpan
        set(sparkSpan) {
            mSparkSpan = sparkSpan
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_SPARK_SPAN, sparkSpan).apply()
            isSpanUpdate = true
        }

    var mixerDelay: Int
        get() = mMixerDelay
        set(mixerDelay) {
            mMixerDelay = mixerDelay
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_MIXER_DELAY, mixerDelay).apply()
        }

    var pulseDelay: Int
        get() = mPulseDelay
        set(pulseDelay) {
            mPulseDelay = pulseDelay
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_PULSE_DELAY, mPulseDelay).apply()
        }

    var pulsePause: Int
        get() = mPulsePause
        set(pulsePause) {
            mPulsePause = pulsePause
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_PULSE_PAUSE, mPulsePause).apply()
        }

    val defaultConfig: String
        get() = mFadecandyClient.defaultConfig

    /**
     * add a singleton listener.
     *
     * @param listener
     */
    fun addListener(listener: ISingletonListener) {
        mListeners.add(listener)
    }

    /**
     * remove a singleton listener.
     *
     * @param listener
     */
    fun removeListener(listener: ISingletonListener) {
        mListeners.remove(listener)
    }

    init {
        mLedCount = prefs.getInt(AppConstants.PREFERENCE_FIELD_LEDCOUNT, AppConstants.DEFAULT_LED_COUNT)
        mServerMode = prefs.getBoolean(AppConstants.PREFERENCE_FIELD_SERVER_MODE, AppConstants.DEFAULT_SERVER_MODE)
        mRemoteServerIp = prefs.getString(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_IP, AppConstants.DEFAULT_SERVER_IP)
        mRemoteServerPort = prefs.getInt(AppConstants.PREFERENCE_FIELD_REMOTE_SERVER_PORT, AppConstants.DEFAULT_SERVER_PORT)
        mSparkSpan = prefs.getInt(AppConstants.PREFERENCE_FIELD_SPARK_SPAN, AppConstants.DEFAULT_SPARK_SPAN)
        mSparkSpeed = prefs.getInt(AppConstants.PREFERENCE_FIELD_SPARK_SPEED, AppConstants.DEFAULT_SPARK_SPEED)
        mMixerDelay = prefs.getInt(AppConstants.PREFERENCE_FIELD_MIXER_DELAY, AppConstants.DEFAULT_MIXER_DELAY)
        mPulseDelay = prefs.getInt(AppConstants.PREFERENCE_FIELD_PULSE_DELAY, AppConstants.DEFAULT_PULSE_DELAY)
        mPulsePause = prefs.getInt(AppConstants.PREFERENCE_FIELD_PULSE_PAUSE, AppConstants.DEFAULT_PULSE_PAUSE)
        mTemperature = prefs.getInt(AppConstants.PREFERENCE_FIELD_TEMPERATURE, AppConstants.DEFAULT_TEMPERATURE)
        currentColorCorrection = prefs.getInt(AppConstants.PREFERENCE_FIELD_BRIGHTNESS, AppConstants.DEFAULT_BRIGHTNESS)
        color = prefs.getInt(AppConstants.PREFERENCE_FIELD_COLOR, AppConstants.DEFAULT_COLOR)

        mExecutorService = Executors.newSingleThreadScheduledExecutor()

        isBrightnessUpdate = false
        isSpanUpdate = false

        //build Fadecandy client to bind Fadecandy service & start server
        mFadecandyClient = FadecandyClient(mContext, object : IFcServerEventListener {

            override fun onServerStart() {
                Log.v(TAG, "onServerStart")
                for (i in mListeners.indices) {
                    mListeners[i].onServerStart()
                }
                initOpcClient()
            }

            override fun onServerClose() {
                Log.v(TAG, "onServerClose")
                for (i in mListeners.indices) {
                    mListeners[i].onServerClose()
                }
            }

            override fun onServerError(error: ServerError) {
                Log.v(TAG, "onServerError")
                for (i in mListeners.indices) {
                    mListeners[i].onServerError()
                }
            }

        }, object : IUsbEventListener {
            override fun onUsbDeviceAttached(usbItem: UsbItem?) {
                for (i in mListeners.indices) {
                    mListeners[i].onUsbDeviceAttached(usbItem)
                }
            }

            override fun onUsbDeviceDetached(usbItem: UsbItem?) {
                for (i in mListeners.indices) {
                    mListeners[i].onUsbDeviceDetached(usbItem)
                }
            }
        }, "fr.bmartel.fadecandy/.activity.MainActivity")

        if (mServerMode) {
            mFadecandyClient.startServer()
        } else {
            createWebsocketClient()
        }

        initOpcClient()
    }

    /**
     * Clear all led.
     */
    fun clearPixel() {
        if (!mIsSendingRequest) {
            mIsSendingRequest = true
            mExecutorService.execute {
                checkJoinThread()
                if (ColorUtils.clear(this@FadecandySingleton) == -1) {
                    //error occured
                    for (i in mListeners.indices) {
                        mListeners[i].onServerConnectionFailure()
                    }
                }
                mIsSendingRequest = false
            }
        }
    }

    /**
     * Wait for led animation thread to join.
     */
    fun checkJoinThread() {
        if (workerThread != null) {
            isAnimating = false
            try {
                workerThread?.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * set all pixel color.
     *
     * @param color
     */
    fun setFullColor(color: Int, storeColorVal: Boolean, force: Boolean) {
        this.color = color
        if (storeColorVal) {
            prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_COLOR, this.color).apply()
        }

        if (!mIsSendingRequest || force) {
            mIsSendingRequest = true
            mExecutorService.execute {
                checkJoinThread()

                if (ColorUtils.setFullColor(this@FadecandySingleton) == -1) {
                    Log.e(TAG, "error occured")
                    //error occured
                    for (i in mListeners.indices) {
                        mListeners[i].onServerConnectionFailure()
                    }
                }
                mIsSendingRequest = false
            }
        }
    }

    /**
     * unbind service & unregister receiver.
     */
    fun disconnect() {
        mFadecandyClient.disconnect()
    }

    /**
     * stop Fadecandy server.
     */
    fun closeServer() {
        mFadecandyClient.closeServer()
    }

    /**
     * start Fadecandy server.
     */
    fun startServer() {
        mFadecandyClient.startServer()
    }

    /**
     * bind service & register receiver.
     */
    fun connect() {
        if (!mServerMode) {
            mFadecandyClient.connect(ServiceType.NON_PERSISTENT_SERVICE)
        } else {
            mFadecandyClient.connect()
        }
    }

    /**
     * set brightness (same color correction for RGB)
     *
     * @param value
     */
    fun setColorCorrection(value: Int, force: Boolean) {
        currentColorCorrection = value
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_BRIGHTNESS, currentColorCorrection).apply()

        if (!mIsSendingRequest || force) {
            mIsSendingRequest = true

            mExecutorService.execute {
                if (ColorUtils.setBrightness(this@FadecandySingleton) == -1) {
                    //error occured
                    for (i in mListeners.indices) {
                        mListeners[i].onServerConnectionFailure()
                    }
                }
                mIsSendingRequest = false
            }
        }
    }

    /**
     * set color correction during spark animation.
     *
     * @param value
     */
    fun setColorCorrectionSpark(value: Int) {
        currentColorCorrection = value
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_BRIGHTNESS, currentColorCorrection).apply()
        isBrightnessUpdate = true
    }

    /**
     * start Spark animation
     *
     * @param color color to be used when running spark animation
     */
    fun spark(color: Int) {
        this.color = color
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_COLOR, this.color).apply()
        mIsSparkColorUpdate = true

        if (!mIsSparkling) {
            mIsSparkling = true

            mExecutorService.execute(Runnable executor@ {
                checkJoinThread()
                isAnimating = true

                workerThread = Thread(Runnable worker@ {
                    if (spark() == -1) {
                        //error occured
                        for (i in mListeners.indices) {
                            mListeners[i].onServerConnectionFailure()
                        }
                        isAnimating = false
                        mIsSparkling = false
                        return@worker
                    }

                    isAnimating = false
                    mIsSparkling = false

                    if (mRestartAnimation) {
                        mRestartAnimation = false
                        initOpcClient()
                        mExecutorService.execute(object : Runnable {
                            override fun run() {
                                spark(color)
                            }
                        })
                    }
                })
                workerThread?.start()
            })
        }
    }

    private fun spark(): Int {
        var spark = Spark(Spark.buildColors(color, mLedCount * sparkSpan / 100))
        pixelStrip?.animation = spark
        var status: Int?

        isSpanUpdate = false
        setmIsSparkColorUpdate(false)

        while (isAnimating) {
            status = opcClient?.animate()
            if (status == -1) {
                return -1
            }

            if (isBrightnessUpdate) {
                status = opcClient?.setColorCorrection(AppConstants.DEFAULT_GAMMA_CORRECTION,
                        currentColorCorrection / 100f,
                        currentColorCorrection / 100f,
                        currentColorCorrection / 100f)
                if (status == -1) {
                    return -1
                }
                isBrightnessUpdate = false
            }

            if (isSpanUpdate) {
                pixelStrip?.clear()
                spark = Spark(Spark.buildColors(color, mLedCount * sparkSpan / 100))
                pixelStrip?.animation = spark
                isSpanUpdate = false
            }
            if (ismIsSparkColorUpdate()) {
                spark.updateColor(color, mLedCount * sparkSpan / 100)
                setmIsSparkColorUpdate(false)
            }
            try {
                Thread.sleep(Spark.convertSpeed(speed).toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return 0
    }

    fun ismIsSparkColorUpdate(): Boolean {
        return mIsSparkColorUpdate
    }

    fun setmIsSparkColorUpdate(sparkColorUpdate: Boolean) {
        mIsSparkColorUpdate = sparkColorUpdate
    }

    /**
     * Restart Fadecandy Server.
     */
    fun restartServer() {
        mFadecandyClient.startServer()
    }

    private fun createWebsocketClient() {
        Log.v(TAG, "connecting to websocket server")
        val cancelTask = mExecutorService.schedule({
            mWebsocketFuture?.cancel(true)
            for (i in mListeners.indices) {
                mListeners[i].onServerConnectionFailure()
            }
        }, AppConstants.WEBSOCKET_CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)

        val mWebSocketConnectCallback = AsyncHttpClient.WebSocketConnectCallback { ex, webSocket ->
            Log.v(TAG, "onCompleted")
            cancelTask?.cancel(true)

            mWebsocket = webSocket

            if (ex != null) {
                for (i in mListeners.indices) {
                    mListeners[i].onServerConnectionFailure()
                }
                return@WebSocketConnectCallback
            }

            webSocket.stringCallback = WebSocket.StringCallback { message ->
                Log.v(TAG, "messageJson : " + message)
                try {
                    val messageJson = JSONObject(message)

                    if (messageJson.has("type")) {
                        val type = messageJson.getString("type")

                        if (type == "connected_devices_changed") {
                            val devices = messageJson.get("devices") as JSONArray

                            for (i in mListeners.indices) {
                                mListeners[i].onConnectedDeviceChanged(devices.length())
                            }

                        } else if (type == "list_connected_devices") {
                            val devices = messageJson.get("devices") as JSONArray

                            for (i in mListeners.indices) {
                                mListeners[i].onConnectedDeviceChanged(devices.length())
                            }
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            webSocket.closedCallback = CompletedCallback {
                if (!mWebsocketClose) {
                    for (i in mListeners.indices) {
                        mListeners[i].onServerConnectionClosed()
                    }
                }
                eventManager.set()
            }

            for (i in mListeners.indices) {
                mListeners[i].onServerConnectionSuccess()
            }

            sendListConnectedDevices()
            initOpcClient()
        }
        val asyncHttpClient = AsyncHttpClient.getDefaultInstance()

        mWebsocketFuture = asyncHttpClient.websocket("ws://$remoteServerIp:$remoteServerPort/", null, mWebSocketConnectCallback)
    }

    /**
     * get connected devices list from websocket connection.
     */
    private fun sendListConnectedDevices() {
        try {
            val req = JSONObject()
            req.put("type", "list_connected_devices")
            mWebsocket?.send(req.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * close websocket connection.
     */
    private fun closeWebsocket() {
        eventManager.reset()
        mWebsocketClose = true
        mWebsocket?.close()
        try {
            eventManager.waitOne(AppConstants.STOP_WEBSOCKET_TIMEOUT.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mWebsocketClose = false
        mWebsocketFuture?.cancel(true)
    }

    /**
     * restart websocket connection.
     */
    fun restartRemoteConnection() {
        closeWebsocket()
        createWebsocketClient()
    }

    /**
     * mixer effect.
     */
    fun mixer() {
        if (!mIsMixing) {
            mIsMixing = true
            mExecutorService.execute {
                checkJoinThread()
                workerThread = Thread(Runnable {
                    isAnimating = true
                    if (ColorUtils.mixer(this@FadecandySingleton) == -1) {
                        //error occured
                        for (i in mListeners.indices) {
                            mListeners[i].onServerConnectionFailure()
                        }
                    }
                    isAnimating = false
                    mIsMixing = false

                    if (mRestartAnimation) {
                        mRestartAnimation = false
                        initOpcClient()
                        mExecutorService.execute { mixer() }
                    }
                })
                workerThread?.start()
            }
        }
    }

    /**
     * pulse effect.
     *
     * @param color
     */
    fun pulse(color: Int) {
        this.color = color
        prefs.edit().putInt(AppConstants.PREFERENCE_FIELD_COLOR, this.color).apply()

        if (!mIsPulsing) {
            mIsPulsing = true
            mIsSendingRequest = false

            val oldBrightness = currentColorCorrection

            mExecutorService.execute {
                checkJoinThread()
                workerThread = Thread(Runnable {
                    isAnimating = true
                    if (ColorUtils.pulse(this@FadecandySingleton) == -1) {
                        //error occured
                        for (i in mListeners.indices) {
                            mListeners[i].onServerConnectionFailure()
                        }
                    }
                    mIsPulsing = false
                    isAnimating = false

                    if (mRestartAnimation) {
                        mRestartAnimation = false
                        initOpcClient()
                        mExecutorService.execute(object : Runnable {
                            override fun run() {
                                pulse(color)
                            }
                        })
                    } else {
                        setColorCorrection(oldBrightness, true)
                    }
                })
                workerThread?.start()
            }
        }
    }

    fun initOpcClient() {
        opcClient?.close()
        opcClient = OpcClient(ipAddress, serverPort)

        opcClient?.setReuseAddress(true)
        opcClient?.setSoTimeout(AppConstants.SOCKET_TIMEOUT)
        opcClient?.setSoConTimeout(AppConstants.SOCKET_CONNECTION_TIMEOUT)

        opcClient?.addSocketListener {
            for (i in mListeners.indices) {
                mListeners[i].onServerConnectionFailure()
            }
        }
        opcDevice = opcClient?.addDevice()
        pixelStrip = opcDevice?.addPixelStrip(0, mLedCount)
    }

    fun updateConfig(config: String) {
        mFadecandyClient.config = config
    }

    companion object {
        /**
         * Singleton static instance.
         */
        private var mInstance: FadecandySingleton? = null

        private val TAG = FadecandySingleton::class.java.simpleName

        /**
         * Get the static singleton instance.
         *
         * @param context Android application context.
         * @return singleton instance
         */
        fun getInstance(context: Context): FadecandySingleton? {
            if (mInstance == null) {
                mInstance = FadecandySingleton(context)
            }
            return mInstance
        }
    }
}
