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
package fr.bmartel.fadecandy.menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import fr.bmartel.android.fadecandy.model.ServiceType;
import fr.bmartel.fadecandy.R;
import fr.bmartel.fadecandy.dialog.AboutDialog;
import fr.bmartel.fadecandy.dialog.ConfigurationDialog;
import fr.bmartel.fadecandy.dialog.LedStripConfigurationDialog;
import fr.bmartel.fadecandy.dialog.OpenSourceItemsDialog;
import fr.bmartel.fadecandy.inter.IFc;

/**
 * Some functions used to manage Menu
 *
 * @author Bertrand Martel
 */
public class MenuUtils {

    private final static String TAG = MenuUtils.class.getSimpleName();

    /**
     * Execute actions according to selected menu item
     *
     * @param menuItem MenuItem object
     * @param mDrawer  navigation drawer
     * @param context  android context
     */
    public static void selectDrawerItem(MenuItem menuItem, DrawerLayout mDrawer, Context context, final IFc fcActivity) {

        switch (menuItem.getItemId()) {
            case R.id.server_status: {
                fcActivity.switchServerStatus();
                break;
            }
            case R.id.server_config: {
                if (fcActivity != null) {
                    ConfigurationDialog dialog = new ConfigurationDialog(fcActivity);
                    fcActivity.setCurrentDialog(dialog);
                    dialog.show();
                }
                break;
            }
            case R.id.ledstrip_config: {
                if (fcActivity != null) {
                    LedStripConfigurationDialog dialog = new LedStripConfigurationDialog(fcActivity);
                    fcActivity.setCurrentDialog(dialog);
                    dialog.show();
                }
                break;
            }
            case R.id.open_source_components: {
                OpenSourceItemsDialog dialog = new OpenSourceItemsDialog(context);
                fcActivity.setCurrentDialog(dialog);
                dialog.show();
                break;
            }
            case R.id.rate_app: {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getApplicationContext().getPackageName())));
                break;
            }
            case R.id.about_app: {
                AboutDialog dialog = new AboutDialog(context);
                fcActivity.setCurrentDialog(dialog);
                dialog.show();
                break;
            }
            case R.id.report_bugs: {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getResources().getString(R.string.issue))));
                break;
            }
            case R.id.service_config: {

                CharSequence[] array = {"persistent", "non persistent"};

                int indexCheck = 0;
                if (fcActivity.getServiceType() == ServiceType.NON_PERSISTENT_SERVICE) {
                    indexCheck = 1;
                }

                Dialog dialog = new AlertDialog.Builder(context)
                        .setSingleChoiceItems(array, indexCheck, null)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                if (selectedPosition == 1) {
                                    fcActivity.setServiceType(ServiceType.NON_PERSISTENT_SERVICE);
                                } else {
                                    fcActivity.setServiceType(ServiceType.PERSISTENT_SERVICE);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                fcActivity.setCurrentDialog(dialog);

                break;
            }
        }
        mDrawer.closeDrawers();
    }
}