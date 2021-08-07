package io.github.shadow578.yodel.util

import com.bumptech.glide.util.Util
import io.github.shadow578.yodel.LocaleOverride
import io.github.shadow578.yodel.RoboTest
import io.github.shadow578.yodel.util.preferences.Prefs
import io.kotest.assertions.withClue
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.file.shouldStartWithPath
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
    fun testWrapLocale() {
        Prefs.AppLocaleOverride.set(LocaleOverride.German)
        Prefs.AppLocaleOverride.get() shouldBe LocaleOverride.German

        val wrapped = context.wrapLocale()
        wrapped.shouldNotBeNull()

        // .locales was only added in SDK 24
        // before that, we have to use .locale (which is now deprecated)
        untilSDK(23) {
            wrapped.resources.configuration.locale shouldBe LocaleOverride.German.locale
        }

        aboveSDK(23) {
            wrapped.resources.configuration.locales.get(0) shouldBe LocaleOverride.German.locale
        }
    }
}