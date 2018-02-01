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
package fr.bmartel.fadecandy.inter

import android.app.Dialog
import android.content.Context
import android.support.v4.app.Fragment
import fr.bmartel.android.fadecandy.model.ServiceType

/**
 * Activity interface.
 *
 * @author Bertrand Martel
 */
interface IFc {
    fun getConfig(): String?

    fun getSFragmentManager(): android.support.v4.app.FragmentManager

    /**
     * start the server if stopped / stop the server if started.
     */
    fun switchServerStatus()

    /**
     * restart Fadecandy server.
     */
    fun restartServer()

    /**
     * set opened dialog in activity
     *
     * @param dialog
     */
    fun setCurrentDialog(dialog: Dialog)

    /**
     * restart connection to remote websocket server.
     */
    fun restartRemoteConnection()

    fun setReplaceableFragment(fragment: Fragment)

    fun clearBackStack()

    var serviceType: ServiceType

    val context: Context

    var ipAddress: String

    var ledCount: Int

    var serverPort: Int

    var serverMode: Boolean

    var remoteServerIp: String

    var remoteServerPort: Int

    var sparkSpan: Int
}
