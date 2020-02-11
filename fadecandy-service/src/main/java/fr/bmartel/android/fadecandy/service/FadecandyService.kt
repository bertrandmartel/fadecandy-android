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
package fr.bmartel.android.fadecandy.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import fr.bmartel.android.fadecandy.activity.UsbEventReceiverActivity
import fr.bmartel.android.fadecandy.constant.Constants
import fr.bmartel.android.fadecandy.inter.IUsbEventListener
import fr.bmartel.android.fadecandy.model.ServiceType
import fr.bmartel.android.fadecandy.model.UsbItem
import fr.bmartel.android.fadecandy.utils.ManualResetEvent
import fr.bmartel.android.fadecandy.utils.NotificationHelper
import java.util.*

/**
 * Fadecandy Server service.
 *
 * @author Bertrand Martel
 */
class FadecandyService : Service() {

    /**
     * Usb manager instance.
     */
    lateinit var mUsbManager: UsbManager

    /**
     * List of Fadecandy USB device attached.
     */
    /**
     * Retrieve list of Fadecandy USB device attached.
     *
     * @return
     */
    val usbDeviceMap = HashMap<Int?, UsbItem?>()

    /**
     * Server address.
     */
    /**
     * Retrieve server addresss.
     *
     * @return
     */
    var ipAddress: String? = null
        set(address) {
            field = address
            prefs.edit().putString(Constants.PREFERENCE_IP_ADDRESS, address).apply()
        }

    /**
     * define that server is started or not.
     */
    /**
     * define if server is started or not.
     *
     * @return
     */
    var isServerRunning: Boolean = false
        private set

    /**
     * list of USB listeners.
     */
    private val mUsbListeners = ArrayList<IUsbEventListener>()

    /**
     * monitoring object used to wait for server close before starting server if already started.
     */
    private val eventManager = ManualResetEvent(open = false)

    /**
     * Shared preferences used by Service.
     */
    private lateinit var prefs: SharedPreferences

    /**
     * define if user has click on notification to close Service.
     */
    private var mExit = false

    /**
     * get current Fadecandy server configuration.
     *
     * @return
     */
    /**
     * Set fadecandy configuration. server must be restarted to take effect.
     *
     * @param config
     */
    var config: String = ""
        set(conf) {
            field = conf
            prefs.edit().putString(Constants.PREFERENCE_CONFIG, conf).apply()
        }

    /**
     * Broadcast receiver used to receive ATTACHED/DETACHED usb event & usb user permission events .
     */
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                Log.v(TAG, "ACTION_USB_DEVICE_DETACHED")
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

                if (device != null) {
                    val it = usbDeviceMap.entries.iterator()
                    while (it.hasNext()) {
                        val pair = it.next() as Map.Entry<Int, UsbItem>
                        if (pair.value.device == device) {
                            usbDeviceLeft(pair.key)
                            usbDeviceMap.remove(pair.key)
                            for (i in mUsbListeners.indices) {
                                mUsbListeners[i].onUsbDeviceDetached(usbItem = pair.value)
                            }
                            break
                        }
                    }
                } else {
                    Log.e(TAG, "usb device null")
                }
            } else if (UsbEventReceiverActivity.ACTION_USB_DEVICE_ATTACHED == action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                dispatchAttached(device = device)
            } else if (ACTION_USB_PERMISSION == action) {
                Log.v(TAG, "ACTION_USB_PERMISSION")
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.v(TAG, "Received permission result")
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

                    if (device != null) {
                        val item = openDevice(device = device)
                        if (item != null) {
                            val fd = item.connection.fileDescriptor
                            dispatchUsb(device = item, fd = fd)
                        }
                    } else {
                        Log.v(TAG, "permission denied for device")
                    }
                } else {
                    Log.e(TAG, "permission denied")
                }
            } else if (FadecandyService.ACTION_EXIT == action) {
                Log.v(TAG, "stopping FadecandyService")

                mExit = true
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private lateinit var mBinder: FadecandyServiceBinder

    /**
     * Retrieve service type (persistent or non persistent).
     *
     * @return
     */
    /**
     * Set service type.
     *
     * @param serviceType
     */
    var serviceType: ServiceType? = Constants.DEFAULT_SERVICE_TYPE
        set(type) {
            field = type
            prefs.edit().putInt(Constants.PREFERENCE_SERVICE_TYPE, ServiceType.getState(type)).apply()
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
    var serverPort: Int = 0
        set(port) {
            field = port
            prefs.edit().putInt(Constants.PREFERENCE_PORT, port).apply()
        }

    /**
     * Start Fadecandy server.
     *
     * @param config Fadecandy configuration in Json string.
     * @return start status (0 : OK | 1 : ERROR)
     */
    external fun startFcServer(config: String): Int

    /**
     * Stop fadecandy server (async)
     */
    external fun stopFcServer()

    /**
     * notify Fadecandy server that a Fadecandy USB device has been attached and provide all its characteristics.
     *
     * @param vendorId       Fadecandy device vendor ID
     * @param productId      Fadecandy device product ID
     * @param serialNumber   Fadecandy device serial number (depend on Android version)
     * @param fileDescriptor USB file descriptor (what is used to identify the USB device)
     */
    external fun usbDeviceArrived(vendorId: Int, productId: Int, serialNumber: String, fileDescriptor: Int)

    /**
     * notify Fadecandy server that a Fadecandy USB device has been detached.
     *
     * @param fileDescriptor
     */
    external fun usbDeviceLeft(fileDescriptor: Int)

    override fun onCreate() {
        super.onCreate()

        mExit = false

        mBinder = FadecandyServiceBinder(service = this)

        prefs = this.getSharedPreferences(Constants.PREFERENCE_PREFS, Context.MODE_PRIVATE)
        config = prefs.getString(Constants.PREFERENCE_CONFIG, getDefaultConfig(Constants.DEFAULT_SERVER_ADDRESS, Constants.DEFAULT_SERVER_PORT))
        serviceType = ServiceType.getServiceType(prefs.getInt(Constants.PREFERENCE_SERVICE_TYPE, ServiceType.getState(Constants.DEFAULT_SERVICE_TYPE)))
        serverPort = prefs.getInt(Constants.PREFERENCE_PORT, Constants.DEFAULT_SERVER_PORT)
        ipAddress = prefs.getString(Constants.PREFERENCE_IP_ADDRESS, Constants.DEFAULT_SERVER_ADDRESS)

        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        initBroadcastReceiver()
    }

    /**
     * Initialize broadcast receiver to receive USB ATTACHED/DETACHED events.
     */
    private fun initBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbEventReceiverActivity.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(FadecandyService.ACTION_EXIT)
        registerReceiver(receiver, filter)
    }

    /**
     * add an usb listener to listen for Fadecandy device attached  / detached.
     *
     * @param listener
     */
    fun addUsbListener(listener: IUsbEventListener) {
        mUsbListeners.add(listener)
    }

    /**
     * remove an usb listener.
     *
     * @param listener
     */
    fun removeUsbListener(listener: IUsbEventListener) {
        mUsbListeners.remove(listener)
    }

    /**
     * initialize usb device list to request permission if not already given for already connected USB devices.
     */
    private fun initUsbDeviceList() {
        usbDeviceMap.clear()

        val deviceList = mUsbManager.deviceList
        val deviceIterator = deviceList?.values?.iterator()

        while ((deviceIterator?.hasNext() == true)) {
            val device = deviceIterator.next()
            if (device?.vendorId == Constants.FC_VENDOR && device.productId == Constants.FC_PRODUCT) {
                dispatchAttached(device = device)
            }
        }
    }

    /**
     * start Fadecandy server with a new configuration.
     */
    fun startServer(config: String): Int {
        this.config = config
        return startServer()
    }

    /**
     * stop server & unregister receiver.
     */
    fun clean() {
        stopFcServer()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        clean()

        super.onDestroy()

        //@todo find why a process still exist at this moment
        if (mExit) {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    /**
     * dispatched attached usb device to fadecandy server native interface.
     *
     * @param device Usb device to be dispatched.
     */
    private fun dispatchAttached(device: UsbDevice) {
        if (mUsbManager.hasPermission(device) != true) {
            Log.v(TAG, "Requesting permissiom to device")
            val mPermissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            mUsbManager.requestPermission(device, mPermissionIntent)
        } else {
            Log.v(TAG, "Already has permission : opening device")
            val item = openDevice(device = device)
            val fd = item?.connection?.fileDescriptor
            dispatchUsb(device = item, fd = fd)
        }
    }

    /**
     * append USB device to usb list & pass to native interface
     *
     * @param device USB device object
     * @param fd     USB device file descriptor
     */
    private fun dispatchUsb(device: UsbItem?, fd: Int?) {
        var serialNum: String?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            serialNum = device?.device?.serialNumber
        } else {
            serialNum = "${device?.device?.productId}"
        }

        usbDeviceMap.set(fd, device)

        for (i in mUsbListeners.indices) {
            mUsbListeners[i].onUsbDeviceAttached(usbItem = device)
        }
        System.out.println("vendor id : " + device?.device?.vendorId)
        System.out.println("product id : " + device?.device?.productId)
        usbDeviceArrived(
                vendorId = device?.device?.vendorId ?: 0,
                productId = device?.device?.productId ?: 0,
                serialNumber = serialNum ?: "",
                fileDescriptor = fd ?: 0)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //this is the activity which will be opened when user click on notification
        val testIntent = Intent()
        if (intent != null && intent.hasExtra(Constants.SERVICE_EXTRA_ACTIVITY)) {
            testIntent.component = ComponentName.unflattenFromString(intent.getStringExtra(Constants.SERVICE_EXTRA_ACTIVITY))
        }

        val serviceTypeOverride: ServiceType?

        if (intent != null && intent.hasExtra(Constants.SERVICE_EXTRA_SERVICE_TYPE)) {
            serviceTypeOverride = ServiceType.getServiceType(intent.getIntExtra(Constants.SERVICE_EXTRA_SERVICE_TYPE, ServiceType.NON_PERSISTENT_SERVICE.ordinal))
        } else {
            serviceTypeOverride = serviceType
        }

        when (serviceTypeOverride) {
            ServiceType.NON_PERSISTENT_SERVICE -> {
                Log.v(TAG, "starting NON_PERSISTENT_SERVICE...")
                return Service.START_NOT_STICKY
            }
            ServiceType.PERSISTENT_SERVICE -> {
                startForeground(1, NotificationHelper.createNotification(this, null, testIntent))
                Log.v(TAG, "starting PERSISTENT_SERVICE...")
                return Service.START_STICKY
            }
        }

        return Service.START_NOT_STICKY
    }

    /**
     * start Fadecandy server
     *
     * @return server status (0 : started | 1 : not stated (error))
     */
    fun startServer(): Int {
        if (isServerRunning) {
            eventManager.reset()
            stopServer()
            try {
                eventManager.waitOne(milliseconds = STOP_SERVER_TIMEOUT.toLong())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (config == "") {
            config = getDefaultConfig(address = ipAddress, serverPort = serverPort)
        }

        val status = startFcServer(config = config)

        initUsbDeviceList()

        if (status == 0) {
            Log.v(TAG, "server running")
            isServerRunning = true
        } else {
            Log.e(TAG, "error occured while starting server")
        }
        return status
    }

    /**
     * Stop Fadecandy server.
     *
     * @return stop status
     */
    fun stopServer(): Int {
        Log.v(TAG, "stop server")
        stopFcServer()
        isServerRunning = false
        return 0
    }

    /**
     * called from native to notify server has been closed successfully.
     */
    private fun onServerClose() {
        eventManager.set()
    }

    /**
     * called from native to process USB transfer.
     *
     * @param fileDescriptor USB device file descriptor.
     * @param timeout        transfer timeout.
     * @param data           data to be transferred.
     * @return transfer status
     */
    private fun bulkTransfer(fileDescriptor: Int, timeout: Int, data: ByteArray): Int {
        if (usbDeviceMap.containsKey(fileDescriptor)) {
            return usbDeviceMap[fileDescriptor]?.connection?.bulkTransfer(usbDeviceMap[fileDescriptor]?.usbEndpoint, data, data.size, timeout)
                    ?: 0
        } else {
            Log.e(TAG, "device with fd $fileDescriptor not found")
        }
        return -1
    }

    private fun onParseError(parseOffset: Int, message: String) {
        Toast.makeText(this, "Parse error at character $parseOffset: $message", Toast.LENGTH_LONG).show()
    }

    /**
     * Open USB device & choose USBEndpoint.
     *
     * @param device Usb device object.
     * @return Ubs item composed of USBDevice , USBEndpoint & UsbConnection
     */
    private fun openDevice(device: UsbDevice?): UsbItem? {

        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

        try {
            val connection = manager.openDevice(device)
            Log.v("USB", "Device name=" + device?.deviceName)
            var intf: UsbInterface? = null

            for (interfaceIndex in 0 until (device?.interfaceCount ?: 0)) {
                val usbInterface = device?.getInterface(interfaceIndex)
                if (usbInterface?.interfaceClass == UsbConstants.USB_CLASS_VENDOR_SPEC) {
                    intf = usbInterface
                }
            }

            if (intf == null) {
                intf = device?.getInterface(0)
            }

            val endpoint = intf?.getEndpoint(0)

            if (connection != null) {
                connection.claimInterface(intf, true)
            } else {
                Log.e(TAG, "USB connection error")
                return null
            }
            return UsbItem(device = device, connection = connection, usbEndpoint = endpoint)

        } catch (e: IllegalArgumentException) {
        }
        return null
    }

    companion object {

        private val TAG = FadecandyService::class.java.simpleName

        /**
         * broadcast action received when user accept USB permission.
         */
        private val ACTION_USB_PERMISSION = "fr.bmartel.fadecandy.USB_PERMISSION"

        /**
         * broadcast action received when user click to remove service notification.
         */
        val ACTION_EXIT = "exit"

        /**
         * max time to wait for server close callback (in millis)
         */
        private val STOP_SERVER_TIMEOUT = 400

        /**
         * load shared libraries.
         */
        init {
            System.loadLibrary("websockets")
            System.loadLibrary("fadecandy-server")
        }

        /**
         * get default Fadecandy server configuration.
         *
         * @return
         */
        fun getDefaultConfig(address: String?, serverPort: Int): String {
            return "{\n" +
                    "    \"listen\": [\"" + address + "\", " + serverPort + "],\n" +
                    "    \"verbose\": true,\n" +
                    "\n" +
                    "    \"color\": {\n" +
                    "        \"gamma\": 2.5,\n" +
                    "        \"whitepoint\": [1.0, 1.0, 1.0]\n" +
                    "    },\n" +
                    "\n" +
                    "    \"devices\": [\n" +
                    "        {\n" +
                    "            \"type\": \"fadecandy\",\n" +
                    "            \"map\": [\n" +
                    "                [ 0, 0, 0, 512 ]\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}"
        }
    }
}