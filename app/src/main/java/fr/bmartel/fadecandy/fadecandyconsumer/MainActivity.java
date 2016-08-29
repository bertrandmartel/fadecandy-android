package fr.bmartel.fadecandy.fadecandyconsumer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.fadecandy.FadecandyClient;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.ledcontrol.ColorUtils;
import fr.bmartel.fadecandy.ledcontrol.Spark;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private boolean bound = false;

    private ExecutorService threadPool = null;

    private final static String host = "127.0.0.1";

    private final static int port = 7890;

    private final static int STRIP_COUNT = 30;

    private Thread workerThread = null;

    private boolean controlLoop = true;

    private int currentSparkColor = 0;

    private FadecandyClient fadecandyClient;

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

        fadecandyClient = new FadecandyClient(getApplicationContext());
        fadecandyClient.startServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (fadecandyClient != null) {
                fadecandyClient.closeServer();
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

                MenuItem item = nvDrawer.getMenu().findItem(R.id.server_status);
                if (item != null) {
                    item.setIcon(R.drawable.ic_check);
                    item.setTitle(getResources().getString(R.string.start));
                }
            } else {
                fadecandyClient.startServer();

                MenuItem item = nvDrawer.getMenu().findItem(R.id.server_status);
                if (item != null) {
                    item.setIcon(R.drawable.ic_cancel);
                    item.setTitle(getResources().getString(R.string.cancel));
                }
            }
        }
    }
}
