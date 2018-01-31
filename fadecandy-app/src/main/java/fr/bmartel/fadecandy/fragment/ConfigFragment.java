/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016-2018 Bertrand Martel
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
package fr.bmartel.fadecandy.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.activity.BaseActivity;
import fr.bmartel.fadecandy.webview.IConfigListener;
import fr.bmartel.fadecandy.webview.JsInterface;

/**
 * Fadecandy configuration Fragment.
 *
 * @author Bertrand Martel
 */
public class ConfigFragment extends android.support.v4.app.Fragment {

    protected FadecandySingleton mSingleton;

    /**
     * Javascript interface.
     */
    private JsInterface mJavascriptInterface;

    private WebView mWebView;

    private Handler mHandler;

    public ConfigFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config_fragment, container, false);

        mHandler = new Handler();

        TextView tv = view.findViewById(R.id.config_link);
        tv.setText(Html.fromHtml(getResources().getString(R.string.fadecandy_config_link)));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        Button updateBtn = view.findViewById(R.id.button_update_config);
        Button defaultBtn = view.findViewById(R.id.button_default_config);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getConfig(new IConfigListener() {
                    @Override
                    public void onConfigReceived(final String config) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSingleton.updateConfig(config);
                                mSingleton.restartServer();
                                getActivity().onBackPressed();
                            }
                        });
                    }
                });
            }
        });

        mSingleton = FadecandySingleton.getInstance(getActivity().getApplicationContext());

        defaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConfig(mSingleton.getDefaultConfig());
            }
        });
        setupWebView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) getActivity()).setTitle(getResources().getString(R.string.menu_config_fadecandy));
    }

    @SuppressLint("JavascriptInterface")
    private void setupWebView(View view) {
        final ProgressBar loadingProgress = view.findViewById(R.id.progress_bar);
        mWebView = view.findViewById(R.id.webView);
        mJavascriptInterface = new JsInterface(loadingProgress, mHandler);
        mWebView.addJavascriptInterface(mJavascriptInterface, "JSInterface");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        try {
            final String videoUrl = "file:///android_asset/config.html" +
                    "?config=" + Base64.encodeToString(mSingleton.getConfig().getBytes("UTF-8"), Base64.DEFAULT);
            mWebView.loadUrl(videoUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setConfig(final String config) {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("javascript:try{");
                stringBuilder.append("setConfig('" + config + "')");
                stringBuilder.append("}catch(error){console.error(error.message);}");
                mWebView.loadUrl(stringBuilder.toString());
            }
        });
    }

    private void getConfig(IConfigListener configListener) {
        mJavascriptInterface.setConfigListener(configListener);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("javascript:try{");
                stringBuilder.append("getConfig()");
                stringBuilder.append("}catch(error){console.error(error.message);}");
                mWebView.loadUrl(stringBuilder.toString());
            }
        });
    }
}
