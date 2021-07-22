package io.github.shadow578.music_dl.downloader;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * temporary files created by youtube-dl
 */
public class TempFiles {

    /**
     * suffix for the metadata file
     */
    private static final String METADATA_FILE_SUFFIX = ".info.json";

    /**
     * suffixes (file types) for the thumbnail file
     */
    private static final String[] THUMBNAIL_FILE_SUFFIXES = {".webp", ".webm", ".jpg", ".jpeg", ".png"};

    /**
     * the main audio file downloaded by youtube-dl
     */
    @NonNull
    private final File audio;

    public TempFiles(@NonNull File tempAudioFile) {
        audio = tempAudioFile;
    }

    /**
     * delete all files
     *
     * @return did all deletes succeed?
     */
    public boolean delete() {
        return getAudio().delete() & getMetadataJson().delete() & getThumbnail().delete();
    }

    /**
     * @return the main audio file downloaded by youtube-dl
     */
    @NonNull
    public File getAudio() {
        return audio;
    }

    /**
     * @return the metadata json downloaded by youtube-dl
     */
    @NonNull
    public File getMetadataJson() {
        return new File(getAudio().getAbsolutePath() + METADATA_FILE_SUFFIX);
    }

    /**
     * @return the thumbnail downloaded by youtube-dl, webp format
     */
    @NonNull
    public File getThumbnail() {
        // check all suffixes, use the first that exists
        // youtube-dl downloads the thumbnail for us, but does not tell us the file name / type (with no way to tell it what to use :|)
        File thumbnailFile = null;
        for (String suffix : THUMBNAIL_FILE_SUFFIXES) {
            thumbnailFile = new File(getAudio().getAbsolutePath() + suffix);
            if (thumbnailFile.exists()) {
                break;
            }
        }

        return thumbnailFile;
    }
}
