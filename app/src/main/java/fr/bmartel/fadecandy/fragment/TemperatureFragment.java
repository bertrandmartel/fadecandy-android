package fr.bmartel.fadecandy.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import fr.bmartel.fadecandy.R;

/**
 * Created by akinaru on 31/08/16.
 */
public class TemperatureFragment extends MainFragment {

    private final static String TAG = FullColorFragment.class.getSimpleName();

    public TemperatureFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.temperature_fragment, container, false);

        mIsSpark = false;

        super.onCreate(view);

        DiscreteSeekBar temperatureSeekbar = (DiscreteSeekBar) view.findViewById(R.id.seekbar_temperature);

        temperatureSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });

        temperatureSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                Log.v(TAG, "value : " + value);
                int color = 0xFFFF00 + (value * 255 / 100);
                mSingleton.setTemperature(color);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        if (mSingleton.getTemperature() != -1) {

            int value = (mSingleton.getTemperature() - 0xFFFF00) * 100 / 255;

            temperatureSeekbar.setProgress(value);
        }

        Button button_all_off = (Button) view.findViewById(R.id.button_all_off);

        button_all_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSingleton.clearPixel();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
