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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.inter.IFc;
import fr.bmartel.fadecandy.menu.MenuUtils;

/**
 * Abstract activity for all activities in Bluetooth LE Analyzer
 *
 * @author Bertrand Martel
 */
public abstract class BaseActivity extends AppCompatActivity implements IFc {

    private final static String TAG = BaseActivity.class.getSimpleName();

    /**
     * application toolbar
     */
    protected Toolbar toolbar = null;

    /**
     * navigationdrawer
     */
    protected DrawerLayout mDrawer = null;

    /**
     * toggle on the hamburger button
     */
    protected ActionBarDrawerToggle drawerToggle;

    /**
     * navigation view
     */
    protected NavigationView nvDrawer;

    /**
     * activity layout ressource id
     */
    private int layoutId;

    /**
     * set activity ressource id
     *
     * @param resId
     */
    protected void setLayout(int resId) {
        layoutId = resId;
    }

    protected boolean openingDrawer = false;

    protected FloatingActionButton mFailureButton;

    protected boolean mFailure = false;

    protected boolean mAssociated = false;

    protected DiscreteSeekBar discreteSeekBar;

    protected final static boolean SHOW_SEEKBAR_VALUE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutId);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.fc_title));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.setDrawerListener(drawerToggle);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Setup drawer view
        setupDrawerContent(nvDrawer);
    }

    /**
     * setup navigation view
     *
     * @param navigationView
     */
    private void setupDrawerContent(final NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        MenuUtils.selectDrawerItem(menuItem, mDrawer, BaseActivity.this, BaseActivity.this);

                        return false;
                    }
                });
    }

    /**
     * setup action drawer
     *
     * @return
     */
    protected ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                if (slideOffset == 0) {
                    openingDrawer = false;
                    if (SHOW_SEEKBAR_VALUE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                discreteSeekBar.showFloater(250);
                            }
                        });
                    }
                } else if (slideOffset == 1) {
                    openingDrawer = true;
                    if (SHOW_SEEKBAR_VALUE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                discreteSeekBar.hideFloater(1);
                            }
                        });
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        //cancel button
        /*
        MenuItem item = menu.findItem(R.id.cancel_btn);
        if (item != null) {
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    stopServer();
                    return true;
                }
            });
        }
        */

        return super.onCreateOptionsMenu(menu);
    }
}
