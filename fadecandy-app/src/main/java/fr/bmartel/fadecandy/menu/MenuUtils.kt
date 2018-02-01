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
package fr.bmartel.fadecandy.menu

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import fr.bmartel.android.fadecandy.model.ServiceType
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.dialog.AboutDialog
import fr.bmartel.fadecandy.dialog.ConfigurationDialog
import fr.bmartel.fadecandy.dialog.LedStripConfigurationDialog
import fr.bmartel.fadecandy.dialog.OpenSourceItemsDialog
import fr.bmartel.fadecandy.inter.IFc

/**
 * Some functions used to manage Menu
 *
 * @author Bertrand Martel
 */
object MenuUtils {

    /**
     * Execute actions according to selected menu item
     *
     * @param menuItem MenuItem object
     * @param mDrawer  navigation drawer
     * @param context  android context
     */
    fun selectDrawerItem(menuItem: MenuItem, mDrawer: DrawerLayout, context: Context, fcActivity: IFc) {
        when (menuItem.itemId) {
            R.id.server_status -> {
                fcActivity.switchServerStatus()
            }
            R.id.server_config -> {
                val dialog = ConfigurationDialog(activity = fcActivity)
                fcActivity.setCurrentDialog(dialog = dialog)
                dialog.show()
            }
            R.id.ledstrip_config -> {
                val dialog = LedStripConfigurationDialog(mActivity = fcActivity)
                fcActivity.setCurrentDialog(dialog = dialog)
                dialog.show()
            }
            R.id.open_source_components -> {
                val dialog = OpenSourceItemsDialog(context = context)
                fcActivity.setCurrentDialog(dialog = dialog)
                dialog.show()
            }
            R.id.rate_app -> {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.applicationContext.packageName)))
            }
            R.id.about_app -> {
                val dialog = AboutDialog(context = context)
                fcActivity.setCurrentDialog(dialog = dialog)
                dialog.show()
            }
            R.id.report_bugs -> {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.resources.getString(R.string.issue))))
            }
            R.id.service_config -> {

                val array = arrayOf<CharSequence>("persistent", "non persistent")

                var indexCheck = 0
                if (fcActivity.serviceType == ServiceType.NON_PERSISTENT_SERVICE) {
                    indexCheck = 1
                }

                val dialog = AlertDialog.Builder(context)
                        .setSingleChoiceItems(array, indexCheck, null)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                            if (selectedPosition == 1) {
                                fcActivity.serviceType = ServiceType.NON_PERSISTENT_SERVICE
                            } else {
                                fcActivity.serviceType = ServiceType.PERSISTENT_SERVICE
                            }
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                        .show()
                fcActivity.setCurrentDialog(dialog = dialog)
            }
        }
        mDrawer.closeDrawers()
    }
}