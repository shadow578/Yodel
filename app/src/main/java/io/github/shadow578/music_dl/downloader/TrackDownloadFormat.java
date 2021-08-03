package io.github.shadow578.music_dl.downloader;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.R;

/**
 * file formats for track download
 * <p>
 * TODO validate all formats actually work
 * TODO check if more formats support ID3
 */
@KtPorted
public enum TrackDownloadFormat {

    /**
     * mp3 (with metadata in id3 tags)
     */
    MP3("audio/mp3", "mp3", true, R.string.file_format_mp3),


    /**
     * aac
     */
    AAC("audio/aac", "aac", false, R.string.file_format_aac),

    /**
     * webm audio
     */
    WEBM("audio/weba", "weba", false, R.string.file_format_webm),

    /**
     * ogg
     */
    OGG("audio/ogg", "ogg", false, R.string.file_format_ogg),

    /**
     * flac
     */
    FLAC("audio/flac", "flac", false, R.string.file_format_flac),

    /**
     * wav
     */
    WAV("audio/wav", "wav", false, R.string.file_format_wav);

    /**
     * audio format mime type
     */
    @NonNull
    private final String mimeType;

    /**
     * the file extension used by downloader
     */
    @NonNull
    private final String fileExtension;

    /**
     * does the file format support id3 tags (with mp3agic)?
     */
    private final boolean isID3Supported;

    /**
     * the display name string resource
     */
    @StringRes
    private final int displayNameRes;

    /**
     * create a new track format
     *
     * @param mimeType       audio format mime type
     * @param fileExtension  the file extension used by downloader
     * @param isID3Supported does the file format support id3 tags (with mp3agic)?
     * @param displayNameRes the display name string resource
     */
    TrackDownloadFormat(@NonNull String mimeType, @NonNull String fileExtension, boolean isID3Supported, @StringRes int displayNameRes) {
        this.displayNameRes = displayNameRes;
        this.fileExtension = fileExtension;
        this.isID3Supported = isID3Supported;
        this.mimeType = mimeType;
    }

    /**
     * @return the display name string resource
     */
    @StringRes
    public int displayNameRes() {
        return displayNameRes;
    }

    /**
     * @return the file extension used by downloader
     */
    @NonNull
    public String fileExtension() {
        return fileExtension;
    }

    /**
     * @return does the file format support id3 tags (with mp3agic)?
     */
    public boolean isID3Supported() {
        return isID3Supported;
    }

    /**
     * @return audio format mime type
     */
    public String mimeType() {
        return mimeType;
    }
}
