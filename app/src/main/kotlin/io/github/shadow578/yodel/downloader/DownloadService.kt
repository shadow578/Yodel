package io.github.shadow578.yodel.downloader

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleService
import com.google.gson.*
import com.mpatric.mp3agic.*
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLResponse
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.TrackInfo
import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.downloader.wrapper.*
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.preferences.Flags
import io.github.shadow578.yodel.util.preferences.Prefs
import io.github.shadow578.yodel.util.storage.*
import timber.log.Timber
import java.io.*
import java.util.*
import kotlin.math.floor
import kotlin.random.Random

/**
 * tracks downloading service
 */
class DownloaderService : LifecycleService() {
    companion object {
        /**
         * retries for youtube-dl operations
         */
        private const val YOUTUBE_DL_RETRIES = 10

        /**
         * notification id of the progress notification
         */
        private const val PROGRESS_NOTIFICATION_ID = 123456

        /**
         * start the downloader service on demand, as a foreground service
         *
         * @param context the context to launch the service in
         */
        fun startOnDemand(context: Context) {
            val serviceIntent = Intent(context, DownloaderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    /**
     * gson instance
     */
    private val gson = Gson()

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
        notificationManager = NotificationManagerCompat.from(this)

        // ensure downloads are accessible
        if (!checkDownloadsDirSet()) {
            this.toast("Downloads directory not accessible, stopping Downloader!")
            Timber.i("downloads dir not accessible, stopping service")
            stopSelf()
            return
        }

        launchIO {
            try {
                // init youtube-dl
                Timber.i("downloader thread starting...")
                if (!YoutubeDLWrapper.init(this@DownloaderService)) {
                    Timber.e("youtube-dl init failed, stopping service")
                    stopSelf()
                    return@launchIO
                }

                // reset in- progress downloads back to pending
                val tracksDB = TracksDB.get(this@DownloaderService).tracks()
                tracksDB.resetDownloadingToPending()

                // download all pending tracks, stop when no more tracks are available
                while (true) {
                    val pending = tracksDB.pending
                    if (pending.isEmpty()) {
                        Timber.d("no more tracks to download, stopping...")
                        break
                    }
                    pending.forEach {
                        downloadTrack(it)
                    }
                }
            } finally {
                // remove notification and stop
                hideNotification()
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        Timber.i("destroying service...")
        hideNotification()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapLocale())
    }

    /**
     * check if the downloads directory is set and accessible
     *
     * @return is the downloads dir set and accessible?
     */
    private fun checkDownloadsDirSet(): Boolean {
        val downloadsKey = Prefs.DownloadsDirectory.get()
        if (downloadsKey != StorageKey.EMPTY) {
            val downloadsDir = downloadsKey.getPersistedFilePermission(this, true)
            return (downloadsDir != null
                    && downloadsDir.exists()
                    && downloadsDir.canWrite())
        }
        return false
    }

    //region downloader top- level
    /**
     * download a track
     *
     * @param track the track to download
     */
    private fun downloadTrack(track: TrackInfo) {
        Timber.d("starting download of ${track.id}")

        // double- check the track is not downloaded
        val dbTrack = TracksDB.get(this).tracks()[track.id]
        if (dbTrack == null || dbTrack.status != TrackStatus.DownloadPending) {
            Timber.i("skipping download of ${track.id}: appears to already be downloaded")
            return
        }

        // set status to downloading
        track.status = TrackStatus.Downloading
        TracksDB.get(this).tracks().update(track)

        // download the track
        val downloadOk = download(track, Prefs.DownloadFormat.get())

        // update the entry in the DB
        track.status = if (downloadOk) TrackStatus.Downloaded else TrackStatus.DownloadFailed
        TracksDB.get(this).tracks().update(track)
    }

    /**
     * download the track and resolve metadata
     *
     * @param track  the track to download
     * @param format the file format to download the track in
     * @return was the download successful?
     */
    private fun download(track: TrackInfo, format: TrackDownloadFormat): Boolean {
        var files: TempFiles? = null
        return try {
            // create session
            updateNotification(
                createStatusNotification(
                    track,
                    R.string.dl_status_starting_download
                )
            )
            val session = createSession(track, format)
            files = createTempFiles(track, format)

            // download the track and metadata using youtube-dl
            downloadTrack(track, session, files)

            // parse the metadata
            updateNotification(createStatusNotification(track, R.string.dl_status_process_metadata))
            parseMetadata(track, files)

            // write id3v2 metadata for mp3 files
            // if this fails, we do not fail the whole operation
            if (format.supportsID3Tags && Prefs.EnableMetadataTagging.get())
                try {
                    writeID3Tag(track, files)
                } catch (e: DownloaderException) {
                    Timber.e(
                        e,
                        "failed to write id3v2 tags of ${track.id}! (not fatal, the rest of the download was successful)"
                    )
                    maybeShowErrorNotification(e, track)
                }

            // copy audio file to downloads dir
            updateNotification(createStatusNotification(track, R.string.dl_status_finish))
            copyAudioToFinal(track, files, format)

            // copy cover to cover store
            // if this fails, we do not fail the whole operation
            try {
                copyCoverToFinal(track, files)
            } catch (e: DownloaderException) {
                Timber.e(
                    e,
                    "failed to copy cover of ${track.id}! (not fatal, the rest of the download was successful)"
                )
                maybeShowErrorNotification(e, track)
            }
            true
        } catch (e: DownloaderException) {
            Timber.e(e, "download of ${track.id} failed!")
            maybeShowErrorNotification(e, track)
            false
        } finally {
            // delete temp files
            if (files != null && !files.delete())
                Timber.w("could not delete temp files for ${track.id}")
        }
    }
    //endregion

    //region downloader implementation
    /**
     * prepare a new youtube-dl session for the track
     *
     * @param track  the track to prepare the session for
     * @param format the file format to download the track in
     * @return the youtube-dl session
     * @throws DownloaderException if the cache directory could not be created (needed for the session)
     */
    @Throws(DownloaderException::class)
    private fun createSession(track: TrackInfo, format: TrackDownloadFormat): YoutubeDLWrapper {
        val session = YoutubeDLWrapper(resolveVideoUrl(track))
            .cacheDir(downloadCacheDirectory)
            .audioOnly(format.fileExtension)

        // enable ssl fix
        if (Flags.NonSSLDownloads.value)
            session.fixSsl()
        return session
    }

    /**
     * create the temporary files for the download
     *
     * @param track  the track to create the files for
     * @param format the file format to download in
     * @return the temporary files
     */
    private fun createTempFiles(track: TrackInfo, format: TrackDownloadFormat): TempFiles {
        val tempAudio = cacheDir.getTempFile("dl_" + track.id, "")
        return TempFiles(tempAudio, format.fileExtension)
    }

    /**
     * invoke youtube-dl to download the track + metadata + thumbnail
     *
     * @param track   the track to download
     * @param session the current youtube-dl session
     * @param files   the files to write
     * @throws DownloaderException if download fails
     */
    @Throws(DownloaderException::class)
    private fun downloadTrack(track: TrackInfo, session: YoutubeDLWrapper, files: TempFiles) {
        // make sure all files to create are non- existent
        files.delete()

        // prepare the download session
        session.apply {
            output(files.audio)
            //overwriteExisting()
            writeMetadata()
            writeThumbnail()

            // enable verbose output with devtools toggle
            if (Flags.DownloaderVerboseOutput.value) {
                verboseOutput()
                printOutput = true
            }
        }

        // prepare a callback to show the progress in a notification
        val progressCallback = { progress: Float, etaInSeconds: Long, line: String ->
            Timber.d("downloader status: $line")
            updateNotification(
                createProgressNotification(track, progress / 100.0, etaInSeconds)
            )
        }

        // download with multiple tries
        var response: YoutubeDLResponse? = null
        val downloaderOutputs = StringBuilder()
        for (i in 0..YOUTUBE_DL_RETRIES) {
            // append try no info
            downloaderOutputs.append("try $i of $YOUTUBE_DL_RETRIES for ${track.id}")

            try {
                // execute download
                response = session.download(progressCallback)

                // append the output when successful
                downloaderOutputs.append(response.toPrettyString())
            } catch (e: YoutubeDLException) {
                // log and append to output info
                Timber.e(e, "download of ${track.id} failed")
                downloaderOutputs.append("${e.message} \r\n${e.stackTraceToString()}")
            } catch (e: InterruptedException) {
                // log and append to output info
                Timber.e(e, "download of ${track.id} was interrupted")
                downloaderOutputs.append("${e.message} \r\n${e.stackTraceToString()}")
            }

            // exit when download was successful
            if (response != null && response.exitCode == 0) break

            // append newlines to output on retry
            downloaderOutputs.append("\r\n\r\n")
        }

        // check if download was actually successful
        if (response == null || !files.audio.exists() || !files.metadataJson.exists())
            throw DownloaderException(
                "youtube-dl download failed!",
                dlOutput = downloaderOutputs.toString()
            )
    }

    /**
     * parse the metadata file and update the values in the track
     *
     * @param track the track to update
     * @param files the files created by youtube-dl
     * @throws DownloaderException if parsing fails
     */
    @Throws(DownloaderException::class)
    private fun parseMetadata(track: TrackInfo, files: TempFiles) {
        // check metadata file exists
        if (!files.metadataJson.exists())
            throw DownloaderException("metadata file not found!")

        // deserialize the file
        val metadata: TrackMetadata
        try {
            FileReader(files.metadataJson).use { reader ->
                metadata = gson.fromJson(
                    reader,
                    TrackMetadata::class.java
                )
            }
        } catch (e: IOException) {
            throw DownloaderException("deserialization of the metadata file failed", e)
        } catch (e: JsonIOException) {
            throw DownloaderException("deserialization of the metadata file failed", e)
        } catch (e: JsonSyntaxException) {
            throw DownloaderException("deserialization of the metadata file failed", e)
        }

        // set track data
        metadata.getTrackTitle()?.let { track.title = it }
        metadata.getArtistName()?.let { track.artist = it }
        metadata.getUploadDate()?.let { track.releaseDate = it }
        metadata.duration?.let { track.duration = it }
        metadata.album?.let { track.albumName = it }
    }

    /**
     * copy the temporary audio file to the final destination
     *
     * @param track  the track to download
     * @param files  the temporary files, of which the audio file is copied to the downloads dir
     * @param format the file format that was used for the download
     * @throws DownloaderException if creating the final file or the copy operation fails
     */
    @Throws(DownloaderException::class)
    private fun copyAudioToFinal(track: TrackInfo, files: TempFiles, format: TrackDownloadFormat) {
        // check audio file exists
        if (!files.audio.exists())
            throw DownloaderException("cannot find audio file to copy")

        // find root folder for saving downloaded tracks to
        // find using storage framework, and only allow persisted folders we can write to
        val downloadRoot = downloadsDirectory
            ?: throw DownloaderException("failed to find downloads folder")

        // create a final file to write the track to, that does not currently exist
        // if we do not check this ourself, the file extension may be messed up, causing external players to
        // fail to play the file
        var finalFileName: String
        var c = 0
        do {
            // build suffix
            // on the first iteration, there is no suffix
            var suffix = ""
            if (c > 0)
                suffix = " ($c)"
            c++

            // build file display name
            finalFileName = "${track.title}${suffix}.${format.fileExtension}"
        } while (downloadRoot.findFile(finalFileName) != null)

        // create file to write the track to
        val finalFile = downloadRoot.createFile(format.mimetype, finalFileName)
        if (finalFile == null || !finalFile.canWrite())
            throw DownloaderException("Could not create final output file!")

        // copy the temp file to the final destination
        try {
            FileInputStream(files.audio).use { src ->
                contentResolver.openOutputStream(finalFile.uri).use { out ->
                    src.copyTo(out!!)
                }
            }
        } catch (e: IOException) {
            // try to remove the final file
            if (!finalFile.delete())
                Timber.w("failed to delete final file on copy fail")

            throw DownloaderException(
                "error copying temp file (${files.audio}) to final destination (${finalFile.uri})",
                e
            )
        }

        // set the final file in track info
        track.audioFileKey = finalFile.encodeToKey()
    }

    /**
     * copy the album cover to the final destination
     *
     * @param track the track to copy the cover of
     * @param files the files downloaded by youtube-dl
     * @throws DownloaderException if copying the cover fails
     */
    @Throws(DownloaderException::class)
    private fun copyCoverToFinal(track: TrackInfo, files: TempFiles) {
        // check thumbnail file exists
        val thumbnail = files.thumbnail
        if (thumbnail == null || !thumbnail.exists())
            throw DownloaderException("cannot find thumbnail file")

        // get covers directory
        val coverRoot = coverArtDirectory

        // create file for the thumbnail
        val coverFile = File(coverRoot, "${track.id}_${generateRandomAlphaNumeric(64)}.webp")

        // read temporary thumbnail file and write as webp in cover art directory
        try {
            FileInputStream(thumbnail).use { src ->
                FileOutputStream(coverFile).use { out ->
                    val format =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
                    val cover = BitmapFactory.decodeStream(src)
                    cover.compress(format, 100, out)
                    cover.recycle()
                }
            }
        } catch (e: IOException) {
            throw DownloaderException("failed to save cover as webp", e)
        }

        // set the cover file key in track
        track.coverKey = DocumentFile.fromFile(coverFile).encodeToKey()
    }

    /**
     * write the track metadata to the id3v2 tag of the file
     *
     * @param track the track data
     * @param files the files downloaded by youtube-dl
     * @throws DownloaderException if writing the id3 tag fails
     */
    @Throws(DownloaderException::class)
    private fun writeID3Tag(track: TrackInfo, files: TempFiles) {
        try {
            // clear all previous id3 tags, and create a new & empty one
            val mp3Wrapper = MP3agicWrapper(files.audio)
            val tag = mp3Wrapper
                .clearAllTags()
                .tag

            // write basic metadata (title, artist, album, ...)
            tag.title = track.title
            track.artist?.let { tag.artist = it }
            track.releaseDate?.let { tag.year = String.format(Locale.US, "%04d", it.year) }
            track.albumName?.let { tag.album = it }

            // set cover art (if thumbnail was downloaded)
            val thumbnail = files.thumbnail
            if (thumbnail != null && thumbnail.exists()) {
                try {
                    FileInputStream(thumbnail).use { src ->
                        ByteArrayOutputStream().use { out ->
                            // convert to png
                            val cover = BitmapFactory.decodeStream(src)
                            cover.compress(Bitmap.CompressFormat.PNG, 100, out)
                            cover.recycle()

                            // write cover to tag
                            tag.setAlbumImage(out.toByteArray(), "image/png")
                        }
                    }
                } catch (e: IOException) {
                    Timber.e(e, "failed to convert cover image to PNG")
                }
            }

            // save the file with tags
            mp3Wrapper.save()
        } catch (e: IOException) {
            throw DownloaderException("could not write id3v2 tag to file!", e)
        } catch (e: NotSupportedException) {
            throw DownloaderException("could not write id3v2 tag to file!", e)
        } catch (e: InvalidDataException) {
            throw DownloaderException("could not write id3v2 tag to file!", e)
        } catch (e: UnsupportedTagException) {
            throw DownloaderException("could not write id3v2 tag to file!", e)
        }
    }

    //region util
    /**
     * get the video url youtube-dl should use for a track
     *
     * @param track the track to get the video url of
     * @return the video url
     */
    private fun resolveVideoUrl(track: TrackInfo): String {
        return if (Flags.OnlyUseVideoID.value)
            track.id
        else {
            if (Flags.NonSSLDownloads.value)
                "http://www.youtube.com/watch?v=${track.id}"
            else
                "https://www.youtube.com/watch?v=${track.id}"
        }
    }

    /**
     * get the [Prefs.DownloadsDirectory] of the app, using storage framework
     *
     * @return the optional download root directory
     */
    private val downloadsDirectory: DocumentFile?
        get() {
            val key = Prefs.DownloadsDirectory.get()
            return if (key == StorageKey.EMPTY) null else key.getPersistedFilePermission(this, true)
        }

    /**
     * get the cover art directory
     *
     * @return the directory to save cover art to
     * @throws DownloaderException if the directory could not be created
     */
    @get:Throws(DownloaderException::class)
    val coverArtDirectory: File
        get() {
            val coversDir = File(noBackupFilesDir, "cover_store")
            if (!coversDir.exists() && !coversDir.mkdirs())
                throw DownloaderException("could not create cover_store directory")

            return coversDir
        }

    /**
     * get the youtube-dl cache directory
     *
     * @return the cache directory
     * @throws DownloaderException if creating the directory failed
     */
    @get:Throws(DownloaderException::class)
    val downloadCacheDirectory: File
        get() {
            val cacheDir = File(cacheDir, "youtube-dl_cache")
            if (!cacheDir.exists() && !cacheDir.mkdirs())
                throw DownloaderException("could not create youtube-dl_cache directory")

            return cacheDir
        }
    //endregion
    //endregion

    //region status notification
    /**
     * update the progress notification
     *
     * @param newNotification the updated notification
     */
    private fun updateNotification(newNotification: Notification) {
        if (isInForeground) {
            // already in foreground, update the notification
            notificationManager.notify(PROGRESS_NOTIFICATION_ID, newNotification)
        } else {
            // create foreground notification
            isInForeground = true
            startForeground(PROGRESS_NOTIFICATION_ID, newNotification)
        }
    }

    /**
     * cancel the progress notification and call [stopForeground]
     */
    private fun hideNotification() {
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        isInForeground = false
    }

    /**
     * create a download progress display notification (during track download)
     *
     * @param track    the track that is being downloaded
     * @param progress the current download progress, from 0.0 to 1.0
     * @param eta      the estimated download time remaining, in seconds
     * @return the progress notification
     */
    private fun createProgressNotification(
        track: TrackInfo,
        progress: Double,
        eta: Long
    ): Notification {
        return baseNotification
            .setContentTitle(track.title)
            .setSubText(getString(R.string.dl_notification_subtext, eta.secondsToTimeString()))
            .setProgress(100, floor(progress * 100).toInt(), false)
            .build()
    }

    /**
     * create a download prepare display notification (before or after track download)
     *
     * @param track     the track that is being downloaded
     * @param statusRes the status string
     * @return the status notification
     */
    private fun createStatusNotification(
        track: TrackInfo,
        @StringRes statusRes: Int
    ): Notification {
        return baseNotification
            .setContentTitle(track.title)
            .setSubText(getString(statusRes))
            .setProgress(1, 0, true)
            .build()
    }

    /**
     * get the base notification, shared between all status notifications
     *
     * @return the builder, with base settings applied
     */
    private val baseNotification: NotificationCompat.Builder
        get() = NotificationCompat.Builder(this, NotificationChannels.DownloadProgress.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)

    //endregion

    //region error notifications
    /**
     * show a downloader error notification when enabled in prefs
     *
     * @param error the error thrown by the downloader service
     * @param track the track that caused the error while downloading
     */
    private fun maybeShowErrorNotification(error: DownloaderException, track: TrackInfo) {
        // skip if not enabled
        if (!Flags.NotificationsOnDownloadError.value)
            return

        // create the notification
        val notificationId = Random.nextInt()
        val notification = NotificationCompat.Builder(this, NotificationChannels.DeveloperTools.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setShowWhen(false)
            .setContentTitle(error.message)
            .setAutoCancel(true)

        // if we have output of the downloader, write it to a file we
        // can open when the notification is clicked
        if (!error.downloaderOutput.isNullOrEmpty()) {
            // write the file in cache
            val dir = File(cacheDir, "downloader_errors")
            val errorOutputFile = dir.getTempFile("dl_err_${track.id}_", ".txt")
            dir.mkdirs()
            errorOutputFile.appendText(error.downloaderOutput)

            // add action to notification
            notification.addAction(
                NotificationCompat.Action(
                    null,
                    "View Logs",
                    PendingIntent.getBroadcast(
                        this,
                        0,
                        Intent(DownloaderErrorOpenBroadcastReceiver.ACTION_OPEN_ERROR_OUTPUT).apply {
                            setClass(
                                this@DownloaderService,
                                DownloaderErrorOpenBroadcastReceiver::class.java
                            )
                            putExtra(
                                DownloaderErrorOpenBroadcastReceiver.EXTRA_NOTIFICATION_ID,
                                notificationId
                            )
                            data = getContentUri(errorOutputFile)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        },
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
        }

        // send the notification with a randomized id
        // this way, we can have multiple notifications (for multiple errors)
        notificationManager.notify(notificationId, notification.build())
    }

    //endregion
}