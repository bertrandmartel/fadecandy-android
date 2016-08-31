package fr.bmartel.fadecandy.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import fr.bmartel.fadecandy.FadecandySingleton;
import fr.bmartel.fadecandy.R;

/**
 * Created by akinaru on 31/08/16.
 */
public class TemperatureFragment extends android.support.v4.app.Fragment {

    private FadecandySingleton mSingleton;

    private final static String TAG = FullColorFragment.class.getSimpleName();

    public TemperatureFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.temperature_fragment, container, false);

        mSingleton = FadecandySingleton.getInstance(getActivity().getApplicationContext());

        DiscreteSeekBar brightnessSeekbar = (DiscreteSeekBar) view.findViewById(R.id.seekbar_brightness);

        brightnessSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });

        brightnessSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

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
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });


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
