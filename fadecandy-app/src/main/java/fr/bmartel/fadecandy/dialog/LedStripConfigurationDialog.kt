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
import android.widget.EditText
import android.widget.Toast
import fr.bmartel.android.fadecandy.constant.Constants
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.inter.IFc

/**
 * Led count configuration.
 *
 * @author Bertrand Martel
 */
class LedStripConfigurationDialog(private val mActivity: IFc) : AlertDialog(mActivity.context) {

    private val mLedCountEditText: EditText

    init {
        val inflater = layoutInflater
        val dialoglayout = inflater.inflate(R.layout.led_count_dialog, null)
        setView(dialoglayout)

        mLedCountEditText = dialoglayout.findViewById(R.id.led_count_edit)
        mLedCountEditText.setText("${mActivity.ledCount}")
        mLedCountEditText.setSelection(mLedCountEditText.text.length)

        setTitle(R.string.configuration_led_title)
        setButton(DialogInterface.BUTTON_POSITIVE, mActivity.context.resources.getString(R.string.dialog_ok)) { _, _ -> }
        setButton(DialogInterface.BUTTON_NEGATIVE, mActivity.context.getString(R.string.cancel_button)) { _, _ -> }
    }

    public override fun onStart() {
        super.onStart()

        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val ledCount = Integer.parseInt(mLedCountEditText.text.toString())

            if (ledCount > 0 && ledCount <= Constants.MAX_LED) {
                mActivity.ledCount = Integer.parseInt(mLedCountEditText.text.toString())
                dismiss()
            } else {
                Toast.makeText(mActivity.context, mActivity.context.getString(R.string.led_count_errorcase) + " ${Constants.MAX_LED}", Toast.LENGTH_SHORT).show()
            }
        }

    }


}
