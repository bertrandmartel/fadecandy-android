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
package fr.bmartel.android.fadecandy.client;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.inter.IFcServerEventListener;
import fr.bmartel.android.fadecandy.inter.IUsbEventListener;
import fr.bmartel.android.fadecandy.model.ServerError;
import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.android.fadecandy.service.FadecandyService;
import fr.bmartel.android.fadecandy.service.FadecandyServiceBinder;

/**
 * Fadecandy client wrapper to be used to bind service & call service API.
 *
 * @author Bertrand Martel
 */
public class FadecandyClient {

    private final static String TAG = FadecandyClient.class.getSimpleName();

    /**
     * Fadecandy service instance.
     */
    private FadecandyService fadecandyService;

    /**
     * define if fadecandy service is bound.
     */
    private boolean mBound;

    /**
     * android context.
     */
    private Context mContext;

    /**
     * define if server should be started.
     */
    private boolean mShouldStartServer;

    /**
     * activity intent name that will be opened when user click on service notification.
     */
    private String mActivity;

    /**
     * intent used to launch fadecandy service.
     */
    private Intent fadecandyServiceIntent;

    /**
     * default port if service is down.
     */
    public final static int DEFAULT_PORT = 7890;

    /**
     * Service type to override if connect was called with ServiceType argument.
     */
    private ServiceType mOverrideServiceType;

    /**
     * define if service type should be override when calling startService.
     */
    private boolean mShouldOverrideServiceType;

    /**
     * Build Fadecandy client
     *
     * @param context        Android context.
     * @param listener       fadecandy listener.
     * @param usbListener    usb event listener
     * @param activityIntent activity intent name
     */
    public FadecandyClient(Context context, IFcServerEventListener listener, IUsbEventListener usbListener, String activityIntent) {
        mContext = context;
        mListener = listener;
        mUsbListener = usbListener;
        mActivity = activityIntent;
    }

    /**
     * connect with override of service type
     *
     * @param serviceType
     */
    public void connect(ServiceType serviceType) {
        mShouldOverrideServiceType = true;
        mOverrideServiceType = serviceType;
        bindService();
        registerReceiver();
    }

    /**
     * bind service and register receiver.
     */
    public void connect() {
        bindService();
        registerReceiver();
    }

    /**
     * broadcast receiver used to catch user click on service notification.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(FadecandyService.ACTION_EXIT)) {
                Log.v(TAG, "Exit event received : disconnecting");
                disconnect();
            }
        }
    };

    /**
     * register receiver.
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FadecandyService.ACTION_EXIT);
        mContext.registerReceiver(receiver, filter);
    }

    /**
     * Fadecandy server event listener.
     */
    private IFcServerEventListener mListener;

    /**
     * Fadencandy USB event listener.
     */
    private IUsbEventListener mUsbListener;

    /**
     * service intent name.
     */
    public static final String SERVICE_NAME = "fr.bmartel.android.fadecandy.service.FadecandyService";

    /**
     * disconnect from service (that will not close server if service is not destroyed eg service not in persistent mode).
     */
    public void disconnect() {
        if (mBound) {
            Log.v(TAG, "unbind");
            mContext.unbindService(mServiceConnection);
            mContext.unregisterReceiver(receiver);
            mBound = false;
            if (mListener != null) {
                mListener.onServerClose();
            }
        }
    }

    /**
     * force service to stop.
     */
    public void stopService() {
        mContext.stopService(fadecandyServiceIntent);
    }

    /**
     * bind to Fadecandy service.
     */
    private void bindService() {

        fadecandyServiceIntent = new Intent();
        fadecandyServiceIntent.setClassName(mContext, SERVICE_NAME);
        fadecandyServiceIntent.putExtra(Constants.SERVICE_EXTRA_ACTIVITY, mActivity);

        if (mShouldOverrideServiceType) {
            fadecandyServiceIntent.putExtra(Constants.SERVICE_EXTRA_SERVICE_TYPE, mOverrideServiceType.ordinal());
        }

        mContext.startService(fadecandyServiceIntent);

        mBound = mContext.bindService(fadecandyServiceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);

        if (mBound) {
            Log.v(TAG, "service started");
        } else {
            Log.e(TAG, "service not started");
        }
    }

    /**
     * Fadecandy service connection.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.v(TAG, "fadecandy service connected");

            fadecandyService = ((FadecandyServiceBinder) service).getService();

            if (mUsbListener != null) {
                fadecandyService.addUsbListener(mUsbListener);
            }

            if (mShouldStartServer) {
                if (fadecandyService.startServer() == 0) {
                    if (mListener != null) {
                        mListener.onServerStart();
                    }
                } else {
                    if (mListener != null) {
                        mListener.onServerError(ServerError.START_SERVER_ERROR);
                    }
                }
            }
            mShouldStartServer = false;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "fadecandy disconected");
        }
    };

    /**
     * start Fadecandy server. If service is not started/bounded, server will be started as soon as service is started & bounded.
     */
    public void startServer() {

        if (mBound && fadecandyService != null) {

            if (fadecandyService.startServer() == 0) {
                if (mListener != null) {
                    mListener.onServerStart();
                }
            } else {
                if (mListener != null) {
                    mListener.onServerError(ServerError.START_SERVER_ERROR);
                }
            }
        } else {
            mShouldStartServer = true;
            Log.e(TAG, "Starting service...");
            connect();
        }
    }

    /**
     * close Fadecandy server.
     */
    public void closeServer() {

        if (mBound && fadecandyService != null) {

            if (fadecandyService.stopServer() == 0) {
                if (mListener != null) {
                    mListener.onServerClose();
                }
            } else {
                if (mListener != null) {
                    mListener.onServerError(ServerError.CLOSE_SERVER_ERROR);
                }
            }
        } else {
            Log.e(TAG, "service not started.");
        }
    }

    /**
     * get server status.
     *
     * @return true if server is running, false if server is stopped
     */
    public boolean isServerRunning() {

        if (mBound && fadecandyService != null) {
            return fadecandyService.isServerRunning();
        } else {
            Log.e(TAG, "service not started");
        }
        return false;
    }

    /**
     * get service type (persistent or non persistent)
     *
     * @return
     */
    public ServiceType getServiceType() {

        if (mBound && fadecandyService != null) {
            return fadecandyService.getServiceType();
        }
        return null;
    }

    /**
     * set service type (persistent or non persistent)
     *
     * @param serviceType
     */
    public void setServiceType(ServiceType serviceType) {

        if (mBound && fadecandyService != null) {
            fadecandyService.setServiceType(serviceType);
        }
    }

    /**
     * get server port value
     *
     * @return
     */
    public int getServerPort() {

        if (mBound && fadecandyService != null) {
            return fadecandyService.getServerPort();
        }
        return DEFAULT_PORT;
    }

    /**
     * get server ip/hostname value
     *
     * @return
     */
    public String getIpAddress() {

        if (mBound && fadecandyService != null) {
            return fadecandyService.getIpAddress();
        }
        return "";
    }

    /**
     * set server port value.
     *
     * @param port
     */
    public void setServerPort(int port) {

        if (mBound && fadecandyService != null) {
            fadecandyService.setServerPort(port);
        }
    }

    /**
     * set server IP/hostname value
     *
     * @param ip
     */
    public void setServerAddress(String ip) {

        if (mBound && fadecandyService != null) {
            fadecandyService.setServerAddress(ip);
        }
    }

    /**
     * Set fadecandy configuration.
     *
     * @param config
     */
    public void setConfig(String config) {
        fadecandyService.setConfig(config);
    }

    /**
     * get Fadecandy local configuration.
     *
     * @return
     */
    public String getConfig() {

        if (mBound && fadecandyService != null) {
            return fadecandyService.getConfig();
        }
        return null;
    }

    /**
     * get list of Fadecandy USB device connected.
     *
     * @return
     */
    public HashMap<Integer, UsbItem> getUsbDeviceMap() {

        if (mBound && fadecandyService != null) {
            return fadecandyService.getUsbDeviceMap();
        }
        return new HashMap<>();
    }

    public String getDefaultConfig() {
        return fadecandyService.getDefaultConfig(fadecandyService.getIpAddress(), fadecandyService.getServerPort());
    }
}
