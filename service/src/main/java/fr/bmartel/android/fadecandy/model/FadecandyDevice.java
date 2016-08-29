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
public class FadecandyDevice {

    private List<List<Integer>> mMap = new ArrayList<>();
    private String mType;

    public FadecandyDevice(List<List<Integer>> map, String type) {
        mMap = map;
        mType = type;
    }

    public FadecandyDevice(JSONObject fadecandyDevice) {

        try {
            if (fadecandyDevice.has(Constants.CONFIG_TYPE) &&
                    fadecandyDevice.has(Constants.CONFIG_MAP)) {

                mType = fadecandyDevice.getString(Constants.CONFIG_TYPE);
                JSONArray map = fadecandyDevice.getJSONArray(Constants.CONFIG_MAP);

                for (int i = 0; i < map.length(); i++) {

                    JSONArray mapItem = map.getJSONArray(i);
                    List<Integer> itemList = new ArrayList<>();
                    for (int j = 0; j < mapItem.length(); j++) {
                        itemList.add(mapItem.getInt(j));
                    }

                    mMap.add(itemList);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJsonObject() {

        JSONObject fadecandyDevice = new JSONObject();

        try {
            JSONArray map = new JSONArray();

            for (int i = 0; i < mMap.size(); i++) {

                JSONArray mapItem = new JSONArray();

                for (int j = 0; j < mMap.get(i).size(); j++) {
                    mapItem.put(mMap.get(i).get(j));
                }

                map.put(mapItem);
            }

            fadecandyDevice.put(Constants.CONFIG_TYPE, mType);
            fadecandyDevice.put(Constants.CONFIG_MAP, map);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fadecandyDevice;
    }

    public String toString() {
        return toJsonObject().toString();
    }
}
