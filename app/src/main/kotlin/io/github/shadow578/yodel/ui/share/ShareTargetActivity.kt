package io.github.shadow578.yodel.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.downloader.DownloaderService
import io.github.shadow578.yodel.ui.InsertTrackUIHelper
import io.github.shadow578.yodel.util.extractTrackId

/**
 * activity that handles shared youtube links (for download)
 */
class ShareTargetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        // action is SHARE
        if (intent != null && Intent.ACTION_SEND == intent.action) {
            if (handleShare(intent))
                Toast.makeText(this, R.string.share_toast_ok, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.share_toast_fail, Toast.LENGTH_SHORT).show()
        }

        // done
        finish()
    }

    /**
     * handle a shared video url
     *
     * @param intent the share intent (with ACTION_SEND)
     * @return could the url be handled successfully?
     */
    private fun handleShare(intent: Intent): Boolean {
        // type is text/plain
        if (!"text/plain".equals(intent.type, ignoreCase = true))
            return false

        // has EXTRA_TEXT
        if (!intent.hasExtra(Intent.EXTRA_TEXT))
            return false

        // get shared url from text
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (url == null || url.isEmpty())
            return false

        // get video ID
        val trackId = extractTrackId(url) ?: return false

        // get title if possible
        var title: String? = null
        if (intent.hasExtra(Intent.EXTRA_TITLE))
            title = intent.getStringExtra(Intent.EXTRA_TITLE)

        // add to db as pending download
        InsertTrackUIHelper.insertTrack(this, trackId, title)

        // start downloader as needed
        val serviceIntent = Intent(this, DownloaderService::class.java)
        startService(serviceIntent)
        return true
    }
}