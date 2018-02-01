/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2016-2018 Bertrand Martel
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.activity

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import fr.bmartel.fadecandy.FadecandySingleton
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.fragment.ConfigFragment
import fr.bmartel.fadecandy.inter.IFc
import fr.bmartel.fadecandy.menu.MenuUtils

/**
 * Abstract activity for all activities in Fadencandy app.
 *
 * @author Bertrand Martel
 */
abstract class BaseActivity : AppCompatActivity(), IFc {

    /**
     * application toolbar
     */
    lateinit var toolbar: Toolbar

    /**
     * navigationdrawer
     */
    lateinit var mDrawer: DrawerLayout

    /**
     * toggle on the hamburger button
     */
    lateinit var drawerToggle: ActionBarDrawerToggle

    /**
     * navigation view
     */
    lateinit var nvDrawer: NavigationView

    /**
     * activity layout ressource id
     */
    private var layoutId: Int = 0

    /**
     * Fadecandy singleton.
     */
    protected var mSingleton: FadecandySingleton? = null

    private var mFragment: Fragment? = null

    /**
     * set activity ressource id
     *
     * @param resId
     */
    protected fun setLayout(resId: Int) {
        layoutId = resId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)

        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar_item)
        setSupportActionBar(toolbar)

        setToolbarTitle()
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.inflateMenu(R.menu.toolbar_menu)

        // Find our drawer view
        mDrawer = findViewById(R.id.drawer_layout)
        drawerToggle = setupDrawerToggle()
        mDrawer.setDrawerListener(drawerToggle)
        nvDrawer = findViewById(R.id.nvView)

        diplayHideStartStopServer()

        // Setup drawer view
        setupDrawerContent(nvDrawer)
    }

    override fun clearBackStack() {
        val fm = supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
    }

    protected fun diplayHideStartStopServer() {
        runOnUiThread {
            nvDrawer.menu.findItem(R.id.server_status).isVisible = mSingleton?.isServerMode != false
        }
    }

    protected fun setToolbarTitle(size: Int) {
        var deviceTxt = "device"
        if ((mSingleton?.usbDevices?.size ?: 0) > 1) {
            deviceTxt = "devices"
        }
        supportActionBar?.title = resources.getString(R.string.app_title) + " ($size $deviceTxt)"
    }

    protected fun setToolbarTitle(text: String) {
        supportActionBar?.title = resources.getString(R.string.app_title) + " ($text)"
    }

    fun setTitle(text: String) {
        supportActionBar?.title = text
    }

    /**
     * Set toolbar title in initialization or when USB device event occurs
     */
    fun setToolbarTitle() {
        setToolbarTitle(mSingleton?.usbDevices?.size ?: 0)
    }

    override fun setReplaceableFragment(fragment: Fragment) {
        mFragment = fragment
    }

    /**
     * setup navigation view
     *
     * @param navigationView
     */
    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            MenuUtils.selectDrawerItem(menuItem, mDrawer, this@BaseActivity, this@BaseActivity)

            false
        }
    }

    /**
     * setup action drawer
     *
     * @return
     */
    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawer.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Make sure this is the method with just `Bundle` as the signature
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onBackPressed() {
        if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.toolbar_menu, menu)

        val configButton = toolbar.menu.findItem(R.id.button_config)

        configButton.setOnMenuItemClickListener {
            clearBackStack()
            val fragment = ConfigFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment_frame, fragment, "Config")
            ft.addToBackStack(null)
            ft.commit()
            setReplaceableFragment(fragment)
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun getConfig(): String? {
        return mSingleton?.config
    }

    override fun getSFragmentManager(): android.support.v4.app.FragmentManager {
        return supportFragmentManager
    }
}
