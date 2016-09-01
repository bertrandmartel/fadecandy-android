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
 * Fadecandy color object.
 *
 * @author Bertrand Martel
 */
public class FadecandyColor {

    private List<Float> mWhitepoints = new ArrayList<>();
    private float mGamma;

    public FadecandyColor(List<Float> whitepoints, float gamma) {
        mWhitepoints = whitepoints;
        mGamma = gamma;
    }

    public FadecandyColor(JSONObject color) {

        try {
            if (color.has(Constants.CONFIG_GAMMA) &&
                    color.has(Constants.CONFIG_WHITEPOINT)) {
                mGamma = (float) color.getDouble(Constants.CONFIG_GAMMA);

                JSONArray whitepoints = color.getJSONArray(Constants.CONFIG_WHITEPOINT);

                for (int i = 0; i < whitepoints.length(); i++) {
                    mWhitepoints.add((float) whitepoints.getDouble(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJsonObject() {

        JSONObject color = new JSONObject();

        try {

            JSONArray whitepoints = new JSONArray();
            for (Float item : mWhitepoints) {
                whitepoints.put(item);
            }

            color.put(Constants.CONFIG_GAMMA, mGamma);
            color.put(Constants.CONFIG_WHITEPOINT, whitepoints);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return color;
    }

    public String toString() {
        return toJsonObject().toString();
    }

    public List<Float> getWhitepoints() {
        return mWhitepoints;
    }

    public float getGamma() {
        return mGamma;
    }
}
