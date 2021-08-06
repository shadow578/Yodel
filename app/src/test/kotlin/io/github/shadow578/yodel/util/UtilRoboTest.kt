package io.github.shadow578.yodel.util

import com.bumptech.glide.util.Util
import io.github.shadow578.yodel.*
import io.github.shadow578.yodel.util.preferences.Prefs
import io.kotest.assertions.withClue
import io.kotest.matchers.file.*
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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

    /**
     *  [wrapLocale]
     */
    @Test
    fun testWrapLocale(){
        Prefs.AppLocaleOverride.set(LocaleOverride.German)
        Prefs.AppLocaleOverride.get() shouldBe LocaleOverride.German

        val wrapped = context.wrapLocale()
        wrapped.shouldNotBeNull()
        wrapped.resources.configuration.locales.get(0) shouldBe LocaleOverride.German.locale
    }
}