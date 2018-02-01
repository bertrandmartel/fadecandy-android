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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.ndk.CrashlyticsNdk
import com.roughike.bottombar.BottomBar
import fr.bmartel.android.fadecandy.model.ServiceType
import fr.bmartel.android.fadecandy.model.UsbItem
import fr.bmartel.fadecandy.BuildConfig
import fr.bmartel.fadecandy.FadecandySingleton
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.constant.AppConstants
import fr.bmartel.fadecandy.dialog.ConfigurationDialog
import fr.bmartel.fadecandy.fragment.*
import fr.bmartel.fadecandy.inter.IFragment
import fr.bmartel.fadecandy.listener.ISingletonListener
import io.fabric.sdk.android.Fabric

/**
 * Fadecandy Main activity.
 *
 * @author Bertrand Martel
 */
class MainActivity : BaseActivity() {

    /**
     * define if this is the first time server has been started after a connect().
     */
    private var mStarted: Boolean = false

    /**
     * the current fragment.
     */
    lateinit var mFragment: Fragment

    private var mDialog: Dialog? = null

    /**
     * one toast for the whole activity. We dont want to have multiple Toast queued.
     */
    private lateinit var mToast: Toast

    override var serviceType: ServiceType
        get() {
            return mSingleton?.serviceType ?: ServiceType.NON_PERSISTENT_SERVICE
        }
        set(value) {
            mSingleton?.serviceType = value
        }

    override var ipAddress: String
        get() {
            return mSingleton?.ipAddress ?: ""
        }
        set(value) {
            mSingleton?.ipAddress = value
        }

    override var context: Context = this
        get() {
            return this
        }

    override var ledCount: Int
        get() {
            return mSingleton?.ledCount ?: 0
        }
        set(value) {
            mSingleton?.ledCount = value
        }

    override var serverPort: Int
        get() {
            return mSingleton?.serverPort ?: 0
        }
        set(value) {
            mSingleton?.serverPort = value
        }

    override var serverMode: Boolean
        get() {
            return mSingleton?.isServerMode ?: false
        }
        set(value) {
            mSingleton?.isServerMode = value
        }

    override var remoteServerIp: String
        get() {
            return mSingleton?.remoteServerIp ?: ""
        }
        set(value) {
            mSingleton?.remoteServerIp = value
        }

    override var remoteServerPort: Int
        get() {
            return mSingleton?.remoteServerPort ?: 0
        }
        set(value) {
            mSingleton?.remoteServerPort = value
        }

    override var sparkSpan: Int
        get() {
            return mSingleton?.sparkSpan ?: AppConstants.DEFAULT_SPARK_SPAN
        }
        set(value) {
            mSingleton?.sparkSpan = value
        }

    /**
     * Singleton listener used to notify server stop/start/error.
     */
    private val mListener = object : ISingletonListener {
        override fun onServerStart() {
            runOnUiThread {
                val item = nvDrawer.menu.findItem(R.id.server_status)
                if (item != null) {
                    item.setIcon(R.drawable.ic_cancel)
                    item.title = resources.getString(R.string.cancel)
                }

                if (mStarted) {
                    showToast(getString(R.string.server_started))
                    setToolbarTitle()
                } else {
                    (mFragment as IFragment).onServerFirstStart()
                }
                mStarted = true
            }
        }

        override fun onServerClose() {
            runOnUiThread {
                val item = nvDrawer.menu.findItem(R.id.server_status)
                if (item != null) {
                    item.setIcon(R.drawable.ic_action_playback_play)
                    item.title = resources.getString(R.string.start)
                }

                if (mStarted) {
                    showToast(getString(R.string.server_closed))
                    setToolbarTitle("disconnected")
                }
            }
        }

        override fun onServerError() {
            runOnUiThread {
                val item = nvDrawer.menu.findItem(R.id.server_status)
                if (item != null) {
                    item.setIcon(R.drawable.ic_action_playback_play)
                    item.title = resources.getString(R.string.start)
                }

                if (mStarted) {
                    showToast(getString(R.string.server_closed))
                    setToolbarTitle("disconnected")
                }
            }
        }

        override fun onUsbDeviceAttached(usbItem: UsbItem?) {
            runOnUiThread { setToolbarTitle() }
        }

        override fun onUsbDeviceDetached(usbItem: UsbItem?) {
            runOnUiThread { setToolbarTitle() }
        }

        override fun onServerConnectionFailure() {
            runOnUiThread {
                showToast(getString(R.string.server_connection) + " ${mSingleton?.remoteServerIp}:${mSingleton?.serverPort} " + getString(R.string.failed))
                setToolbarTitle("disconnected")
                showConfigurationDialog()
            }
        }

        override fun onConnectedDeviceChanged(size: Int) {
            runOnUiThread { setToolbarTitle(size) }
        }

        override fun onServerConnectionSuccess() {
            runOnUiThread { showToast(getString(R.string.server_connected) + " ${mSingleton?.remoteServerIp}:${mSingleton?.serverPort}") }
        }

        override fun onServerConnectionClosed() {
            runOnUiThread {
                showToast(getString(R.string.server_connection) + " ${mSingleton?.remoteServerIp}:${mSingleton?.serverPort} " + getString(R.string.closed))
                setToolbarTitle(text = "disconnected")
                showConfigurationDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mSingleton = FadecandySingleton.getInstance(context = applicationContext)
        mSingleton?.addListener(listener = mListener)
        mSingleton?.connect()

        setLayout(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, Crashlytics(), CrashlyticsNdk())
        }

        mFragment = FullColorFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, mFragment).commit()
        val mBottomBar = findViewById<BottomBar>(R.id.bottomBar)

        mBottomBar.setOnTabSelectListener { tabId ->
            clearBackStack()
            setToolbarTitle()
            when (tabId) {
                R.id.tab_fullcolor -> {
                    mFragment = FullColorFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, mFragment).commit()
                }
                R.id.tab_spark -> {
                    mFragment = SparkFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, mFragment).commit()
                }
                R.id.tab_temperature -> {
                    mFragment = TemperatureFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, mFragment).commit()
                }
                R.id.tab_mixer -> {
                    mFragment = MixerFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, mFragment).commit()
                }
                R.id.tab_pulse -> {
                    mFragment = PulseFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_frame, mFragment).commit()
                }
            }
        }
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
    }

    private fun showToast(text: String) {
        mToast.setText(text)
        mToast.show()
    }

    private fun showConfigurationDialog() {
        mDialog?.let { it.dismiss() }
        val dialog = ConfigurationDialog(this)
        setCurrentDialog(dialog)
        dialog.show()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mDialog?.let { it.dismiss() }
        mSingleton?.removeListener(listener = mListener)
        mStarted = false
        mSingleton?.let { it.disconnect() }
    }

    override fun switchServerStatus() {
        mSingleton?.let {
            if (mSingleton?.isServerRunning == true) {
                mSingleton?.closeServer()
            } else {
                mSingleton?.startServer()
            }
        }
    }

    override fun restartServer() {
        mSingleton?.restartServer()
    }

    override fun setCurrentDialog(dialog: Dialog) {
        mDialog = dialog
    }

    override fun restartRemoteConnection() {
        mSingleton?.restartRemoteConnection()
    }
}