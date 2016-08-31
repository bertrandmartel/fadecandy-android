package fr.bmartel.fadecandy.fragment;

import android.view.View;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import fr.bmartel.android.fadecandy.model.FadecandyConfig;
import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.inter.IFragment;

/**
 * Created by akinaru on 31/08/16.
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
