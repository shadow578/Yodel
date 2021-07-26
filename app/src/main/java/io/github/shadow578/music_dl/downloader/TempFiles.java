package io.github.shadow578.music_dl.downloader;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Optional;

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
     * the main audio file downloaded by youtube-dl.
     * this file will be the same as {@link #convertedAudio}, but with a .tmp extension
     */
    @NonNull
    private final File downloadedAudio;

    /**
     * the converted audio file, created by ffmpeg with the --extract-audio option
     */
    @NonNull
    private final File convertedAudio;

    /**
     * create a new collection of temp files
     *
     * @param tempFile the base file. this file is not used directly
     * @param format   the format of the output file
     */
    public TempFiles(@NonNull File tempFile, @NonNull String format) {
        convertedAudio = new File(tempFile.getAbsolutePath() + "." + format);
        downloadedAudio = new File(tempFile.getAbsolutePath() + ".tmp");
    }

    /**
     * delete all files
     *
     * @return did all deletes succeed?
     */
    public boolean delete() {
        final Optional<File> thumbnail = getThumbnail();
        boolean thumbnailDeleted = true;
        if (thumbnail.isPresent()) {
            thumbnailDeleted = thumbnail.get().delete();
        }

        return maybeDelete(downloadedAudio)
                & maybeDelete(convertedAudio)
                & maybeDelete(getMetadataJson())
                & thumbnailDeleted;
    }

    /**
     * delete the file if it still exists
     *
     * @param file the file to delete
     * @return does the file no longer exist?
     */
    private boolean maybeDelete(@NonNull File file) {
        return !file.exists() || file.delete();
    }

    /**
     * get the audio file. first tries to get {@link #convertedAudio}, if that does not exist gets {@link #downloadedAudio}
     *
     * @return the audio file
     */
    @NonNull
    public File getAudio() {
        if (convertedAudio.exists()) {
            return convertedAudio;
        }

        return downloadedAudio;
    }

    /**
     * @return the metadata json downloaded by youtube-dl
     */
    @NonNull
    public File getMetadataJson() {
        return new File(downloadedAudio.getAbsolutePath() + METADATA_FILE_SUFFIX);
    }

    /**
     * @return the thumbnail downloaded by youtube-dl, webp format
     */
    @NonNull
    public Optional<File> getThumbnail() {
        // check all suffixes, use the first that exists
        // youtube-dl downloads the thumbnail for us, but does not tell us the file name / type (with no way to tell it what to use :|)
        for (String suffix : THUMBNAIL_FILE_SUFFIXES) {
            File thumbnailFile = new File(downloadedAudio.getAbsolutePath() + suffix);
            if (thumbnailFile.exists()) {
                return Optional.of(thumbnailFile);
            }
        }

        return Optional.empty();
    }
}
