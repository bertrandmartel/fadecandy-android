package fr.bmartel.android.fadecandy.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import fr.bmartel.android.fadecandy.R;
import fr.bmartel.android.fadecandy.service.FadecandyService;

/**
 * Created by akinaru on 29/08/16.
 */
public class NotificationHelper {

    public final static int NOTIFICATION_ID = 4242;

    private static PendingIntent createLaunchIntent(Context context, Intent activityIntent) {
        if (activityIntent != null) {
            return PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return null;
    }


    public static void cancelNotification(Context context) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancel(NOTIFICATION_ID);
    }

    public static Notification createNotification(Context context, String content, Intent activityIntent) {

        String title = context.getString(R.string.app_name);
        PendingIntent pendingIntent = createLaunchIntent(context, activityIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);

        Intent yesReceive = new Intent();
        yesReceive.setAction(FadecandyService.ACTION_EXIT);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_power_settings_new, "stop background service", pendingIntentYes);

        //builder.addAction(generateAction(context, R.drawable.ic_power_settings_new, NotificationService.ACTION_EXIT));

        return builder.build();
    }

    /*
    private static NotificationCompat.Action generateAction(Context context, int icon, String intentAction) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(intentAction);

        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action(icon, null, pendingIntent);
    }
    */

}
