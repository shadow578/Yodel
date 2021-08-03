package io.github.shadow578.music_dl.downloader.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.util.Util;

/**
 * wrapper for MP3agic to make working with it on android easier
 */
@KtPorted
public class MP3agicWrapper {

    /**
     * tag for logging
     */
    private static final String TAG = "MP3agicW";

    /**
     * create a new wrapper instance
     *
     * @param file the mp3 file to read
     * @return the wrapper instance
     * @throws InvalidDataException    thrown by mp3agic, see {@link Mp3File#Mp3File(File, int, boolean)}
     * @throws IOException             thrown by mp3agic, see {@link Mp3File#Mp3File(File, int, boolean)}
     * @throws UnsupportedTagException thrown by mp3agic, see {@link Mp3File#Mp3File(File, int, boolean)}
     */
    @NonNull
    public static MP3agicWrapper create(@NonNull File file) throws InvalidDataException, IOException, UnsupportedTagException {
        return new MP3agicWrapper(file);
    }

    /**
     * the original file passed to the constructor
     */
    @NonNull
    private final File originalFile;

    /**
     * the mp3agic file instance
     */
    @NonNull
    private final Mp3File mp3;

    /**
     * create a new wrapper instance
     *
     * @param file the mp3 file to read
     * @throws InvalidDataException    thrown by mp3agic, see {@link Mp3File#Mp3File(File, int, boolean)}
     * @throws IOException             thrown by mp3agic, see {@link Mp3File#Mp3File(File, int, boolean)}
     * @throws UnsupportedTagException thrown by mp3agic, see {@link Mp3File#Mp3File(File, int, boolean)}
     */
    private MP3agicWrapper(@NonNull File file) throws InvalidDataException, IOException, UnsupportedTagException {
        this.originalFile = file;
        mp3 = new Mp3File(file);
    }

    /**
     * remove all tags that mp3agic supports
     *
     * @return self instance
     */
    public MP3agicWrapper clearAllTags() {
        if (mp3.hasId3v1Tag()) {
            mp3.removeId3v1Tag();
        }
        if (mp3.hasId3v2Tag()) {
            mp3.removeId3v2Tag();
        }
        if (mp3.hasCustomTag()) {
            mp3.removeCustomTag();
        }

        return this;
    }

    /**
     * edit the id3v2 tags on the mp3 file.
     * gets a existing id3v2 tag, or creates a new one if needed
     *
     * @return the id3v2 tag on the mp3
     */
    @NonNull
    public ID3v2 editTag() {
        if (mp3.hasId3v2Tag()) {
            return mp3.getId3v2Tag();
        } else {
            final ID3v24Tag tag = new ID3v24Tag();
            mp3.setId3v2Tag(tag);
            return tag;
        }
    }

    /**
     * save the mp3 file, overwriting the original file
     *
     * @throws IOException           if io operation fails
     * @throws NotSupportedException if mp3agic failes to save the file (see {@link Mp3File#save(String)})
     */
    public void save() throws IOException, NotSupportedException {
        File tagged = null;
        try {
            // create file to write to (original appended with .tagged)
            tagged = new File(originalFile.getAbsolutePath() + ".tagged");

            // save mp3 to tagged file
            mp3.save(tagged.getAbsolutePath());

            // delete original file and move tagged file to its place
            if (!originalFile.delete()) {
                Log.i(TAG, "could not delete original file on save!");
            }
            try (final FileInputStream src = new FileInputStream(tagged.getAbsolutePath());
                 final FileOutputStream out = new FileOutputStream(originalFile.getAbsolutePath(), false)) {
                Util.streamTransfer(src, out, 1024);
            }
        } finally {
            if (tagged != null) {
                if (tagged.exists() && !tagged.delete()) {
                    Log.i(TAG, "failed to delete temporary tagged mp3 file");
                }
            }
        }
    }
}
