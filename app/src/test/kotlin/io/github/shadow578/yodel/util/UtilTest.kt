package io.github.shadow578.yodel.util

import com.bumptech.glide.util.Util
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.*
import org.junit.Test

/**
 * test for [Util]
 */
class UtilTest {
    /**
     * [extractTrackId]
     */
    @Test
    fun shouldExtractVideoId() {
        withClue("extractTrackId() for full youtube URL")
        {
            extractTrackId("https://www.youtube.com/watch?v=6Xs26b4RSu4") shouldBe "6Xs26b4RSu4"
        }

        withClue("extractTrackId() for short youtube URL (https)")
        {
            extractTrackId("https://youtu.be/6Xs26b4RSu4") shouldBe "6Xs26b4RSu4"
        }

        withClue("extractTrackId() for short youtube URL (http)")
        {
            extractTrackId("http://youtu.be/6Xs26b4RSu4") shouldBe "6Xs26b4RSu4"
        }

        withClue("extractTrackId() for short youtube URL (no protocol)")
        {
            extractTrackId("youtu.be/6Xs26b4RSu4") shouldBe "6Xs26b4RSu4"
        }

        withClue("extractTrackId() for youtube URL with playlist")
        {
            extractTrackId("https://www.youtube.com/watch?v=6Xs26b4RSu4&list=RD6Xs26b4RSu4&start_radio=1&rv=6Xs26b4RSu4&t=0") shouldBe "6Xs26b4RSu4"
        }

        withClue("extractTrackId() for youtube music URL")
        {
            extractTrackId("https://music.youtube.com/watch?v=wbJwhx29O5U&list=RDAMVMwbJwhx29O5U") shouldBe "wbJwhx29O5U"
        }

        withClue("extractTrackId() for URL from share dialog of youtube music app")
        {
            extractTrackId("https://music.youtube.com/watch?v=wbJwhx29O5U&feature=share") shouldBe "wbJwhx29O5U"
        }

        withClue("extractTrackId() for invalid URL")
        {
            extractTrackId("foobar").shouldBeNull()
        }
    }

    /**
     * [generateRandomAlphaNumeric]
     */
    @Test
    fun shouldGenerateRandomString() {
        withClue("generateRandomAlphaNumeric(128) generates a string with 128 chars")
        {
            val random = generateRandomAlphaNumeric(128)
            random.shouldNotBeNull()
            random shouldHaveLength 128
            random.shouldNotBeBlank()
        }
    }

    /**
     * [secondsToTimeString]
     */
    @Test
    fun shouldConvertSecondsToString() {
        withClue("secondsToTimeString() should give correct results")
        {
            // < 1h
            620.secondsToTimeString() shouldBe "10:20"
            520.secondsToTimeString() shouldBe "8:40"

            // > 1h
            7300.secondsToTimeString() shouldBe "2:01:40"

            // > 10h
            172800.secondsToTimeString() shouldBe "48:00:00"
        }
    }
}