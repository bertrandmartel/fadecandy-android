/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2016-2018 Bertrand Martel
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.inter.IFc
import fr.bmartel.fadecandy.utils.Utils

/**
 * Max packet count dialog
 *
 * @author Bertrand Martel
 */
class ConfigurationDialog(activity: IFc) : AlertDialog(activity.context) {

    init {
        val inflater = layoutInflater
        val dialoglayout = inflater.inflate(R.layout.configuration_dialog, null)
        setView(dialoglayout)

        val startServerCheckbox = dialoglayout.findViewById<CheckBox>(R.id.start_server_cb)
        startServerCheckbox.isChecked = activity.serverMode

        val spinnerArray = Utils.getIPAddress(true)
        val defaultAddr = activity.ipAddress
        var defaultPos = 0
        for (i in spinnerArray.indices) {
            if (defaultAddr == spinnerArray[i]) {
                defaultPos = i
            }
        }

        val adapter = ArrayAdapter(activity.context, android.R.layout.simple_spinner_item, spinnerArray)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val sItems = dialoglayout.findViewById<Spinner>(R.id.ip_spinner)
        sItems.adapter = adapter
        sItems.setSelection(defaultPos)

        val portEditText = dialoglayout.findViewById<EditText>(R.id.port_edit)

        if (activity.serverMode) {
            portEditText.setText("${activity.serverPort}")
        } else {
            portEditText.setText("${activity.remoteServerPort}")
        }
        portEditText.setSelection(portEditText.text.length)

        val localServerEditText = dialoglayout.findViewById<EditText>(R.id.local_server_edit)
        localServerEditText.setText(activity.remoteServerIp)

        if (!activity.serverMode) {
            sItems.visibility = View.GONE
            localServerEditText.visibility = View.VISIBLE
        }

        startServerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sItems.visibility = View.VISIBLE
                localServerEditText.visibility = View.GONE
            } else {
                sItems.visibility = View.GONE
                localServerEditText.visibility = View.VISIBLE
            }
        }

        setTitle(R.string.configuration_server_title)

        setButton(DialogInterface.BUTTON_POSITIVE, activity.context.resources.getString(R.string.dialog_ok)) { _, _ ->
            if (startServerCheckbox.isChecked) {
                activity.ipAddress = sItems.selectedItem.toString()
                activity.serverPort = Integer.parseInt(portEditText.text.toString())
            } else {
                activity.remoteServerIp = localServerEditText.text.toString()
                activity.remoteServerPort = Integer.parseInt(portEditText.text.toString())
            }

            val oldMode = activity.serverMode
            activity.serverMode = startServerCheckbox.isChecked

            if (startServerCheckbox.isChecked) {
                activity.restartServer()
            }

            if (!oldMode && !startServerCheckbox.isChecked) {
                activity.restartRemoteConnection()
            }
        }
        setButton(DialogInterface.BUTTON_NEGATIVE, activity.context.getString(R.string.cancel_button)) { _, _ -> }
    }
}