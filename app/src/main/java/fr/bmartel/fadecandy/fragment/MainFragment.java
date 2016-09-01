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

import android.view.View;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.inter.IFragment;

/**
 * Common fragment.
 *
 * @author Bertrand Martel
 */
public abstract class MainFragment extends android.support.v4.app.Fragment implements IFragment {

    protected FadecandySingleton mSingleton;

    protected DiscreteSeekBar mBrightnessSeekBar;

    protected boolean mIsSpark = false;

    public void onCreate(View view) {

        mSingleton = FadecandySingleton.getInstance(getActivity().getApplicationContext());

        mBrightnessSeekBar = (DiscreteSeekBar) view.findViewById(R.id.seekbar_brightness);

        mBrightnessSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });

        mBrightnessSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

                if (mIsSpark) {
                    mSingleton.setColorCorrectionSpark(value);
                } else {
                    mSingleton.setColorCorrection(value);
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        if (mSingleton.getCurrentColorCorrection() == -1) {
            FadecandyConfig config = mSingleton.getConfig();

            if (config != null) {
                mBrightnessSeekBar.setProgress((int) ((float) config.getFcColor().getWhitepoints().get(0) * 100));
            }
        } else {
            mBrightnessSeekBar.setProgress(mSingleton.getCurrentColorCorrection());
        }
    }

    @Override
    public void onServerFirstStart() {

        if (mSingleton.getCurrentColorCorrection() == -1) {
            FadecandyConfig config = mSingleton.getConfig();

            if (config != null && mBrightnessSeekBar != null) {
                mBrightnessSeekBar.setProgress((int) ((float) config.getFcColor().getWhitepoints().get(0) * 100));
            }
        } else {
            mBrightnessSeekBar.setProgress(mSingleton.getCurrentColorCorrection());
        }
    }
}
