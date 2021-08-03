package io.github.shadow578.yodel.util.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import io.github.shadow578.yodel.util.unwrap
import java.nio.charset.StandardCharsets
import java.util.*

// region URI encode / decode
/**
 * encode a uri to a key for storage in a database, preferences, ...
 * the string can be converted back to a uri using [StorageKey.decodeToUri]
 *
 * @return the encoded uri key
 */
fun Uri.encodeToKey(): StorageKey {
    // get file uri
    var encodedUri = this.toString()

    // encode the uri
    encodedUri = Uri.encode(encodedUri)

    // base- 64 encode to ensure android does not cry about leaked paths or something...
    return StorageKey(
        Base64.encodeToString(
            encodedUri.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    )
}

/**
 * decode a key to a uri
 * this function will only decode uris encoded with [Uri.encodeToKey]
 *
 * @return the decoded uri
 */
fun StorageKey.decodeToUri(): Uri? {
    return try {
        // base- 64 decode
        var uriString: String? = String(
            Base64.decode(this.key, Base64.NO_WRAP or Base64.URL_SAFE),
            StandardCharsets.UTF_8
        )

        // decode uri
        uriString = Uri.decode(uriString)

        // pare uri
        if (uriString == null || uriString.isEmpty()) null else Uri.parse(uriString)
    } catch (e: IllegalArgumentException) {
        Log.e("StorageHelper", "failed to decode key ${this.key}", e)
        null
    }
}
//endregion

//region DocumentFile encode / decode
/**
 * encode a file to a key for storage in a database, preferences, ...
 * the string can be converted back to a file using [StorageKey.decodeToFile]
 *
 * @return the encoded file key
 */
fun DocumentFile.encodeToKey(): StorageKey = this.uri.encodeToKey()

/**
 * decode a key to a file
 * this function will only decode files encoded with [DocumentFile.encodeToKey]
 *
 * @param ctx the context to create the file in
 * @return the decoded file
 */
fun StorageKey.decodeToFile(ctx: Context): DocumentFile? {
    val uri = this.decodeToUri()
    return if (uri == null) null else DocumentFile.fromSingleUri(ctx, uri)
}
//endregion

// region storage framework wrapper
/**
 * persist a file permission. see [android.content.ContentResolver.takePersistableUriPermission].
 * uses flags `Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION`
 *
 * @param ctx  the context to persist the permission in
 * @return the key for this file. can be read back using [.getPersistedFilePermission]
 */
fun DocumentFile.persistFilePermission(ctx: Context): StorageKey =
    this.uri.persistFilePermission(ctx)

/**
 * persist a file permission. see [android.content.ContentResolver.takePersistableUriPermission].
 * uses flags `Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION`
 *
 * @param ctx the context to persist the permission in
 * @param uri the uri to take permission of
 * @return the key for this uri. can be read back using [.getPersistedFilePermission]
 */
fun Uri.persistFilePermission(ctx: Context): StorageKey {
    ctx.contentResolver.takePersistableUriPermission(
        this,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
    return this.encodeToKey()
}

/**
 * find a persisted file with a given key
 *
 * @param ctx       the context to check in
 * @param mustExist must the file exist?
 * @return the file found
 */
fun StorageKey.getPersistedFilePermission(
    ctx: Context,
    mustExist: Boolean
): DocumentFile? {
    // decode storage key
    val targetUri = this.decodeToUri()

    // find the first persisted permission that matches the search
    return if (targetUri == null) null else ctx.contentResolver.persistedUriPermissions
        .stream()
        .filter { it.isWritePermission }
        .filter { it.uri == targetUri }
        .map { DocumentFile.fromTreeUri(ctx, it.uri) }
        .filter { it != null && it.canRead() && it.canWrite() }
        .filter { !mustExist || it!!.exists() }
        .findFirst()
        .unwrap()
}

//endregion