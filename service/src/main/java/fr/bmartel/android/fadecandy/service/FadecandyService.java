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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.bmartel.android.fadecandy.FadecandyServiceBinder;
import fr.bmartel.android.fadecandy.ServiceType;
import fr.bmartel.android.fadecandy.activity.UsbEventReceiverActivity;
import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.model.FadecandyColor;
import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.android.fadecandy.model.FadecandyDevice;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.android.fadecandy.utils.ManualResetEvent;
import fr.bmartel.android.fadecandy.utils.NotificationHelper;

/**
 * @author Bertrand Martel
 */
public class FadecandyService extends Service {

    private final static String TAG = FadecandyService.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "fr.bmartel.fadecandy.USB_PERMISSION";

    public static final String ACTION_EXIT = "exit";

    private UsbManager mUsbManager;

    private HashMap<Integer, UsbItem> mUsbDevices = new HashMap<>();

    private FadecandyConfig mConfig;

    private ServiceType mServiceType;

    private String mServerAddress;

    private int mServerPort;

    public native int startFcServer(String config);

    public native void stopFcServer();

    public native void usbDeviceArrived(int vendorId, int productId, String serialNumber, int fileDescriptor);

    public native void usbDeviceLeft(int fileDescriptor);

    private boolean mIsServerRunning;

    private final static int STOP_SERVER_TIMEOUT = 400;

    private ManualResetEvent eventManager = new ManualResetEvent(false);

    // Load the .so
    static {
        System.loadLibrary("websockets");
        System.loadLibrary("fadecandy-server");
    }

    private SharedPreferences prefs;

    private boolean mExit = false;

    private boolean mUsbInit = false;

    // Setup
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate()");

        mExit = false;

        mBinder = new FadecandyServiceBinder(this);

        prefs = this.getSharedPreferences(Constants.PREFERENCE_PREFS, Context.MODE_PRIVATE);
        String configStr = prefs.getString(Constants.PREFERENCE_CONFIG, getDefaultConfig().toJsonString());
        int serviceType = prefs.getInt(Constants.PREFERENCE_SERVICE_TYPE, ServiceType.getState(Constants.DEFAULT_SERVICE_TYPE));
        mServerPort = prefs.getInt(Constants.PREFERENCE_PORT, Constants.DEFAULT_SERVER_PORT);
        mServerAddress = prefs.getString(Constants.PREFERENCE_IP_ADDRESS, Constants.DEFAULT_SERVER_ADDRESS);

        mServiceType = ServiceType.getServiceType(serviceType);

        try {
            mConfig = new FadecandyConfig(new JSONObject(configStr));
        } catch (JSONException e) {
            mConfig = getDefaultConfig();
            e.printStackTrace();
        }

        Log.v(TAG, "Fadecandy config : " + mConfig.toJsonString());

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
     * initialize usb device list to request permission if not already given for already connected USB devices.
     */
    private void initUsbDeviceList() {

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
    public void startServer(FadecandyConfig config) {
        mConfig = config;
        startServer();
    }

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

        if (mExit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * get current Fadecandy server configuration.
     *
     * @return
     */
    public FadecandyConfig getConfig() {
        return mConfig;
    }

    /**
     * get default Fadecandy server configuration.
     *
     * @return
     */
    private FadecandyConfig getDefaultConfig() {

        List<Float> defaultGamma = new ArrayList<>();
        defaultGamma.add(1.0f);
        defaultGamma.add(1.0f);
        defaultGamma.add(1.0f);

        List<FadecandyDevice> fcDevices = new ArrayList<>();

        List<List<Integer>> map = new ArrayList<>();
        List<Integer> mapItem = new ArrayList<>();
        mapItem.add(0);
        mapItem.add(0);
        mapItem.add(0);
        mapItem.add(512);
        map.add(mapItem);

        fcDevices.add(new FadecandyDevice(map, "fadecandy"));

        return new FadecandyConfig(Constants.DEFAULT_SERVER_ADDRESS, Constants.DEFAULT_SERVER_PORT, new FadecandyColor(defaultGamma, 2.5f), true, fcDevices);
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

            Log.v(TAG, "Received permission result");
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.v(TAG, "ACTION_USB_DEVICE_DETACHED");

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (device != null) {
                    Iterator it = mUsbDevices.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, UsbItem> pair = (Map.Entry) it.next();
                        if (pair.getValue().getDevice().equals(device)) {
                            usbDeviceLeft(pair.getKey());
                            mUsbDevices.remove(pair.getKey());
                            break;
                        }
                    }


                } else {
                    Log.e(TAG, "usb device null");
                }

            } else if (UsbEventReceiverActivity.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.v(TAG, "ACTION_USB_DEVICE_ATTACHED");

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                dispatchAttached(device);

            } else if (ACTION_USB_PERMISSION.equals(action)) {

                Log.v(TAG, "ACTION_USB_PERMISSION");

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    Log.v(TAG, "Received permission result OK");

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null) {

                        Log.v(TAG, "Openning device");

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

    private void dispatchUsb(UsbItem device, int fd) {

        String serialNum = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            serialNum = device.getDevice().getSerialNumber();
        } else {
            serialNum = "" + device.getDevice().getProductId();
        }

        mUsbDevices.put(fd, device);

        usbDeviceArrived(device.getDevice().getVendorId(), device.getDevice().getProductId(), serialNum, fd);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private FadecandyServiceBinder mBinder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent testIntent = new Intent();
        testIntent.setComponent(ComponentName.unflattenFromString(intent.getStringExtra(Constants.SERVICE_EXTRA_ACTIVITY)));

        switch (mServiceType) {
            case NON_PERSISTENT_SERVICE:
                Log.v(TAG, "NON_PERSISTENT_SERVICE");
                return START_NOT_STICKY;
            case PERSISTENT_SERVICE:
                startForeground(4242, NotificationHelper.createNotification(this, null, testIntent));
                Log.v(TAG, "PERSISTENT_SERVICE");
                return START_STICKY;
        }

        return START_NOT_STICKY;
    }

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

        mConfig = new FadecandyConfig(mServerAddress, mServerPort, mConfig.getFcColor(), mConfig.isVerbose(), mConfig.getFcDevices());

        int status = startFcServer(mConfig.toJsonString());

        if (!mUsbInit) {
            initUsbDeviceList();
            mUsbInit = true;
        }

        if (status == 0) {
            Log.v(TAG, "server running");
            mIsServerRunning = true;
        } else {
            Log.e(TAG, "error occured while starting server");
        }

        return status;
    }

    public int stopServer() {
        Log.v(TAG, "stop server");
        stopFcServer();
        mIsServerRunning = false;
        return 0;
    }

    public boolean isServerRunning() {
        return mIsServerRunning;
    }

    private void onServerClose() {
        eventManager.set();
    }

    private int bulkTransfer(int fileDescriptor, int timeout, byte[] data) {

        if (mUsbDevices.containsKey(fileDescriptor)) {
            Log.v(TAG, "sending " + data.length + " bytes");
            if (mUsbDevices.get(fileDescriptor).getConnection() != null) {
                return mUsbDevices.get(fileDescriptor).getConnection().bulkTransfer(mUsbDevices.get(fileDescriptor).getUsbEndpoint(), data, data.length, timeout);
            }
        } else {
            Log.e(TAG, "device with fd " + fileDescriptor + " not found");
        }
        return -1;
    }

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

    public ServiceType getServiceType() {
        return mServiceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.mServiceType = serviceType;
        prefs.edit().putInt(Constants.PREFERENCE_SERVICE_TYPE, ServiceType.getState(serviceType)).apply();
    }

    public int getServerPort() {
        return mServerPort;
    }

    public String getIpAddress() {
        return mServerAddress;
    }

    public void setServerPort(int port) {
        this.mServerPort = port;
        prefs.edit().putInt(Constants.PREFERENCE_PORT, port).apply();
    }

    public void setServerAddress(String ip) {
        this.mServerAddress = ip;
        prefs.edit().putString(Constants.PREFERENCE_IP_ADDRESS, ip).apply();
    }
}