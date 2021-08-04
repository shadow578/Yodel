package io.github.shadow578.yodel.util.storage

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsNot.not
import org.hamcrest.core.IsNull
import org.junit.Test
import java.io.File

/**
 * instrumented test for StorageHelper
 */
@SmallTest
class StorageHelperAndroidTest {
    /**
     * [decodeToFile] and [decodeToUri]
     */
    @Test
    fun shouldEncodeAndDecodeUri() {
        val uri = Uri.fromFile(
            File(
                InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
                "test.bar"
            )
        )

        // encode
        val key: StorageKey = uri.encodeToKey()
        assertThat(
            key, IsNull.notNullValue(
                StorageKey::class.java
            )
        )

        // decode
        assertThat(
            key.decodeToUri(),
            equalTo(uri)
        )
    }

    /**
     * [decodeToUri] with invalid key
     */
    @Test
    fun shouldNotDecodeUri() {
        assertThat(StorageKey.EMPTY.decodeToUri(), equalTo(null))
    }

    /**
     * [encodeToKey] and [decodeToFile]
     */
    @Test
    fun shouldEncodeAndDecodeFile() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = Uri.fromFile(File(ctx.cacheDir, "test.bar"))
        val file = DocumentFile.fromSingleUri(ctx, uri)

        // check test setup
        assertThat(file, IsNull.notNullValue())

        // encode
        val key: StorageKey = file!!.encodeToKey()
        assertThat(
            key, IsNull.notNullValue(
                StorageKey::class.java
            )
        )

        // decode
        val decodedFile: DocumentFile? = key.decodeToFile(ctx)
        assertThat(decodedFile, not(equalTo(null)))
        assertThat(decodedFile!!.uri, equalTo(file.uri))
    }

    /**
     * [decodeToFile] with invalid key
     */
    @Test
    fun shouldNotDecodeFile() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext

        //empty key
        assertThat(StorageKey.EMPTY.decodeToFile(ctx), equalTo(null))
    }
}