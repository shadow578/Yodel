package io.github.shadow578.music_dl.util;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * instrumented test for {@link Util}
 */
public class UtilTest {

    /**
     * {@link Util#getTempFile(String, String, File)}
     */
    @Test
    public void shouldGetTempFile() {
        final File temp = Util.getTempFile("foo", "bar", InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir());
        assertThat(temp, notNullValue());
        assertThat(temp.exists(), is(false));
    }
}
