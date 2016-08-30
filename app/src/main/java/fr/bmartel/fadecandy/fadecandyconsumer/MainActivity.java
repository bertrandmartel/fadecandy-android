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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.fadecandy.FadecandyClient;
import fr.bmartel.android.fadecandy.IFadecandyListener;
import fr.bmartel.android.fadecandy.ServerError;
import fr.bmartel.android.fadecandy.ServiceType;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;

public class MainActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setLayout(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        Button full_color_red = (Button) findViewById(R.id.full_color_red);
        Button full_color_orange = (Button) findViewById(R.id.full_color_orange);
        Button full_color_blue = (Button) findViewById(R.id.full_color_blue);
        Button full_color_greeen = (Button) findViewById(R.id.full_color_green);

        Button spark_color_red = (Button) findViewById(R.id.spark_color_red);
        Button spark_color_orange = (Button) findViewById(R.id.spark_color_orange);
        Button spark_color_blue = (Button) findViewById(R.id.spark_color_blue);
        Button spark_color_greeen = (Button) findViewById(R.id.spark_color_green);

        full_color_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        checkJoinThread();
                        ColorUtils.setFullColor(host, port, STRIP_COUNT, 0xFF0000);
                    }
                });
            }
        });
        full_color_orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        checkJoinThread();
                        ColorUtils.setFullColor(host, port, STRIP_COUNT, 0xFFA500);
                    }
                });
            }
        });
        full_color_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        checkJoinThread();
                        ColorUtils.setFullColor(host, port, STRIP_COUNT, 0x0000FF);
                    }
                });
            }
        });
        full_color_greeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        checkJoinThread();
                        ColorUtils.setFullColor(host, port, STRIP_COUNT, 0x00FF00);
                    }
                });
            }
        });

        spark_color_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkJoinThread();
                Spark.CONTROL = true;
                controlLoop = true;
                currentSparkColor = 0xFF0000;
                workerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (controlLoop) {
                            Spark.draw(host, port, STRIP_COUNT, 0xFF0000);
                        }
                    }
                });
                workerThread.start();
            }
        });

        spark_color_orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkJoinThread();
                Spark.CONTROL = true;
                controlLoop = true;
                currentSparkColor = 0xFFA500;
                workerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (controlLoop) {
                            Spark.draw(host, port, STRIP_COUNT, 0xFFA500);
                        }
                    }
                });
                workerThread.start();
            }
        });

        spark_color_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkJoinThread();
                Spark.CONTROL = true;
                controlLoop = true;
                currentSparkColor = 0x0000FF;
                workerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (controlLoop) {
                            Spark.draw(host, port, STRIP_COUNT, 0x0000FF);
                        }
                    }
                });
                workerThread.start();
            }
        });

        spark_color_greeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkJoinThread();
                Spark.CONTROL = true;
                controlLoop = true;
                currentSparkColor = 0x00FF00;
                workerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (controlLoop) {
                            Spark.draw(host, port, STRIP_COUNT, 0x00FF00);
                        }
                    }
                });
                workerThread.start();
            }
        });

        SeekBar speedSeekBar = (SeekBar) findViewById(R.id.seekbar_speed);

        speedSeekBar.setProgress(0);
        speedSeekBar.incrementProgressBy(10);
        speedSeekBar.setMax(100);

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 10;
                progress = progress * 10;

                Spark.setSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
    public int getDefaultPort() {
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
}
