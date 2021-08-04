package io.github.shadow578.yodel.util

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.util.Util
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.hamcrest.core.IsNull
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
        val temp: File =
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir.getTempFile(
                "foo",
                "bar"
            )
        MatcherAssert.assertThat(temp, IsNull.notNullValue())
        MatcherAssert.assertThat(temp.exists(), Is.`is`(false))
    }
}