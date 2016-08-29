package fr.bmartel.fadecandy.menu;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;

import fr.bmartel.fadecandy.R;
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
                Log.v(TAG, "switch server_status");
                fcActivity.switchServerStatus();
                break;
            }
            case R.id.server_config: {
                Log.v(TAG, "server_config");
                break;
            }
        }
        mDrawer.closeDrawers();
    }
}