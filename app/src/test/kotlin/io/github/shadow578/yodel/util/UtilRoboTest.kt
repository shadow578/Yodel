package io.github.shadow578.yodel.util

import com.bumptech.glide.util.Util
import io.github.shadow578.yodel.RoboTest
import io.kotest.assertions.withClue
import io.kotest.matchers.file.*
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.Test
import java.io.File
/**
 * robolectric test for [Util]
 */
class UtilRoboTest : RoboTest() {

    /**
     * [getTempFile]
     */
    @Test
    fun shouldGetTempFile() {
        withClue("getTempFile() should return a file in the parent directory that does not exist") {
            val cache = context.cacheDir
            val temp: File = cache.getTempFile("foo", "bar")

            temp.shouldNotBeNull()
            temp.shouldNotExist()
            temp.shouldStartWithPath(cache)
        }
    }
}