package io.github.shadow578.yodel.downloader

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.*

/**
 * track metadata POJO. this is in the format that youtube-dl writes with the --write-info-json option
 * a lot of data is left out, as it's not really relevant for what we're doing (stuff like track info, thumbnails, ...)
 */
data class TrackMetadata(
    /**
     * the full video title. for music videos, this often is in the format 'Artist - Song Title'
     */
    @SerializedName("title")
    val title: String? = null,

    /**
     * the alternative video title. for music videos, this often is just the 'Song Title' (in contrast to [.title]).
     * seems to be the same value as [.track]
     */
    @SerializedName("alt_title")
    val alt_title: String? = null,

    /**
     * the upload date, in the format yyyyMMdd (that is without ANY spaces: 20200924 == 2020-09-24)
     */
    @SerializedName("upload_date")
    val upload_date: String? = null,

    /**
     * the display name of the channel that uploaded the video
     */
    @SerializedName("channel")
    val channel: String? = null,

    /**
     * the duration of the video, in seconds
     */
    @SerializedName("duration")
    val duration: Long? = null,

    /**
     * the title of the track. this seems to be the same as [.alt_title]
     */
    @SerializedName("track")
    val track: String? = null,

    /**
     * the name of the actual song creator (not uploader channel).
     * This seems to be data from Content-ID
     */
    @SerializedName("creator")
    val creator: String? = null,

    /**
     * the name of the actual song artist (not uploader channel).
     * This seems to be data from Content-ID
     */
    @SerializedName("artist")
    val artist: String? = null,

    /**
     * the display name of the album this track is from.
     * only included for songs that are part of a album
     */
    @SerializedName("album")
    val album: String? = null,

    /**
     * categories of the video (like 'Music', 'Entertainment', 'Gaming' ...)
     */
    @SerializedName("categories")
    val categories: List<String>? = null,

    /**
     * tags on the video
     */
    @SerializedName("tags")
    val tags: List<String>? = null,

    /**
     * total view count of the video
     */
    @SerializedName("view_count")
    val view_count: Long? = null,

    /**
     * total likes on the video
     */
    @SerializedName("like_count")
    val like_count: Long? = null,

    /**
     * total dislikes on the video
     */
    @SerializedName("dislike_count")
    val dislike_count: Long? = null,

    /**
     * the average video like/dislike rating.
     * range seems to be 0-5
     */
    @SerializedName("average_rating")
    val average_rating: Double? = null
) {
    /**
     * get the track title. tries the following fields (in that order):
     * - [track]
     * - [alt_title]
     * - [title]
     *
     * @return the track title
     */
    fun getTrackTitle(): String? {
        if (!track.isNullOrBlank()) return track
        if (!alt_title.isNullOrBlank()) return alt_title
        if (!title.isNullOrBlank()) return title
        return null
    }

    /**
     * get the name of the primary song artist. tries the following fields (in that order):
     * - [artist] (first entry)
     * - [creator] (first entry)
     * - [channel]
     *
     * @return the artist name
     */
    fun getArtistName(): String? {
        // try to use artist OR creator
        val artistList = if (artist.isNullOrBlank()) artist else creator
        if (!artistList.isNullOrBlank()) {
            // if artistList is a list, only take the first artist
            val firstComma = artistList.indexOf(',')
            return if (firstComma > 0) artistList.substring(0, firstComma) else artistList
        }

        // fallback to channel
        return channel
    }

    /**
     * parse the [upload_date] into a normal format
     *
     * @return the parsed date
     */
    fun getUploadDate(): LocalDate? {
        return if (upload_date.isNullOrBlank()) {
            // no value for the field
            null
        } else try {
            // parse
            val format = DateTimeFormatter.ofPattern("yyyyMMdd")
            LocalDate.parse(upload_date, format)
        } catch (ignored: DateTimeParseException) {
            // parse failed
            null
        }
    }
}