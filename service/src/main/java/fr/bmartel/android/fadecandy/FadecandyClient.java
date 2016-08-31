package fr.bmartel.android.fadecandy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.android.fadecandy.service.FadecandyService;

public class FadecandyClient {

    private final static String TAG = FadecandyClient.class.getSimpleName();

    private FadecandyService fadecandyService;

    private boolean mBound;

    private Context mContext;

    private boolean mShouldStartServer;

    private String mActivity;

    private Intent fadecandyServiceIntent;

    public FadecandyClient(Context context, IFadecandyListener listener, String activityName) {
        mContext = context;
        mListener = listener;
        mActivity = activityName;
    }

    public void connect() {
        bindService();
        registerReceiver();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(FadecandyService.ACTION_EXIT)) {
                Log.i(TAG, "Exit event received : disconnecting");
                disconnect();
            }
        }
    };

    private void registerReceiver() {
        Log.i(TAG, "register receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FadecandyService.ACTION_EXIT);
        mContext.registerReceiver(receiver, filter);
    }

    private IFadecandyListener mListener;

    public static final String SERVICE_NAME = "fr.bmartel.android.fadecandy.service.FadecandyService";

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

    private void bindService() {

        fadecandyServiceIntent = new Intent();
        fadecandyServiceIntent.setClassName(mContext, SERVICE_NAME);
        fadecandyServiceIntent.putExtra(Constants.SERVICE_EXTRA_ACTIVITY, mActivity);
        mContext.startService(fadecandyServiceIntent);

        mBound = mContext.bindService(fadecandyServiceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);

        if (mBound) {
            Log.v(TAG, "service started");
        } else {
            Log.e(TAG, "service not started");
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "fadecandy service connected");
            fadecandyService = ((FadecandyServiceBinder) service).getService();

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

    public void startServer() {

        if (mBound) {
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

    public void closeServer() {

        if (mBound) {
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

    public boolean isServerRunning() {

        if (mBound) {
            return fadecandyService.isServerRunning();
        } else {
            Log.e(TAG, "service not started");
        }
        return false;
    }

    public ServiceType getServiceType() {
        if (mBound && fadecandyService != null) {
            return fadecandyService.getServiceType();
        }
        return null;
    }

    public void setServiceType(ServiceType serviceType) {
        Log.i(TAG, "serviceType : " + serviceType);
        if (mBound && fadecandyService != null) {
            fadecandyService.setServiceType(serviceType);
        }
    }

    public int getServerPort() {
        if (mBound && fadecandyService != null) {
            return fadecandyService.getServerPort();
        }
        return 0;
    }

    public String getIpAddress() {
        if (mBound && fadecandyService != null) {
            return fadecandyService.getIpAddress();
        }
        return "";
    }

    public void setServerPort(int port) {
        if (mBound && fadecandyService != null) {
            fadecandyService.setServerPort(port);
        }
    }

    public void setServerAddress(String ip) {
        if (mBound && fadecandyService != null) {
            fadecandyService.setServerAddress(ip);
        }
    }

    public FadecandyConfig getConfig() {
        if (mBound && fadecandyService != null) {
            return fadecandyService.getConfig();
        }
        return null;
    }
}
