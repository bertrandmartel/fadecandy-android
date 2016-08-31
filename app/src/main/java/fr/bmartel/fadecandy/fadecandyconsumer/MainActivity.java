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
package fr.bmartel.fadecandy.fadecandyconsumer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.fadecandy.FadecandyClient;
import fr.bmartel.android.fadecandy.IFadecandyListener;
import fr.bmartel.android.fadecandy.ServerError;
import fr.bmartel.android.fadecandy.ServiceType;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;

public class MainActivity extends BaseActivity implements ColorPicker.OnColorChangedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private ExecutorService threadPool = null;

    private final static String host = "127.0.0.1";

    private final static int port = 7890;

    private final static int STRIP_COUNT = 30;

    private Thread workerThread = null;

    private boolean controlLoop = true;

    private int currentSparkColor = 0;

    private FadecandyClient fadecandyClient;

    private boolean mStarted;

    private ExecutorService executorService;

    private BottomBar mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setLayout(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        executorService = Executors.newSingleThreadExecutor();

        //init color picker
        ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        picker.setOnColorChangedListener(this);
        picker.setShowOldCenterColor(false);

        discreteSeekBar = (DiscreteSeekBar) findViewById(R.id.discrete1);

        discreteSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });

        discreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        Button button_all_off = (Button) findViewById(R.id.button_all_off);

        button_all_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        checkJoinThread();
                        ColorUtils.clear(host, port, STRIP_COUNT);
                    }
                });
            }
        });

        threadPool = Executors.newFixedThreadPool(1);

        fadecandyClient = new FadecandyClient(getApplicationContext(), new IFadecandyListener() {

            @Override
            public void onServerStart() {

                Log.v(TAG, "onServerStart");

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        MenuItem item = nvDrawer.getMenu().findItem(R.id.server_status);
                        if (item != null) {
                            item.setIcon(R.drawable.ic_cancel);
                            item.setTitle(getResources().getString(R.string.cancel));
                        }

                        item = toolbar.getMenu().findItem(R.id.disconnect_button);
                        item.setIcon(R.drawable.ic_cancel_white);

                        if (mStarted) {
                            Toast.makeText(MainActivity.this, "server started", Toast.LENGTH_SHORT).show();
                        }
                        mStarted = true;
                    }
                });
            }

            @Override
            public void onServerClose() {

                Log.v(TAG, "onServerClose");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuItem item = nvDrawer.getMenu().findItem(R.id.server_status);
                        if (item != null) {
                            item.setIcon(R.drawable.ic_action_playback_play);
                            item.setTitle(getResources().getString(R.string.start));
                        }

                        item = toolbar.getMenu().findItem(R.id.disconnect_button);
                        item.setIcon(R.drawable.ic_action_playback_play_white);

                        if (mStarted) {
                            Toast.makeText(MainActivity.this, "server closed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onServerError(ServerError error) {

            }
        }, new Intent(getApplicationContext(), MainActivity.class));
        fadecandyClient.startServer();

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
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

        //discreteSeekBar.setProgress(100);
        //discreteSeekBar.showFloater(250);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (fadecandyClient != null && fadecandyClient.isServerRunning()) {
            menu.findItem(R.id.disconnect_button).setIcon(R.drawable.ic_cancel_white);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.disconnect_button:
                switchServerStatus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        mStarted = false;
        try {
            if (fadecandyClient != null) {
                fadecandyClient.disconnect();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void checkJoinThread() {
        if (workerThread != null) {
            Spark.CONTROL = false;
            controlLoop = false;
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void switchServerStatus() {

        if (fadecandyClient != null) {

            if (fadecandyClient.isServerRunning()) {
                fadecandyClient.closeServer();
            } else {
                fadecandyClient.startServer();
            }
        }
    }

    @Override
    public ServiceType getServiceType() {
        if (fadecandyClient != null) {
            return fadecandyClient.getServiceType();
        }
        return null;
    }

    @Override
    public void setServiceType(ServiceType serviceType) {
        if (fadecandyClient != null) {
            fadecandyClient.setServiceType(serviceType);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public int getServerPort() {
        if (fadecandyClient != null) {
            return fadecandyClient.getServerPort();
        }
        return 0;
    }

    @Override
    public String getIpAddress() {
        if (fadecandyClient != null) {
            return fadecandyClient.getIpAddress();
        }
        return "";
    }

    @Override
    public void setServerPort(int port) {
        if (fadecandyClient != null) {
            fadecandyClient.setServerPort(port);
        }
    }

    @Override
    public void setServerAddress(String ip) {
        if (fadecandyClient != null) {
            fadecandyClient.setServerAddress(ip);
        }
    }

    @Override
    public void onColorChanged(final int color) {

        Log.i(TAG, "color changed : " + Color.red(color) + " - " + Color.green(color) + " - " + Color.blue(color));

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                checkJoinThread();
                ColorUtils.setFullColor(getIpAddress(), getServerPort(), STRIP_COUNT, color);
            }
        });
    }
}
