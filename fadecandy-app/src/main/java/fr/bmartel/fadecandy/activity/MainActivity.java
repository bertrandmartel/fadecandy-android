/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Bertrand Martel
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import fr.bmartel.android.fadecandy.client.FadecandyClient;
import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.fadecandy.BuildConfig;
import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.constant.AppConstants;
import fr.bmartel.fadecandy.dialog.ConfigurationDialog;
import fr.bmartel.fadecandy.fragment.FullColorFragment;
import fr.bmartel.fadecandy.fragment.MixerFragment;
import fr.bmartel.fadecandy.fragment.PulseFragment;
import fr.bmartel.fadecandy.fragment.SparkFragment;
import fr.bmartel.fadecandy.fragment.TemperatureFragment;
import fr.bmartel.fadecandy.inter.IFragment;
import fr.bmartel.fadecandy.listener.ISingletonListener;
import io.fabric.sdk.android.Fabric;

/**
 * Fadecandy Main activity.
 *
 * @author Bertrand Martel
 */
public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    /**
     * define if this is the first time server has been started after a connect().
     */
    private boolean mStarted;

    /**
     * the current fragment.
     */
    private Fragment mFragment;

    /**
     * one dialog to show above the activity. We dont want to have multiple Dialog above each other.
     */
    private Dialog mDialog;

    /**
     * one toast for the whole activity. We dont want to have multiple Toast queued.
     */
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mSingleton = FadecandySingleton.getInstance(getApplicationContext());

        mSingleton.addListener(mListener);

        mSingleton.connect();

        setLayout(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        }

        mFragment = new FullColorFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();

        BottomBar mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_fullcolor:
                        mFragment = new FullColorFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();
                        break;
                    case R.id.tab_spark:
                        mFragment = new SparkFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();
                        break;
                    case R.id.tab_temperature:
                        mFragment = new TemperatureFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();
                        break;
                    case R.id.tab_mixer:
                        mFragment = new MixerFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();
                        break;
                    case R.id.tab_pulse:
                        mFragment = new PulseFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();
                        break;
                }
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_fullcolor:
                        break;
                    case R.id.tab_spark:
                        break;
                    case R.id.tab_temperature:
                        break;
                }
            }
        });

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    private void showToast(String text) {
        mToast.setText(text);
        mToast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Singleton listener used to notify server stop/start/error.
     */
    private ISingletonListener mListener = new ISingletonListener() {
        @Override
        public void onServerStart() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    MenuItem item = nvDrawer.getMenu().findItem(R.id.server_status);
                    if (item != null) {
                        item.setIcon(R.drawable.ic_cancel);
                        item.setTitle(getResources().getString(R.string.cancel));
                    }

                    if (mStarted) {
                        showToast(getString(R.string.server_started));
                        setToolbarTitle();
                    } else {
                        ((IFragment) mFragment).onServerFirstStart();
                    }
                    mStarted = true;
                }
            });
        }

        @Override
        public void onServerClose() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MenuItem item = nvDrawer.getMenu().findItem(R.id.server_status);
                    if (item != null) {
                        item.setIcon(R.drawable.ic_action_playback_play);
                        item.setTitle(getResources().getString(R.string.start));
                    }

                    if (mStarted) {
                        showToast(getString(R.string.server_closed));
                        setToolbarTitle("disconnected");
                    }
                }
            });
        }

        @Override
        public void onUsbDeviceAttached(UsbItem usbItem) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setToolbarTitle();
                }
            });
        }

        @Override
        public void onUsbDeviceDetached(UsbItem usbItem) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setToolbarTitle();
                }
            });
        }

        @Override
        public void onServerConnectionFailure() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.server_connection) + " " + mSingleton.getRemoteServerIp() + ":" + mSingleton.getServerPort() + " " + getString(R.string.failed));
                    setToolbarTitle("disconnected");
                    showConfigurationDialog();
                }
            });
        }

        @Override
        public void onConnectedDeviceChanged(final int size) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setToolbarTitle(size);
                }
            });
        }

        @Override
        public void onServerConnectionSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.server_connected) + " " + mSingleton.getRemoteServerIp() + ":" + mSingleton.getServerPort());
                }
            });
        }

        @Override
        public void onServerConnectionClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.server_connection) + " " + mSingleton.getRemoteServerIp() + ":" + mSingleton.getServerPort() + " " + getString(R.string.closed));
                    setToolbarTitle("disconnected");
                    showConfigurationDialog();
                }
            });
        }
    };

    private void showConfigurationDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        ConfigurationDialog dialog = new ConfigurationDialog(this);
        setCurrentDialog(dialog);
        dialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        if (mDialog != null) {
            mDialog.dismiss();
        }

        mSingleton.removeListener(mListener);

        mStarted = false;
        try {
            if (mSingleton != null) {
                mSingleton.disconnect();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchServerStatus() {

        if (mSingleton != null) {

            if (mSingleton.isServerRunning()) {
                mSingleton.closeServer();
            } else {
                mSingleton.startServer();
            }
        }
    }

    @Override
    public ServiceType getServiceType() {
        if (mSingleton != null) {
            return mSingleton.getServiceType();
        }
        return null;
    }

    @Override
    public void setServiceType(ServiceType serviceType) {
        if (mSingleton != null) {
            mSingleton.setServiceType(serviceType);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public int getServerPort() {
        if (mSingleton != null) {
            return mSingleton.getServerPort();
        }
        return FadecandyClient.DEFAULT_PORT;
    }

    @Override
    public String getIpAddress() {
        if (mSingleton != null) {
            return mSingleton.getIpAddress();
        }
        return "";
    }

    @Override
    public void setServerPort(int port) {
        if (mSingleton != null) {
            mSingleton.setServerPort(port);
        }
    }

    @Override
    public void setServerAddress(String ip) {
        if (mSingleton != null) {
            mSingleton.setServerAddress(ip);
        }
    }

    @Override
    public int getLedCount() {
        if (mSingleton != null) {
            return mSingleton.getLedCount();
        }
        return 0;
    }

    @Override
    public void setLedCount(int ledCount) {
        if (mSingleton != null) {
            mSingleton.setLedCount(ledCount);
        }
    }

    @Override
    public void restartServer() {
        if (mSingleton != null) {
            mSingleton.restartServer();
        }
    }

    @Override
    public boolean isServerMode() {
        if (mSingleton != null) {
            return mSingleton.isServerMode();
        }
        return false;
    }

    @Override
    public void setServerMode(boolean androidServerMode) {
        if (mSingleton != null) {
            mSingleton.setServerMode(androidServerMode);
            diplayHideStartStopServer();
        }
    }

    @Override
    public void setCurrentDialog(Dialog dialog) {
        mDialog = dialog;
    }

    @Override
    public void restartRemoteConnection() {
        if (mSingleton != null) {
            mSingleton.restartRemoteConnection();
        }
    }

    @Override
    public void setRemoteServerIp(String ip) {
        if (mSingleton != null) {
            mSingleton.setRemoteServerIp(ip);
        }
    }

    @Override
    public void setRemoteServerPort(int port) {
        if (mSingleton != null) {
            mSingleton.setRemoteServerPort(port);
        }
    }

    public String getRemoteServerIp() {
        if (mSingleton != null) {
            return mSingleton.getRemoteServerIp();
        }
        return "";
    }

    public int getRemoteServerPort() {
        if (mSingleton != null) {
            return mSingleton.getRemoteServerPort();
        }
        return 0;
    }

    @Override
    public int getSparkSpan() {
        if (mSingleton != null) {
            return mSingleton.getSparkSpan();
        }
        return AppConstants.DEFAULT_SPARK_SPAN;
    }

    @Override
    public void setSparkSpan(int sparkSpan) {
        if (mSingleton != null) {
            mSingleton.setSparkSpan(sparkSpan);
        }
    }
}
