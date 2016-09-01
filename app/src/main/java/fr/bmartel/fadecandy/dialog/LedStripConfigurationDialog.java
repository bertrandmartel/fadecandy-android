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
 * Led count configuration.
 *
 * @author Bertrand Martel
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

        setButton(DialogInterface.BUTTON_NEGATIVE, activity.getContext().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
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
                    Toast.makeText(mActivity.getContext(), mActivity.getContext().getString(R.string.led_count_errorcase) + " " + Constants.MAX_LED, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
