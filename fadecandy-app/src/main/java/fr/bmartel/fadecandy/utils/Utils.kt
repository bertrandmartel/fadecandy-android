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
package fr.bmartel.fadecandy.utils

import java.net.NetworkInterface
import java.util.*

/**
 * Some utility functions.
 */
object Utils {

    /**
     * Get IP address from first non-localhost interface
     */
    fun getIPAddress(useIPv4: Boolean): List<String> {
        val ipList = ArrayList<String>()
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                val sAddr = addr.hostAddress
                val isIPv4 = sAddr.indexOf(':') < 0
                if (useIPv4) {
                    if (isIPv4)
                        ipList.add(sAddr)
                } else {
                    if (!isIPv4) {
                        val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                        if (delim < 0) {
                            ipList.add(sAddr.toUpperCase())
                        } else {
                            ipList.add(sAddr.substring(0, delim).toUpperCase())
                        }
                    }
                }
            }
        }
        // for now eat exceptions
        return ipList
    }
}