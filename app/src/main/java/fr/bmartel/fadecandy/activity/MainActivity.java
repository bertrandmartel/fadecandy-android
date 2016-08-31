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
package fr.bmartel.fadecandy.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import fr.bmartel.android.fadecandy.ServiceType;
import fr.bmartel.android.fadecandy.model.UsbItem;
import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.fragment.FullColorFragment;
import fr.bmartel.fadecandy.fragment.SparkFragment;
import fr.bmartel.fadecandy.fragment.TemperatureFragment;
import fr.bmartel.fadecandy.inter.IFragment;
import fr.bmartel.fadecandy.listener.ISingletonListener;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private boolean mStarted;

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mSingleton = FadecandySingleton.getInstance(getApplicationContext());

        mSingleton.addListener(mListener);

        mSingleton.connect();

        setLayout(R.layout.activity_main);
        super.onCreate(savedInstanceState);

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

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

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
                        Toast.makeText(MainActivity.this, "server started", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "server closed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onUsbDeviceAttached(UsbItem usbItem) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String deviceTxt = "device";
                    if (mSingleton.getUsbDevices().size() > 1) {
                        deviceTxt = "devices";
                    }
                    getSupportActionBar().setTitle(getResources().getString(R.string.app_title) + " (" + mSingleton.getUsbDevices().size() + " " + deviceTxt + ")");
                }
            });
        }

        @Override
        public void onUsbDeviceDetached(UsbItem usbItem) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String deviceTxt = "device";
                    if (mSingleton.getUsbDevices().size() > 1) {
                        deviceTxt = "devices";
                    }
                    getSupportActionBar().setTitle(getResources().getString(R.string.app_title) + " (" + mSingleton.getUsbDevices().size() + " " + deviceTxt + ")");
                }
            });
        }
    };

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
        return 0;
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

}
