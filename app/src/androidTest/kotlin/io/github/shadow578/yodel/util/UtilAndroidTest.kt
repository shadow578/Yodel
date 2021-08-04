package io.github.shadow578.yodel.util

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.util.Util
import io.kotest.assertions.withClue
import io.kotest.matchers.file.*
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.Test
import java.io.File

/**
 * instrumented test for [Util]
 */
@SmallTest
class UtilAndroidTest {
    /**
     * [getTempFile]
     */
    @Test
    fun shouldGetTempFile() {
        withClue("getTempFile() should return a file in the parent directory that does not exist") {
            val cache = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
            val temp: File = cache.getTempFile("foo", "bar")

            temp.shouldNotBeNull()
            temp.shouldNotExist()
            temp.shouldStartWithPath(cache)
        }
    }
}