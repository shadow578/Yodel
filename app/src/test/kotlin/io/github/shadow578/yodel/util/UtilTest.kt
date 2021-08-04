package io.github.shadow578.yodel.util

import com.bumptech.glide.util.Util
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase
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
        // youtube full
        assertThat(
            extractTrackId("https://www.youtube.com/watch?v=6Xs26b4RSu4"),
            equalToIgnoringCase("6Xs26b4RSu4")
        )

        // youtube short (https)
        assertThat(
            extractTrackId("https://youtu.be/6Xs26b4RSu4"),
            equalToIgnoringCase("6Xs26b4RSu4")
        )

        // youtube short (http)
        assertThat(
            extractTrackId("http://youtu.be/6Xs26b4RSu4"),
            equalToIgnoringCase("6Xs26b4RSu4")
        )

        // youtube short (no protocol)
        assertThat(
            extractTrackId("youtu.be/6Xs26b4RSu4"),
            equalToIgnoringCase("6Xs26b4RSu4")
        )

        // youtube full with playlist
        assertThat(
            extractTrackId("https://www.youtube.com/watch?v=6Xs26b4RSu4&list=RD6Xs26b4RSu4&start_radio=1&rv=6Xs26b4RSu4&t=0"),
            equalToIgnoringCase("6Xs26b4RSu4")
        )


        // youtube music
        assertThat(
            extractTrackId("https://music.youtube.com/watch?v=wbJwhx29O5U&list=RDAMVMwbJwhx29O5U"),
            equalToIgnoringCase("wbJwhx29O5U")
        )

        // youtube music by share dialog
        assertThat(
            extractTrackId("https://music.youtube.com/watch?v=wbJwhx29O5U&feature=share"),
            equalToIgnoringCase("wbJwhx29O5U")
        )

        // invalid link
        assertThat(extractTrackId("foobar"), equalTo(null))
    }

    /**
     * [Util.generateRandomAlphaNumeric]
     */
    @Test
    fun shouldGenerateRandomString() {
        val random: String = generateRandomAlphaNumeric(128)
        assertThat(
            random, Matchers.notNullValue(
                String::class.java
            )
        )
        assertThat(random.length, Matchers.`is`(128))
        assertThat(random.isEmpty(), Matchers.`is`(false))
    }

    /**
     * [secondsToTimeString]
     */
    @Test
    fun shouldConvertSecondsToString() {
        // < 1h
        assertThat(620.secondsToTimeString(), Matchers.equalTo("10:20"))
        assertThat(520.secondsToTimeString(), Matchers.equalTo("8:40"))

        // > 1h
        assertThat(7300.secondsToTimeString(), Matchers.equalTo("2:01:40"))

        // > 10d
        assertThat(172800.secondsToTimeString(), Matchers.equalTo("48:00:00"))
    }
}