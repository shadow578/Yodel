package io.github.shadow578.music_dl.downloader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import io.github.shadow578.music_dl.KtPorted;

/**
 * track metadata POJO. this is in the format that youtube-dl writes with the --write-info-json option
 * a lot of data is left out, as it's not really relevant for what we're doing (stuff like track info, thumbnails, ...)
 */
@SuppressWarnings("unused")
@KtPorted
public class TrackMetadata {

    /**
     * the full video title. for music videos, this often is in the format 'Artist - Song Title'
     */
    @SerializedName("title")
    @Nullable
    public String title;

    /**
     * the alternative video title. for music videos, this often is just the 'Song Title' (in contrast to {@link #title}).
     * seems to be the same value as {@link #track}
     */
    @SerializedName("alt_title")
    @Nullable
    public String alt_title;

    /**
     * the upload date, in the format yyyyMMdd (that is without ANY spaces: 20200924 == 2020-09-24)
     */
    @SerializedName("upload_date")
    @Nullable
    public String upload_date;

    /**
     * the display name of the channel that uploaded the video
     */
    @SerializedName("channel")
    @Nullable
    public String channel;

    /**
     * the duration of the video, in seconds
     */
    @SerializedName("duration")
    @Nullable
    public Long duration;

    /**
     * the title of the track. this seems to be the same as {@link #alt_title}
     */
    @SerializedName("track")
    @Nullable
    public String track;

    /**
     * the name of the actual song creator (not uploader channel).
     * This seems to be data from Content-ID
     */
    @SerializedName("creator")
    @Nullable
    public String creator;

    /**
     * the name of the actual song artist (not uploader channel).
     * This seems to be data from Content-ID
     */
    @SerializedName("artist")
    @Nullable
    public String artist;

    /**
     * the display name of the album this track is from.
     * only included for songs that are part of a album
     */
    @SerializedName("album")
    @Nullable
    public String album;

    /**
     * categories of the video (like 'Music', 'Entertainment', 'Gaming' ...)
     */
    @SerializedName("categories")
    @Nullable
    public List<String> categories;

    /**
     * tags on the video
     */
    @SerializedName("tags")
    @Nullable
    public List<String> tags;

    /**
     * total view count of the video
     */
    @SerializedName("view_count")
    @Nullable
    public Long view_count;

    /**
     * total likes on the video
     */
    @SerializedName("like_count")
    @Nullable
    public Long like_count;

    /**
     * total dislikes on the video
     */
    @SerializedName("dislike_count")
    @Nullable
    public Long dislike_count;

    /**
     * the average video like/dislike rating.
     * range seems to be 0-5
     */
    @SerializedName("average_rating")
    @Nullable
    public Double average_rating;

    /**
     * get the track title. tries the following fields (in that order):
     * - {@link #track}
     * - {@link #alt_title}
     * - {@link #title}
     *
     * @return the track title
     */
    @NonNull
    public Optional<String> getTrackTitle() {
        if (notNullOrEmpty(track)) {
            return Optional.of(track);
        }

        if (notNullOrEmpty(alt_title)) {
            return Optional.of(alt_title);
        }

        if (notNullOrEmpty(title)) {
            return Optional.of(title);
        }

        return Optional.empty();
    }

    /**
     * get the name of the primary song artist. tries the following fields (in that order):
     * - {@link #artist} (first entry)
     * - {@link #creator} (first entry)
     * - {@link #channel}
     *
     * @return the artist name
     */
    @NonNull
    public Optional<String> getArtistName() {
        String artistList = artist;
        if(!notNullOrEmpty(artistList))
        {
            artistList = creator;
        }

        if(notNullOrEmpty(artistList))
        {
            // check if comma- delimited list
            final int firstComma = artistList.indexOf(',');
            if(firstComma > 0)
            {
                // is a list, only take first artist
                artistList = artistList.substring(0, firstComma);
            }

            return Optional.of(artistList);
        }


        if (notNullOrEmpty(channel)) {
            return Optional.of(channel);
        }

        return Optional.empty();
    }

    /**
     * check a string is not null and not empty
     *
     * @param string the string to check
     * @return is the string not null or empty?
     */
    private boolean notNullOrEmpty(@Nullable String string) {
        return string != null && !string.trim().isEmpty();
    }

    /**
     * parse the {@link #upload_date} into a normal format
     *
     * @return the parsed date
     */
    @NonNull
    public Optional<LocalDate> getUploadDate() {
        // check if field is set
        if (upload_date == null || upload_date.isEmpty()) {
            return Optional.empty();
        }

        // try to parse
        try {
            final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
            return Optional.of(LocalDate.parse(upload_date, format));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }
}
