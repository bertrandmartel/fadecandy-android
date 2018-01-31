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
package fr.bmartel.fadecandy.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.inter.IFc;
import fr.bmartel.fadecandy.utils.Utils;

/**
 * Max packet count dialog
 *
 * @author Bertrand Martel
 */
public class ConfigurationDialog extends AlertDialog {

    public ConfigurationDialog(final IFc activity) {
        super(activity.getContext());

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.configuration_dialog, null);
        setView(dialoglayout);

        final CheckBox startServerCheckbox = dialoglayout.findViewById(R.id.start_server_cb);

        boolean serverMode = activity.isServerMode();

        startServerCheckbox.setChecked(serverMode);

        List<String> spinnerArray = Utils.getIPAddress(true);
        String defaultAddr = activity.getIpAddress();
        int defaultPos = 0;
        for (int i = 0; i < spinnerArray.size(); i++) {
            if (defaultAddr.equals(spinnerArray.get(i))) {
                defaultPos = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity.getContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner sItems = dialoglayout.findViewById(R.id.ip_spinner);
        sItems.setAdapter(adapter);
        sItems.setSelection(defaultPos);

        final EditText portEditText = dialoglayout.findViewById(R.id.port_edit);

        if (serverMode) {
            portEditText.setText("" + activity.getServerPort());
        } else {
            portEditText.setText("" + activity.getRemoteServerPort());
        }
        portEditText.setSelection(portEditText.getText().length());

        final EditText localServerEditText = dialoglayout.findViewById(R.id.local_server_edit);
        localServerEditText.setText(activity.getRemoteServerIp());

        if (!serverMode) {
            sItems.setVisibility(View.GONE);
            localServerEditText.setVisibility(View.VISIBLE);
        }

        startServerCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            sItems.setVisibility(View.VISIBLE);
                            localServerEditText.setVisibility(View.GONE);
                        } else {
                            sItems.setVisibility(View.GONE);
                            localServerEditText.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        setTitle(R.string.configuration_server_title);

        setButton(DialogInterface.BUTTON_POSITIVE, activity.getContext().getResources().getString(R.string.dialog_ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (startServerCheckbox.isChecked()) {
                    activity.setServerAddress(sItems.getSelectedItem().toString());
                    activity.setServerPort(Integer.parseInt(portEditText.getText().toString()));
                } else {
                    activity.setRemoteServerIp(localServerEditText.getText().toString());
                    activity.setRemoteServerPort(Integer.parseInt(portEditText.getText().toString()));
                }

                boolean oldMode = activity.isServerMode();

                activity.setServerMode(startServerCheckbox.isChecked());

                if (startServerCheckbox.isChecked()) {
                    activity.restartServer();
                }

                if (oldMode == false && startServerCheckbox.isChecked() == false) {
                    activity.restartRemoteConnection();
                }
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, activity.getContext().getString(R.string.cancel_button), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }
}