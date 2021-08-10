package io.github.shadow578.yodel.util

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import io.github.shadow578.yodel.R

/**
 * base foreground service implementation. handles notification show / hide automatically.
 * this service also follows the AppLocaleOverride
 *
 * @param notificationId id of the foreground notification
 * @param notificationChannel channel of the foreground notification
 */
abstract class YodelForegroundService(
        val notificationId: Int,
        val notificationChannel: NotificationChannels
) : LifecycleService() {
    companion object {
        /**
         * logging tag
         */
        private const val TAG = "FGService"
    }

    /**
     * notification manager, for progress notification
     */
    private lateinit var notificationManager: NotificationManagerCompat

    /**
     * is the service currently in foreground?
     */
    private var isInForeground = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "creating service...")
        notificationManager = NotificationManagerCompat.from(this)
    }

    override fun onDestroy() {
        Log.i(TAG, "destroying service...")
        cancelForeground()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapLocale())
    }

    /**
     * update the foreground notification.
     * the first time this is called (after [cancelForeground]), the service is brought to foreground using [startForeground]
     *
     * @param newNotification the updated notification
     */
    protected fun updateForeground(newNotification: Notification) {
        if (isInForeground) {
            // already in foreground, update the notification
            notificationManager.notify(notificationId, newNotification)
        } else {
            // create foreground notification
            isInForeground = true
            startForeground(notificationId, newNotification)
        }
    }

    /**
     * cancel the foreground notification and call [stopForeground]
     */
    protected fun cancelForeground() {
        notificationManager.cancel(notificationId)
        stopForeground(true)
        isInForeground = false
    }

    /**
     * create a new notification with basic settings applied
     *
     * @return the builder, with base settings applied
     */
    protected fun newNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, notificationChannel.id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
    }
}