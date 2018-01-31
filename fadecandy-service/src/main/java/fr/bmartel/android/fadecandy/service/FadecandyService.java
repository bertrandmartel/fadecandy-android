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
package fr.bmartel.android.fadecandy.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.bmartel.android.fadecandy.activity.UsbEventReceiverActivity;
import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.inter.IUsbEventListener;
import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.android.fadecandy.utils.ManualResetEvent;
import fr.bmartel.android.fadecandy.utils.NotificationHelper;

/**
 * Fadecandy Server service.
 *
 * @author Bertrand Martel
 */
public class FadecandyService extends Service {

    private final static String TAG = FadecandyService.class.getSimpleName();

    /**
     * broadcast action received when user accept USB permission.
     */
    private static final String ACTION_USB_PERMISSION = "fr.bmartel.fadecandy.USB_PERMISSION";

    /**
     * broadcast action received when user click to remove service notification.
     */
    public static final String ACTION_EXIT = "exit";

    /**
     * Usb manager instance.
     */
    private UsbManager mUsbManager;

    /**
     * List of Fadecandy USB device attached.
     */
    private HashMap<Integer, UsbItem> mUsbDevices = new HashMap<>();

    /**
     * Fadecandy server configuration.
     */
    private String mConfig;

    /**
     * Service type persistent or non persistent.
     */
    private ServiceType mServiceType;

    /**
     * Server address.
     */
    private String mServerAddress;

    /**
     * Server port.
     */
    private int mServerPort;

    /**
     * Start Fadecandy server.
     *
     * @param config Fadecandy configuration in Json string.
     * @return start status (0 : OK | 1 : ERROR)
     */
    public native int startFcServer(String config);

    /**
     * Stop fadecandy server (async)
     */
    public native void stopFcServer();

    /**
     * notify Fadecandy server that a Fadecandy USB device has been attached and provide all its characteristics.
     *
     * @param vendorId       Fadecandy device vendor ID
     * @param productId      Fadecandy device product ID
     * @param serialNumber   Fadecandy device serial number (depend on Android version)
     * @param fileDescriptor USB file descriptor (what is used to identify the USB device)
     */
    public native void usbDeviceArrived(int vendorId, int productId, String serialNumber, int fileDescriptor);

    /**
     * notify Fadecandy server that a Fadecandy USB device has been detached.
     *
     * @param fileDescriptor
     */
    public native void usbDeviceLeft(int fileDescriptor);

    /**
     * define that server is started or not.
     */
    private boolean mIsServerRunning;

    /**
     * max time to wait for server close callback (in millis)
     */
    private final static int STOP_SERVER_TIMEOUT = 400;

    /**
     * list of USB listeners.
     */
    private List<IUsbEventListener> mUsbListeners = new ArrayList<>();

    /**
     * monitoring object used to wait for server close before starting server if already started.
     */
    private ManualResetEvent eventManager = new ManualResetEvent(false);

    /**
     * Shared preferences used by Service.
     */
    private SharedPreferences prefs;

    /**
     * define if user has click on notification to close Service.
     */
    private boolean mExit = false;

    /**
     * load shared libraries.
     */
    static {
        System.loadLibrary("websockets");
        System.loadLibrary("fadecandy-server");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate()");

        mExit = false;

        mBinder = new FadecandyServiceBinder(this);

        prefs = this.getSharedPreferences(Constants.PREFERENCE_PREFS, Context.MODE_PRIVATE);
        String configStr = prefs.getString(Constants.PREFERENCE_CONFIG, getDefaultConfig(Constants.DEFAULT_SERVER_ADDRESS, Constants.DEFAULT_SERVER_PORT));
        int serviceType = prefs.getInt(Constants.PREFERENCE_SERVICE_TYPE, ServiceType.getState(Constants.DEFAULT_SERVICE_TYPE));
        mServerPort = prefs.getInt(Constants.PREFERENCE_PORT, Constants.DEFAULT_SERVER_PORT);
        mServerAddress = prefs.getString(Constants.PREFERENCE_IP_ADDRESS, Constants.DEFAULT_SERVER_ADDRESS);

        mServiceType = ServiceType.getServiceType(serviceType);

        mConfig = configStr;

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        initBroadcastReceiver();
    }

    /**
     * Initialize broadcast receiver to receive USB ATTACHED/DETACHED events.
     */
    private void initBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbEventReceiverActivity.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(FadecandyService.ACTION_EXIT);
        registerReceiver(receiver, filter);
    }

    /**
     * add an usb listener to listen for Fadecandy device attached  / detached.
     *
     * @param listener
     */
    public void addUsbListener(IUsbEventListener listener) {
        mUsbListeners.add(listener);
    }

    /**
     * remove an usb listener.
     *
     * @param listener
     */
    public void removeUsbListener(IUsbEventListener listener) {
        mUsbListeners.remove(listener);
    }

    /**
     * initialize usb device list to request permission if not already given for already connected USB devices.
     */
    private void initUsbDeviceList() {
        mUsbDevices.clear();

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == Constants.FC_VENDOR && device.getProductId() == Constants.FC_PRODUCT) {
                dispatchAttached(device);
            }
        }
    }

    /**
     * start Fadecandy server with a new configuration.
     */
    public void startServer(String config) {
        mConfig = config;
        startServer();
    }

    /**
     * stop server & unregister receiver.
     */
    public void clean() {
        stopFcServer();

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        clean();

        super.onDestroy();

        //@todo find why a process still exist at this moment
        if (mExit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * get current Fadecandy server configuration.
     *
     * @return
     */
    public String getConfig() {
        return mConfig;
    }

    /**
     * Set fadecandy configuration. server must be restarted to take effect.
     *
     * @param config
     */
    public void setConfig(String config) {
        mConfig = config;
        prefs.edit().putString(Constants.PREFERENCE_CONFIG, config).apply();
    }

    /**
     * get default Fadecandy server configuration.
     *
     * @return
     */
    public static String getDefaultConfig(final String address, final int serverPort) {
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
                "}";
    }

    /**
     * dispatched attached usb device to fadecandy server native interface.
     *
     * @param device Usb device to be dispatched.
     */
    private void dispatchAttached(UsbDevice device) {
        if (!mUsbManager.hasPermission(device)) {
            Log.v(TAG, "Requesting permissiom to device");
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(device, mPermissionIntent);
        } else {
            Log.v(TAG, "Already has permission : opening device");
            UsbItem item = openDevice(device);
            int fd = item.getConnection().getFileDescriptor();
            dispatchUsb(item, fd);
        }
    }

    /**
     * Broadcast receiver used to receive ATTACHED/DETACHED usb event & usb user permission events .
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.v(TAG, "ACTION_USB_DEVICE_DETACHED");

                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (device != null) {
                    Iterator it = mUsbDevices.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, UsbItem> pair = (Map.Entry) it.next();
                        if (pair.getValue().getDevice().equals(device)) {
                            usbDeviceLeft(pair.getKey());
                            mUsbDevices.remove(pair.getKey());
                            for (int i = 0; i < mUsbListeners.size(); i++) {
                                mUsbListeners.get(i).onUsbDeviceDetached(pair.getValue());
                            }
                            break;
                        }
                    }


                } else {
                    Log.e(TAG, "usb device null");
                }

            } else if (UsbEventReceiverActivity.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                dispatchAttached(device);

            } else if (ACTION_USB_PERMISSION.equals(action)) {
                Log.v(TAG, "ACTION_USB_PERMISSION");
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.v(TAG, "Received permission result");
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null) {
                        UsbItem item = openDevice(device);
                        if (item != null) {
                            int fd = item.getConnection().getFileDescriptor();
                            dispatchUsb(item, fd);
                        }
                    } else {
                        Log.v(TAG, "permission denied for device " + device);
                    }
                } else {
                    Log.e(TAG, "permission denied");
                }
            } else if (FadecandyService.ACTION_EXIT.equals(action)) {
                Log.v(TAG, "stopping FadecandyService");

                mExit = true;
                stopForeground(true);
                stopSelf();
            }
        }
    };

    /**
     * append USB device to usb list & pass to native interface
     *
     * @param device USB device object
     * @param fd     USB device file descriptor
     */
    private void dispatchUsb(UsbItem device, int fd) {
        String serialNum = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            serialNum = device.getDevice().getSerialNumber();
        } else {
            serialNum = "" + device.getDevice().getProductId();
        }

        mUsbDevices.put(fd, device);

        for (int i = 0; i < mUsbListeners.size(); i++) {
            mUsbListeners.get(i).onUsbDeviceAttached(device);
        }

        usbDeviceArrived(device.getDevice().getVendorId(), device.getDevice().getProductId(), serialNum, fd);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private FadecandyServiceBinder mBinder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //this is the activity which will be opened when user click on notification
        Intent testIntent = new Intent();
        if (intent != null && intent.hasExtra(Constants.SERVICE_EXTRA_ACTIVITY)) {
            testIntent.setComponent(ComponentName.unflattenFromString(intent.getStringExtra(Constants.SERVICE_EXTRA_ACTIVITY)));
        }

        ServiceType serviceTypeOverride;

        if (intent != null && intent.hasExtra(Constants.SERVICE_EXTRA_SERVICE_TYPE)) {
            serviceTypeOverride = ServiceType.getServiceType(intent.getIntExtra(Constants.SERVICE_EXTRA_SERVICE_TYPE, ServiceType.NON_PERSISTENT_SERVICE.ordinal()));
        } else {
            serviceTypeOverride = mServiceType;
        }

        switch (serviceTypeOverride) {
            case NON_PERSISTENT_SERVICE:
                Log.v(TAG, "starting NON_PERSISTENT_SERVICE...");
                return START_NOT_STICKY;
            case PERSISTENT_SERVICE:
                startForeground(4242, NotificationHelper.createNotification(this, null, testIntent));
                Log.v(TAG, "starting PERSISTENT_SERVICE...");
                return START_STICKY;
        }

        return START_NOT_STICKY;
    }

    /**
     * start Fadecandy server
     *
     * @return server status (0 : started | 1 : not stated (error))
     */
    public int startServer() {
        if (isServerRunning()) {
            eventManager.reset();
            stopServer();
            try {
                eventManager.waitOne(STOP_SERVER_TIMEOUT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mConfig == null || mConfig.equals("")) {
            mConfig = getDefaultConfig(mServerAddress, mServerPort);
        }

        int status = startFcServer(mConfig);

        initUsbDeviceList();

        if (status == 0) {
            Log.v(TAG, "server running");
            mIsServerRunning = true;
        } else {
            Log.e(TAG, "error occured while starting server");
        }

        return status;
    }

    /**
     * Stop Fadecandy server.
     *
     * @return stop status
     */
    public int stopServer() {
        Log.v(TAG, "stop server");
        stopFcServer();
        mIsServerRunning = false;
        return 0;
    }

    /**
     * define if server is started or not.
     *
     * @return
     */
    public boolean isServerRunning() {
        return mIsServerRunning;
    }

    /**
     * called from native to notify server has been closed successfully.
     */
    private void onServerClose() {
        eventManager.set();
    }

    /**
     * called from native to process USB transfer.
     *
     * @param fileDescriptor USB device file descriptor.
     * @param timeout        transfer timeout.
     * @param data           data to be transferred.
     * @return transfer status
     */
    private int bulkTransfer(int fileDescriptor, int timeout, byte[] data) {
        if (mUsbDevices.containsKey(fileDescriptor)) {
            if (mUsbDevices.get(fileDescriptor).getConnection() != null) {
                return mUsbDevices.get(fileDescriptor).getConnection().bulkTransfer(mUsbDevices.get(fileDescriptor).getUsbEndpoint(), data, data.length, timeout);
            }
        } else {
            Log.e(TAG, "device with fd " + fileDescriptor + " not found");
        }
        return -1;
    }

    private void onParseError(int parseOffset, String message) {
        Toast.makeText(this, "Parse error at character " + parseOffset + ": " + message, Toast.LENGTH_LONG).show();
    }

    /**
     * Open USB device & choose USBEndpoint.
     *
     * @param device Usb device object.
     * @return Ubs item composed of USBDevice , USBEndpoint & UsbConnection
     */
    private UsbItem openDevice(UsbDevice device) {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        try {
            UsbDeviceConnection connection = manager.openDevice(device);

            Log.v("USB", "Device name=" + device.getDeviceName());

            UsbInterface intf = null;

            for (int interfaceIndex = 0; interfaceIndex < device.getInterfaceCount(); interfaceIndex++) {

                UsbInterface usbInterface = device.getInterface(interfaceIndex);

                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_VENDOR_SPEC) {
                    intf = usbInterface;
                }
            }

            if (intf == null) {
                intf = device.getInterface(0);
            }

            UsbEndpoint endpoint = intf.getEndpoint(0);

            if (connection != null) {
                connection.claimInterface(intf, true);
            } else {
                Log.e(TAG, "USB connection error");
                return null;
            }

            return new UsbItem(device, connection, endpoint);

        } catch (IllegalArgumentException e) {
        }
        return null;
    }

    /**
     * Retrieve service type (persistent or non persistent).
     *
     * @return
     */
    public ServiceType getServiceType() {
        return mServiceType;
    }

    /**
     * Set service type.
     *
     * @param serviceType
     */
    public void setServiceType(ServiceType serviceType) {
        this.mServiceType = serviceType;
        prefs.edit().putInt(Constants.PREFERENCE_SERVICE_TYPE, ServiceType.getState(serviceType)).apply();
    }

    /**
     * Retrieve server port.
     *
     * @return
     */
    public int getServerPort() {
        return mServerPort;
    }

    /**
     * Retrieve server addresss.
     *
     * @return
     */
    public String getIpAddress() {
        return mServerAddress;
    }

    /**
     * Set server port.
     *
     * @param port
     */
    public void setServerPort(int port) {
        this.mServerPort = port;
        prefs.edit().putInt(Constants.PREFERENCE_PORT, port).apply();
    }

    /**
     * Set server address.
     *
     * @param ip
     */
    public void setServerAddress(String ip) {
        this.mServerAddress = ip;
        prefs.edit().putString(Constants.PREFERENCE_IP_ADDRESS, ip).apply();
    }

    /**
     * Retrieve list of Fadecandy USB device attached.
     *
     * @return
     */
    public HashMap<Integer, UsbItem> getUsbDeviceMap() {
        return mUsbDevices;
    }
}