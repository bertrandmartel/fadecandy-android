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
package fr.bmartel.fadecandy.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import fr.bmartel.fadecandy.FadecandySingleton
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.activity.BaseActivity
import fr.bmartel.fadecandy.webview.IConfigListener
import fr.bmartel.fadecandy.webview.JsInterface
import java.io.UnsupportedEncodingException

/**
 * Fadecandy configuration Fragment.
 *
 * @author Bertrand Martel
 */
class ConfigFragment : android.support.v4.app.Fragment() {

    private var mSingleton: FadecandySingleton? = null

    /**
     * Javascript interface.
     */
    lateinit var mJavascriptInterface: JsInterface

    lateinit var mWebView: WebView

    lateinit var mHandler: Handler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.config_fragment, container, false)

        mHandler = Handler()

        val tv = view.findViewById<TextView>(R.id.config_link)
        tv.text = Html.fromHtml(resources.getString(R.string.fadecandy_config_link))
        tv.movementMethod = LinkMovementMethod.getInstance()

        val updateBtn = view.findViewById<Button>(R.id.button_update_config)
        val defaultBtn = view.findViewById<Button>(R.id.button_default_config)
        updateBtn.setOnClickListener {
            getConfig(object : IConfigListener {
                override fun onConfigReceived(config: String) {
                    mHandler.post {
                        mSingleton?.updateConfig(config)
                        mSingleton?.restartServer()
                        activity?.onBackPressed()
                    }
                }
            })
        }

        mSingleton = FadecandySingleton.getInstance(activity!!.applicationContext)
        defaultBtn.setOnClickListener { setConfig(mSingleton?.defaultConfig) }
        setupWebView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as BaseActivity).setTitle(resources.getString(R.string.menu_config_fadecandy))
    }

    @SuppressLint("JavascriptInterface")
    private fun setupWebView(view: View) {
        val loadingProgress = view.findViewById<ProgressBar>(R.id.progress_bar)
        mWebView = view.findViewById(R.id.webView)
        mJavascriptInterface = JsInterface(loadingProgress, mHandler)
        mWebView.addJavascriptInterface(mJavascriptInterface, "JSInterface")
        val webSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        try {
            val videoUrl = "file:///android_asset/config.html" +
                    "?config=" + Base64.encodeToString(mSingleton?.config?.toByteArray(charset("UTF-8")), Base64.DEFAULT)
            mWebView.loadUrl(videoUrl)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

    private fun setConfig(config: String?) {
        mWebView.post {
            val stringBuilder = StringBuilder()
            stringBuilder.append("javascript:try{")
            stringBuilder.append("setConfig('$config')")
            stringBuilder.append("}catch(error){console.error(error.message);}")
            mWebView.loadUrl(stringBuilder.toString())
        }
    }

    private fun getConfig(configListener: IConfigListener) {
        mJavascriptInterface.setConfigListener(configListener)
        mWebView.post {
            val stringBuilder = StringBuilder()
            stringBuilder.append("javascript:try{")
            stringBuilder.append("getConfig()")
            stringBuilder.append("}catch(error){console.error(error.message);}")
            mWebView.loadUrl(stringBuilder.toString())
        }
    }
}
