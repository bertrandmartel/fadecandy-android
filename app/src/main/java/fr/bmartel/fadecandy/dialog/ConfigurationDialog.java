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
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }
}