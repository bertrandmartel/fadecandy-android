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
import android.widget.ArrayAdapter;
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
        final Spinner sItems = (Spinner) dialoglayout.findViewById(R.id.ip_spinner);
        sItems.setAdapter(adapter);
        sItems.setSelection(defaultPos);

        final EditText portEditText = (EditText) dialoglayout.findViewById(R.id.port_edit);
        portEditText.setText("" + activity.getServerPort());
        portEditText.setSelection(portEditText.getText().length());

        setTitle(R.string.configuration_server_title);

        setButton(DialogInterface.BUTTON_POSITIVE, activity.getContext().getResources().getString(R.string.dialog_ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.setServerAddress(sItems.getSelectedItem().toString());
                activity.setServerPort(Integer.parseInt(portEditText.getText().toString()));
                activity.restartServer();
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, activity.getContext().getString(R.string.cancel_button), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }
}