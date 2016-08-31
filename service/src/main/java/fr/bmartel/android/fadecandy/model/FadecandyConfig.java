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
package fr.bmartel.android.fadecandy.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.fadecandy.constant.Constants;

/**
 * @author Bertrand Martel
 */
public class FadecandyConfig {

    private String mHost;
    private int mPort;
    private FadecandyColor mFcColor;
    private boolean mVerbose;
    private List<FadecandyDevice> mFcDevices = new ArrayList<>();

    public FadecandyConfig(String host, int port, FadecandyColor color, boolean verbose, List<FadecandyDevice> devices) {
        mHost = host;
        mPort = port;
        mFcColor = color;
        mVerbose = verbose;
        mFcDevices = devices;
    }

    public FadecandyConfig(JSONObject config) {

        try {

            if (config.has(Constants.CONFIG_LISTEN) &&
                    config.has(Constants.CONFIG_VERBOSE) &&
                    config.has(Constants.CONFIG_COLOR) &&
                    config.has(Constants.CONFIG_DEVICES)) {

                JSONArray listen = config.getJSONArray(Constants.CONFIG_LISTEN);

                if (listen.length() > 1) {
                    mHost = listen.getString(0);
                    mPort = listen.getInt(1);
                }

                mVerbose = config.getBoolean(Constants.CONFIG_VERBOSE);

                mFcColor = new FadecandyColor(config.getJSONObject(Constants.CONFIG_COLOR));

                JSONArray fcDeviceArr = config.getJSONArray(Constants.CONFIG_DEVICES);

                for (int i = 0; i < fcDeviceArr.length(); i++) {
                    mFcDevices.add(new FadecandyDevice((JSONObject) fcDeviceArr.get(i)));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject toJsonObject() {

        JSONObject config = new JSONObject();

        try {
            JSONArray listen = new JSONArray();
            listen.put(mHost);
            listen.put(mPort);

            JSONArray devices = new JSONArray();

            for (FadecandyDevice device : mFcDevices) {
                devices.put(device.toJsonObject());
            }

            config.put(Constants.CONFIG_LISTEN, listen);
            config.put(Constants.CONFIG_VERBOSE, mVerbose);
            config.put(Constants.CONFIG_COLOR, mFcColor.toJsonObject());
            config.put(Constants.CONFIG_DEVICES, devices);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return config;
    }

    public String toJsonString() {
        return toJsonObject().toString();
    }

    public FadecandyColor getFcColor() {
        return mFcColor;
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public List<FadecandyDevice> getFcDevices() {
        return mFcDevices;
    }
}
