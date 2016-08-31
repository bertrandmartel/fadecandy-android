package fr.bmartel.fadecandy.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import fr.bmartel.android.fadecandy.constant.Constants;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.inter.IFc;

/**
 * Created by akinaru on 31/08/16.
 */
public class LedStripConfigurationDialog extends AlertDialog {

    private EditText mLedCountEditText;

    private IFc mActivity;

    public LedStripConfigurationDialog(IFc activity) {
        super(activity.getContext());

        mActivity = activity;

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.led_count_dialog, null);
        setView(dialoglayout);

        mLedCountEditText = (EditText) dialoglayout.findViewById(R.id.led_count_edit);
        mLedCountEditText.setText("" + activity.getLedCount());
        mLedCountEditText.setSelection(mLedCountEditText.getText().length());

        setTitle(R.string.configuration_led_title);

        setButton(DialogInterface.BUTTON_POSITIVE, activity.getContext().getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int ledCount = Integer.parseInt(mLedCountEditText.getText().toString());

                if (ledCount > 0 && ledCount <= Constants.MAX_LED) {
                    mActivity.setLedCount(Integer.parseInt(mLedCountEditText.getText().toString()));
                    dismiss();
                } else {
                    Toast.makeText(mActivity.getContext(), "Led count must be > 0 and <= " + Constants.MAX_LED, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
