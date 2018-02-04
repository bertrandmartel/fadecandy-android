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
package fr.bmartel.android.fadecandy.client

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

import java.util.HashMap

import fr.bmartel.android.fadecandy.constant.Constants
import fr.bmartel.android.fadecandy.inter.IFcServerEventListener
import fr.bmartel.android.fadecandy.inter.IUsbEventListener
import fr.bmartel.android.fadecandy.model.ServerError
import fr.bmartel.android.fadecandy.model.ServiceType
import fr.bmartel.android.fadecandy.model.UsbItem
import fr.bmartel.android.fadecandy.service.FadecandyService
import fr.bmartel.android.fadecandy.service.FadecandyServiceBinder

/**
 * Fadecandy client wrapper to be used to bind service & call service API.
 *
 * @author Bertrand Martel
 */
class FadecandyClient(
        /**
         * android context.
         */
        private val context: Context,
        /**
         * Fadecandy server event listener.
         */
        private val listener: IFcServerEventListener?,
        /**
         * Fadencandy USB event listener.
         */
        private val usbListener: IUsbEventListener?,
        /**
         * activity intent name that will be opened when user click on service notification.
         */
        private val activity: String) {

    /**
     * Fadecandy service instance.
     */
    private var fadecandyService: FadecandyService? = null

    /**
     * define if fadecandy service is bound.
     */
    private var mBound: Boolean = false

    /**
     * define if server should be started.
     */
    private var mShouldStartServer: Boolean = false

    /**
     * intent used to launch fadecandy service.
     */
    private var fadecandyServiceIntent: Intent? = null

    /**
     * Service type to override if connect was called with ServiceType argument.
     */
    private var mOverrideServiceType: ServiceType? = null

    /**
     * define if service type should be override when calling startService.
     */
    private var mShouldOverrideServiceType: Boolean = false

    /**
     * broadcast receiver used to catch user click on service notification.
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FadecandyService.ACTION_EXIT) {
                Log.v(TAG, "Exit event received : disconnecting")
                disconnect()
            }
        }
    }

    /**
     * Fadecandy service connection.
     */
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.v(TAG, "fadecandy service connected")
            fadecandyService = (service as FadecandyServiceBinder).service

            if (usbListener != null) {
                fadecandyService?.addUsbListener(listener = usbListener)
            }

            if (mShouldStartServer) {
                if (fadecandyService?.startServer() == 0) {
                    listener?.onServerStart()
                } else {
                    listener?.onServerError(error = ServerError.START_SERVER_ERROR)
                }
            }
            mShouldStartServer = false
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.v(TAG, "fadecandy disconected")
        }
    }

    /**
     * get server status.
     *
     * @return true if server is running, false if server is stopped
     */
    val isServerRunning: Boolean
        get() {
            if (mBound && fadecandyService != null) {
                return (fadecandyService?.isServerRunning ?: false)
            } else {
                Log.e(TAG, "service not started")
            }
            return false
        }

    /**
     * get service type (persistent or non persistent)
     *
     * @return
     */
    /**
     * set service type (persistent or non persistent)
     *
     * @param serviceType
     */
    var serviceType: ServiceType?
        get() = if (mBound && fadecandyService != null) {
            fadecandyService?.serviceType
        } else null
        set(serviceType) {
            if (mBound && fadecandyService != null) {
                fadecandyService?.serviceType = serviceType
            }
        }

    /**
     * get server port value
     *
     * @return
     */
    /**
     * set server port value.
     *
     * @param port
     */
    var serverPort: Int
        get() = if (mBound && fadecandyService != null) {
            fadecandyService?.serverPort ?: Constants.DEFAULT_SERVER_PORT
        } else DEFAULT_PORT
        set(port) {
            if (mBound && fadecandyService != null) {
                fadecandyService?.serverPort = port
            }
        }

    /**
     * get server ip/hostname value
     *
     * @return
     */
    val ipAddress: String
        get() = if (mBound && fadecandyService != null) {
            fadecandyService?.ipAddress ?: Constants.DEFAULT_SERVER_ADDRESS
        } else ""

    /**
     * get Fadecandy local configuration.
     *
     * @return
     */
    /**
     * Set fadecandy configuration.
     *
     * @param config
     */
    var config: String?
        get() = if (mBound && fadecandyService != null) {
            fadecandyService?.config
        } else ""
        set(config) {
            fadecandyService?.config = (config ?: "")
        }

    /**
     * get list of Fadecandy USB device connected.
     *
     * @return
     */
    val usbDeviceMap: HashMap<Int?, UsbItem?>
        get() = if (mBound && fadecandyService != null) {
            fadecandyService?.usbDeviceMap ?: HashMap()
        } else HashMap()

    val defaultConfig: String
        get() = FadecandyService.getDefaultConfig(
                address = fadecandyService?.ipAddress,
                serverPort = fadecandyService?.serverPort ?: Constants.DEFAULT_SERVER_PORT)

    /**
     * connect with override of service type
     *
     * @param serviceType
     */
    fun connect(serviceType: ServiceType) {
        mShouldOverrideServiceType = true
        mOverrideServiceType = serviceType
        bindService()
        registerReceiver()
    }

    /**
     * bind service and register receiver.
     */
    fun connect() {
        bindService()
        registerReceiver()
    }

    /**
     * register receiver.
     */
    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(FadecandyService.ACTION_EXIT)
        context.registerReceiver(receiver, filter)
    }

    /**
     * disconnect from service (that will not close server if service is not destroyed eg service not in persistent mode).
     */
    fun disconnect() {
        if (mBound) {
            context.unbindService(mServiceConnection)
            context.unregisterReceiver(receiver)
            mBound = false
            listener?.onServerClose()
        }
    }

    /**
     * force service to stop.
     */
    fun stopService() {
        context.stopService(fadecandyServiceIntent)
    }

    /**
     * bind to Fadecandy service.
     */
    private fun bindService() {
        fadecandyServiceIntent = Intent()
        fadecandyServiceIntent?.setClassName(context, SERVICE_NAME)
        fadecandyServiceIntent?.putExtra(Constants.SERVICE_EXTRA_ACTIVITY, activity)

        if (mShouldOverrideServiceType) {
            fadecandyServiceIntent?.putExtra(Constants.SERVICE_EXTRA_SERVICE_TYPE, mOverrideServiceType!!.ordinal)
        }

        context.startService(fadecandyServiceIntent)

        mBound = context.bindService(fadecandyServiceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE)

        if (mBound) {
            Log.v(TAG, "service started")
        } else {
            Log.e(TAG, "service not started")
        }
    }

    /**
     * start Fadecandy server.
     * If service is not started/bounded, server will be started as soon as service is started & bounded.
     */
    fun startServer() {
        if (mBound && fadecandyService != null) {
            if (fadecandyService?.startServer() == 0) {
                listener?.onServerStart()
            } else {
                listener?.onServerError(error = ServerError.START_SERVER_ERROR)
            }
        } else {
            mShouldStartServer = true
            connect()
        }
    }

    /**
     * start Fadecandy server after setting server configuration.
     * If service is not started/bounded, server will be started as soon as service is started & bounded.
     */
    fun startServer(config: String) {
        if (mBound && fadecandyService != null) {
            if (fadecandyService?.startServer(config = config) == 0) {
                listener?.onServerStart()
            } else {
                listener?.onServerError(error = ServerError.START_SERVER_ERROR)
            }
        } else {
            mShouldStartServer = true
            connect()
        }
    }

    /**
     * close Fadecandy server.
     */
    fun closeServer() {
        if (mBound && fadecandyService != null) {
            if (fadecandyService?.stopServer() == 0) {
                listener?.onServerClose()
            } else {
                listener?.onServerError(error = ServerError.CLOSE_SERVER_ERROR)
            }
        } else {
            Log.e(TAG, "service not started.")
        }
    }

    /**
     * set server IP/hostname value
     *
     * @param ip
     */
    fun setServerAddress(ip: String?) {
        if (mBound && fadecandyService != null) {
            fadecandyService?.setServerAddress(ip = ip)
        }
    }

    companion object {
        private val TAG = FadecandyClient::class.java.simpleName
        /**
         * default port if service is down.
         */
        val DEFAULT_PORT = 7890

        /**
         * service intent name.
         */
        val SERVICE_NAME = "fr.bmartel.android.fadecandy.service.FadecandyService"
    }
}
