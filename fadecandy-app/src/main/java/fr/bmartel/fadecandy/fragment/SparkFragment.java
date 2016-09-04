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
package fr.bmartel.fadecandy.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import fr.bmartel.fadecandy.R;

/**
 * Spark fragment.
 *
 * @author Bertrand Martel
 */
public class SparkFragment extends MainFragment implements ColorPicker.OnColorChangedListener {

    private final static String TAG = FullColorFragment.class.getSimpleName();

    public SparkFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.spark_fragment, container, false);

        mIsSpark = true;

        super.onCreate(view);

        //init color picker
        ColorPicker picker = (ColorPicker) view.findViewById(R.id.picker);
        picker.setOnColorChangedListener(this);
        picker.setShowOldCenterColor(false);
        picker.setColor(mSingleton.getColor());

        DiscreteSeekBar speedSeekbar = (DiscreteSeekBar) view.findViewById(R.id.seekbar_speed);

        speedSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });

        speedSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                mSingleton.setSpeed(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        DiscreteSeekBar spanSeekbar = (DiscreteSeekBar) view.findViewById(R.id.seekbar_span);

        spanSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });

        spanSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                mSingleton.setSparkSpan(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        speedSeekbar.setProgress(mSingleton.getSpeed());
        spanSeekbar.setProgress(mSingleton.getSparkSpan());

        return view;
    }

    @Override
    public void onColorChanged(int color) {
        mSingleton.spark(color);
    }

    @Override
    public void onServerFirstStart() {

    }
}
