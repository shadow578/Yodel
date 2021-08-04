package io.github.shadow578.yodel.util.storage

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.*
import io.kotest.matchers.nulls.*
import org.junit.Test
import java.io.File

/**
 * instrumented test for StorageHelper
 */
@SmallTest
class StorageHelperAndroidTest {
    private fun beNonEmptyStorageKey() = object : Matcher<StorageKey> {
        override fun test(value: StorageKey): MatcherResult = MatcherResult(
            value.key.isNotBlank(),
            "$value should be a non-empty storage key",
            "$value should be a empty storage key"
        )
    }

    /**
     * [decodeToFile] and [decodeToUri]
     */
    @Test
    fun shouldEncodeAndDecodeUri() {
        val cache = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val uri = Uri.fromFile(File(cache, "foo.bar"))

        // encode
        val key: StorageKey = uri.encodeToKey()
        key should beNonEmptyStorageKey()

        // decode
        key.decodeToUri() shouldBe uri
    }

    /**
     * [decodeToUri] with invalid key
     */
    @Test
    fun shouldNotDecodeUri() {
        StorageKey.EMPTY.decodeToUri().shouldBeNull()
    }

    /**
     * [encodeToKey] and [decodeToFile]
     */
    @Test
    fun shouldEncodeAndDecodeFile() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = Uri.fromFile(File(ctx.cacheDir, "foo.bar"))
        val file = DocumentFile.fromSingleUri(ctx, uri)

        // check test setup
        file.shouldNotBeNull()

        // encode
        val key: StorageKey = file.encodeToKey()
        key should beNonEmptyStorageKey()

        // decode
        val decodedFile: DocumentFile? = key.decodeToFile(ctx)
        decodedFile.shouldNotBeNull()
        decodedFile.uri shouldBe file.uri
    }

    /**
     * [decodeToFile] with invalid key
     */
    @Test
    fun shouldNotDecodeFile() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext

        //empty key
        StorageKey.EMPTY.decodeToFile(ctx).shouldBeNull()
    }
}