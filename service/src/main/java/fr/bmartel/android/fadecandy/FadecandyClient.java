package fr.bmartel.android.fadecandy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class FadecandyClient {

    private final static String TAG = FadecandyClient.class.getSimpleName();

    private IFadecandyService fadecandyService;

    private boolean mBound;

    private Context mContext;

    private boolean mShouldStartServer;

    public FadecandyClient(Context context, IFadecandyListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void connect() {
        bindService();
    }

    private IFadecandyListener mListener;

    private static final String SERVICE_NAME = "fr.bmartel.android.fadecandy.service.FadecandyService";

    public void disconnect() {
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private void bindService() {

        Intent intent = new Intent();
        intent.setClassName(mContext, SERVICE_NAME);

        mContext.startService(intent);

        mBound = mContext.bindService(intent, mServiceConnection,
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
            fadecandyService = IFadecandyService.Stub.asInterface(service);

            if (mShouldStartServer) {
                try {
                    if (fadecandyService.startServer() == 0) {
                        if (mListener != null) {
                            mListener.onServerStart();
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onServerError(ServerError.START_SERVER_ERROR);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
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
            try {
                if (fadecandyService.startServer() == 0) {
                    if (mListener != null) {
                        mListener.onServerStart();
                    }
                } else {
                    if (mListener != null) {
                        mListener.onServerError(ServerError.START_SERVER_ERROR);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mShouldStartServer = true;
            Log.e(TAG, "Starting service...");
            bindService();
        }
    }

    public void closeServer() {

        if (mBound) {
            try {
                if (fadecandyService.stopServer() == 0) {
                    if (mListener != null) {
                        mListener.onServerClose();
                    }
                } else {
                    if (mListener != null) {
                        mListener.onServerError(ServerError.CLOSE_SERVER_ERROR);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "service not started.");
        }
    }

    public boolean isServerRunning() {

        if (mBound) {
            try {
                return fadecandyService.isServerRunning();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "service not started");
        }
        return false;
    }
}
