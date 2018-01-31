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
package fr.bmartel.fadecandy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import fr.bmartel.fadecandy.R;

/**
 * Adapter for open source projects
 *
 * @author Bertrand Martel
 */
public class OpenSourceItemAdapter extends BaseAdapter {

    private static final String[][] COMPONENTS = new String[][]{

            {"fadecandy", "https://github.com/scanlime/fadecandy"},
            {"rapidjson",
                    "https://github.com/scanlime/rapidjson"},
            {"libwebsockets",
                    "https://github.com/bertrandmartel/libwebsockets"},
            {"DiscreteSeekBar",
                    "https://github.com/AnderWeb/discreteSeekBar"},
            {"BottomBar",
                    "https://github.com/roughike/BottomBar"},
            {"Android Holo ColorPicker",
                    "https://github.com/LarsWerkman/HoloColorPicker"},
            {"Open Pixel Control Library",
                    "https://github.com/bertrandmartel/opc-java"},
            {"AndroidAsync",
                    "https://github.com/koush/AndroidAsync"},
            {"Ace editor",
                    "https://github.com/ajaxorg/ace"},
            {"JS Beautifier",
                    "https://github.com/beautify-web/js-beautify"},
            {"Led Icon by Kenneth Appiah, CA (Pulic Domain)",
                    "https://thenounproject.com/search/?q=led&i=3156"}
    };

    private LayoutInflater mInflater;

    public OpenSourceItemAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return COMPONENTS.length;
    }

    @Override
    public Object getItem(int position) {
        return COMPONENTS[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.open_source_items, parent, false);
        }

        TextView title = convertView.findViewById(R.id.title);
        TextView url = convertView.findViewById(R.id.url);

        title.setText(COMPONENTS[position][0]);
        url.setText(COMPONENTS[position][1]);

        return convertView;
    }
}