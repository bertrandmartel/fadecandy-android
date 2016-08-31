package fr.bmartel.fadecandy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.fadecandy.FadecandyClient;
import fr.bmartel.android.fadecandy.IFadecandyListener;
import fr.bmartel.android.fadecandy.ServerError;
import fr.bmartel.android.fadecandy.ServiceType;
import fr.bmartel.fadecandy.activity.MainActivity;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;
import fr.bmartel.fadecandy.listener.ISingletonListener;

/**
 * Created by akinaru on 31/08/16.
 */
public class FadecandySingleton {

    private static FadecandySingleton mInstance;

    private ExecutorService mExecutorService;

    private FadecandyClient mFadecandyClient;

    private Context mContext;

    private List<ISingletonListener> mListeners = new ArrayList<>();

    private final static int STRIP_COUNT = 30;

    private final static String TAG = FadecandySingleton.class.getSimpleName();

    public static FadecandySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FadecandySingleton(context);
        }
        return mInstance;
    }

    public void addListener(ISingletonListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(ISingletonListener listener) {
        mListeners.remove(listener);
    }

    public FadecandySingleton(Context context) {

        mExecutorService = Executors.newSingleThreadExecutor();

        mContext = context;

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
        }, new Intent(mContext, MainActivity.class));

        mFadecandyClient.startServer();
    }

    public void clearPixel() {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                checkJoinThread();
                ColorUtils.clear(getIpAddress(), getServerPort(), STRIP_COUNT);
            }
        });
    }

    public void checkJoinThread() {
        Spark.CONTROL = false;
    }

    public void setFullColor(final int color) {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                checkJoinThread();
                ColorUtils.setFullColor(getIpAddress(), getServerPort(), STRIP_COUNT, color);
            }
        });
    }

    public boolean isServerRunning() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.isServerRunning();
        }
        return false;
    }

    public void switchServerStatus() {

        if (mFadecandyClient != null) {

            if (mFadecandyClient.isServerRunning()) {
                mFadecandyClient.closeServer();
            } else {
                mFadecandyClient.startServer();
            }
        }
    }

    public ServiceType getServiceType() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getServiceType();
        }
        return null;
    }

    public void setServiceType(ServiceType serviceType) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServiceType(serviceType);
        }
    }

    public int getServerPort() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getServerPort();
        }
        return 0;
    }

    public String getIpAddress() {
        if (mFadecandyClient != null) {
            return mFadecandyClient.getIpAddress();
        }
        return "";
    }

    public void setServerPort(int port) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServerPort(port);
        }
    }

    public void setServerAddress(String ip) {
        if (mFadecandyClient != null) {
            mFadecandyClient.setServerAddress(ip);
        }
    }

    public void disconnect() {
        if (mFadecandyClient != null) {
            mFadecandyClient.disconnect();
        }
    }

    public void closeServer() {
        if (mFadecandyClient != null) {
            mFadecandyClient.closeServer();
        }
    }

    public void startServer() {
        if (mFadecandyClient != null) {
            mFadecandyClient.startServer();
        }
    }
}
