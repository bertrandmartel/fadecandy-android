package fr.bmartel.fadecandy.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;

import fr.bmartel.fadecandy.R;

/**
 * Created by akinaru on 31/08/16.
 */
public class FullColorFragment extends MainFragment implements ColorPicker.OnColorChangedListener {

    private final static String TAG = FullColorFragment.class.getSimpleName();

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

        if (mSingleton.getColor() != -1) {
            picker.setColor(mSingleton.getColor());
        }

        Button button_all_off = (Button) view.findViewById(R.id.button_all_off);

        button_all_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSingleton.clearPixel();
            }
        });

        return view;
    }

    @Override
    public void onColorChanged(int color) {

        Log.i(TAG, "color changed : " + Color.red(color) + " - " + Color.green(color) + " - " + Color.blue(color));

        mSingleton.setFullColor(color);
    }

}
