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
import android.content.Context
import android.content.DialogInterface
import android.widget.TextView
import fr.bmartel.fadecandy.BuildConfig
import fr.bmartel.fadecandy.R

/**
 * About dialog
 *
 * @author Bertrand Martel
 */
class AboutDialog(context: Context) : AlertDialog(context) {

    init {
        val inflater = layoutInflater
        val dialoglayout = inflater.inflate(R.layout.about_dialog, null)
        setView(dialoglayout)

        val name = dialoglayout.findViewById<TextView>(R.id.name)
        val copyright = dialoglayout.findViewById<TextView>(R.id.copyright)
        val githubLink = dialoglayout.findViewById<TextView>(R.id.github_link)

        name.text = context.resources.getString(R.string.app_name) + " v ${BuildConfig.VERSION_NAME}"
        copyright.setText(R.string.copyright)
        githubLink.setText(R.string.github_link)

        setTitle(R.string.about)
        setButton(DialogInterface.BUTTON_POSITIVE, context.resources.getString(R.string.dialog_ok),
                null as DialogInterface.OnClickListener?)
    }
}