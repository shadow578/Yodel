package io.github.shadow578.yodel.downloader

import android.content.*
import androidx.core.app.NotificationManagerCompat

class DownloaderErrorOpenBroadcastReceiver : BroadcastReceiver() {

    companion object {
        /**
         * open a error output log file.
         * the file is passed as intent data
         */
        const val ACTION_OPEN_ERROR_OUTPUT = "io.github.shadow578.yodel.OPEN_ERROR_OUTPUT"

        /**
         * extra for providing the notification id so the notification can be closed
         */
        const val EXTRA_NOTIFICATION_ID = "io.github.shadow578.yodel.NOTIFICATION_ID"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == ACTION_OPEN_ERROR_OUTPUT && intent.data != null) {
            // cancel the notification
            if (intent.hasExtra(EXTRA_NOTIFICATION_ID)) {
                val notifyManager = NotificationManagerCompat.from(context)
                notifyManager.cancel(intent.extras?.getInt(EXTRA_NOTIFICATION_ID) ?: 0)
            }

            // create intent for opening the file
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(intent.data, "text/plain")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // show file open chooser
            context.startActivity(
                Intent.createChooser(
                    openIntent,
                    "View Logs"
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                })
        }
    }
}