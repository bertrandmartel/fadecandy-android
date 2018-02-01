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
package fr.bmartel.android.fadecandy.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat

import fr.bmartel.android.fadecandy.R
import fr.bmartel.android.fadecandy.service.FadecandyService

/**
 * Some functions to create persistent service notification.
 *
 * @author Bertrand Martel
 */
object NotificationHelper {

    /**
     * Create the intent to be launched when user click on notification.
     *
     * @param context        Android context
     * @param activityIntent activity intent that was passed to onStartCommand
     * @return intent
     */
    private fun createLaunchIntent(context: Context, activityIntent: Intent?): PendingIntent? {
        return if (activityIntent != null) {
            PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else null
    }

    /**
     * Create notification for persistent service.
     *
     * @param context        Android context
     * @param content        content to be displayed in notification.
     * @param activityIntent activity intent that was passed to onStartCommand
     * @return Android notification
     */
    fun createNotification(context: Context, content: String?, activityIntent: Intent): Notification {

        val title = context.getString(R.string.app_name)
        val pendingIntent = createLaunchIntent(context = context, activityIntent = activityIntent)

        val builder = NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)

        val yesReceive = Intent()
        yesReceive.action = FadecandyService.ACTION_EXIT
        val pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(R.drawable.ic_power_settings_new, context.getString(R.string.notification_stop_text), pendingIntentYes)

        return builder.build()
    }
}
