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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;

import fr.bmartel.fadecandy.R;

/**
 * Full Color Fragment.
 *
 * @author Bertrand Martel
 */
public class FullColorFragment extends MainFragment implements ColorPicker.OnColorChangedListener {

    public FullColorFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.full_color_fragment, container, false);

        mIsSpark = false;

        super.onCreate(view);

        //init color picker
        final ColorPicker picker = (ColorPicker) view.findViewById(R.id.picker);
        picker.setOnColorChangedListener(this);
        picker.setShowOldCenterColor(false);
        picker.setColor(mSingleton.getColor());

        Button button_all_off = (Button) view.findViewById(R.id.button_all_off);

        button_all_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSingleton.isServerMode() && !mSingleton.isServerRunning()) {
                    return;
                }
                mSingleton.clearPixel();
            }
        });

        return view;
    }

    @Override
    public void onColorChanged(int color) {

        if (mSingleton.isServerMode() && !mSingleton.isServerRunning()) {
            return;
        }
        mSingleton.setFullColor(color, true, false);
    }

}
