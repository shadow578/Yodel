package io.github.shadow578.yodel.util.storage

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.test.platform.app.InstrumentationRegistry
import io.github.shadow578.yodel.RoboTest
import io.kotest.matchers.*
import io.kotest.matchers.nulls.*
import org.junit.Test
import java.io.File

/**
 * robolectric test for StorageHelper
 */
class StorageHelperRoboTest : RoboTest() {
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
        val cache = context.cacheDir
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
        val uri = Uri.fromFile(File(context.cacheDir, "foo.bar"))
        val file = DocumentFile.fromSingleUri(context, uri)

        // check test setup
        file.shouldNotBeNull()

        // encode
        val key: StorageKey = file.encodeToKey()
        key should beNonEmptyStorageKey()

        // decode
        val decodedFile: DocumentFile? = key.decodeToFile(context)
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