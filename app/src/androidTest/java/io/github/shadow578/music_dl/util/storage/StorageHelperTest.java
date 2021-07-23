package io.github.shadow578.music_dl.util.storage;

import android.net.Uri;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.io.File;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * instrumented test for {@link StorageHelper}
 */
@SmallTest
public class StorageHelperTest {

    /**
     * {@link StorageHelper#encodeUri(Uri)} and {@link StorageHelper#decodeUri(StorageKey)}
     */
    @Test
    public void shouldEncodeAndDecodeUri() {
        final Uri uri = Uri.fromFile(new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(), "test.bar"));

        // encode
        final StorageKey key = StorageHelper.encodeUri(uri);
        assertThat(key, notNullValue(StorageKey.class));

        // decode
        assertThat(StorageHelper.decodeUri(key), isPresentAnd(equalTo(uri)));
    }

    /**
     * {@link StorageHelper#decodeUri(StorageKey)} with invalid key
     */
    @Test
    public void shouldNotDecode() {
        assertThat(StorageHelper.decodeUri(StorageKey.EMPTY), isEmpty());
    }
}
