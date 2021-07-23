package io.github.shadow578.music_dl.util.storage;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * helper class for storage framework
 */
@SuppressWarnings("unused")
public class StorageHelper {
    // region URI encode / decode

    /**
     * encode a uri to a string for storage in a database, preferences, ...
     * the string can be converted back to a uri using {@link #decodeUri(StorageKey)}
     *
     * @param uri the uri to encode
     * @return the encoded uri key
     */
    @NonNull
    public static StorageKey encodeUri(@NonNull Uri uri) {
        // get file uri
        String encodedUri = uri.toString();

        // encode the uri
        encodedUri = Uri.encode(encodedUri);

        // base- 64 encode to ensure android does not cry about leaked paths or something...
        return new StorageKey(Base64.encodeToString(encodedUri.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT));
    }

    /**
     * decode a string to a uri
     * this function will only decode uris encoded with {@link #encodeUri(Uri)}
     *
     * @param key the encoded uri key
     * @return the decoded uri
     */
    @NonNull
    public static Optional<Uri> decodeUri(@NonNull StorageKey key) {
        try {
            // base- 64 decode
            String uriString = new String(Base64.decode(key.toString(), Base64.DEFAULT), StandardCharsets.UTF_8);

            // decode uri
            uriString = Uri.decode(uriString);

            // check if empty
            if (uriString == null || uriString.isEmpty()) {
                return Optional.empty();
            }

            // parse uri
            return Optional.of(Uri.parse(uriString));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
    //endregion

    //region DocumentFile encode / decode

    /**
     * encode a file to a string for storage in a database, preferences, ...
     * the string can be converted back to a file using {@link #decodeFile(Context, StorageKey)}
     *
     * @param file the file to encode
     * @return the encoded file key
     */
    @NonNull
    public static StorageKey encodeFile(@NonNull DocumentFile file) {
        return encodeUri(file.getUri());
    }

    /**
     * decode a string to a file
     * this function will only decode files encoded with {@link #encodeFile(DocumentFile)}
     *
     * @param ctx the context to create the file in
     * @param key the encoded file key
     * @return the decoded file
     */
    @NonNull
    public static Optional<DocumentFile> decodeFile(@NonNull Context ctx, @NonNull StorageKey key) {
        // decode uri
        final Optional<Uri> uri = decodeUri(key);
        return uri.map(value -> DocumentFile.fromSingleUri(ctx, value));

        // get file
    }
    //endregion

    // region storage framework wrapper

    /**
     * persist a file permission. see {@link android.content.ContentResolver#takePersistableUriPermission(Uri, int)}.
     * uses flags {@code Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION}
     *
     * @param ctx  the context to persist the permission in
     * @param file the file to take permission of
     * @return the key for this file. can be read back using {@link #getPersistedFilePermission(Context, StorageKey, boolean)}
     */
    @NonNull
    public static StorageKey persistFilePermission(@NonNull Context ctx, @NonNull DocumentFile file) {
        return persistFilePermission(ctx, file.getUri());
    }

    /**
     * persist a file permission. see {@link android.content.ContentResolver#takePersistableUriPermission(Uri, int)}.
     * uses flags {@code Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION}
     *
     * @param ctx the context to persist the permission in
     * @param uri the uri to take permission of
     * @return the key for this uri. can be read back using {@link #getPersistedFilePermission(Context, StorageKey, boolean)}
     */
    @NonNull
    public static StorageKey persistFilePermission(@NonNull Context ctx, @NonNull Uri uri) {
        ctx.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return encodeUri(uri);
    }

    /**
     * find a persisted file with a given key
     *
     * @param ctx       the context to check in
     * @param key       the key of the file to find
     * @param mustExist must the file exist?
     * @return the file found
     */
    @NonNull
    public static Optional<DocumentFile> getPersistedFilePermission(@NonNull Context ctx, @NonNull StorageKey key, boolean mustExist) {
        // decode the uri from key
        final Optional<Uri> targetUri = decodeUri(key);
        //noinspection OptionalIsPresent
        if (!targetUri.isPresent()) {
            return Optional.empty();
        }

        // find the persistent uri with that key
        return ctx.getContentResolver().getPersistedUriPermissions()
                .stream()
                .filter(UriPermission::isWritePermission)
                .filter(uri -> uri.getUri().equals(targetUri.get()))
                .map(uri -> DocumentFile.fromTreeUri(ctx, uri.getUri()))
                .filter(doc -> doc != null && doc.canRead() && doc.canWrite())
                .filter(doc -> !mustExist || doc.exists())
                .findFirst();
    }

    //endregion

}
