package io.github.shadow578.music_dl.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * test for {@link Util}
 */
public class UtilTest {

    /**
     * {@link Util#extractTrackId(String)}
     */
    @Test
    public void shouldExtractVideoId() {
        // youtube full
        assertThat(Util.extractTrackId("https://www.youtube.com/watch?v=6Xs26b4RSu4"),
                isPresentAnd(equalToIgnoringCase("6Xs26b4RSu4")));

        // youtube short (https)
        assertThat(Util.extractTrackId("https://youtu.be/6Xs26b4RSu4"),
                isPresentAnd(equalToIgnoringCase("6Xs26b4RSu4")));

        // youtube short (http)
        assertThat(Util.extractTrackId("http://youtu.be/6Xs26b4RSu4"),
                isPresentAnd(equalToIgnoringCase("6Xs26b4RSu4")));

        // youtube short (no protocol)
        assertThat(Util.extractTrackId("youtu.be/6Xs26b4RSu4"),
                isPresentAnd(equalToIgnoringCase("6Xs26b4RSu4")));

        // youtube full with playlist
        assertThat(Util.extractTrackId("https://www.youtube.com/watch?v=6Xs26b4RSu4&list=RD6Xs26b4RSu4&start_radio=1&rv=6Xs26b4RSu4&t=0"),
                isPresentAnd(equalToIgnoringCase("6Xs26b4RSu4")));

        // youtube music
        assertThat(Util.extractTrackId("https://music.youtube.com/watch?v=wbJwhx29O5U&list=RDAMVMwbJwhx29O5U"),
                isPresentAnd(equalToIgnoringCase("wbJwhx29O5U")));

        // youtube music by share dialog
        assertThat(Util.extractTrackId("https://music.youtube.com/watch?v=wbJwhx29O5U&feature=share"),
                isPresentAnd(equalToIgnoringCase("wbJwhx29O5U")));

        // invalid link
        assertThat(Util.extractTrackId("foobar"), isEmpty());
    }

    /**
     * {@link Util#generateRandomAlphaNumeric(int)}
     */
    @Test
    public void shouldGenerateRandomString() {
        final String random = Util.generateRandomAlphaNumeric(128);
        assertThat(random, notNullValue(String.class));
        assertThat(random.length(), is(128));
        assertThat(random.isEmpty(), is(false));
    }

    /**
     * {@link Util#streamTransfer(InputStream, OutputStream, int)}
     */
    @Test
    public void shouldTransferStream() throws IOException {
        final InputStream in = new ByteArrayInputStream("foobar".getBytes());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Util.streamTransfer(in, out, 1024);

        assertThat(out.toString(), equalTo("foobar"));
    }

    /**
     * {@link Util#secondsToTimeString(long)}
     */
    @Test
    public void shouldConvertSecondsToString() {
        // < 1h
        assertThat(Util.secondsToTimeString(620), equalTo("10:20"));
        assertThat(Util.secondsToTimeString(520), equalTo("8:40"));

        // > 1h
        assertThat(Util.secondsToTimeString(7300), equalTo("2:01:40"));

        // > 10d
        assertThat(Util.secondsToTimeString(172800), equalTo("48:00:00"));
    }
}
