package io.github.shadow578.music_dl.util.storage;

import android.net.Uri;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * test for {@link StorageHelper}
 * TODO: StorageHelper depends on android libraries, maybe run these tests in a instrumentation test is possible?
 */
public class StorageHelperTest {

    /**
     * {@link StorageHelper#encodeUri(Uri)} and {@link StorageHelper#decodeUri(StorageKey)}
     */
    //@Test
    public void shouldEncodeAndDecodeUri() {
        final Uri uri = Uri.parse("file://data/user/0/io.gihub.shadow578.yt_dl/cache/test.mp3");

        // encode
        final StorageKey key = StorageHelper.encodeUri(uri);
        assertThat(key, notNullValue(StorageKey.class));

        // decode
        assertThat(StorageHelper.decodeUri(key), isPresentAnd(equalTo(uri)));
    }

    /**
     * {@link StorageHelper#decodeUri(StorageKey)} with invalid key
     */
    //@Test
    public void shouldNotDecode() {
        // empty key
        assertThat(StorageHelper.decodeUri(StorageKey.EMPTY), isEmpty());

        // invalid key
        assertThat(StorageHelper.decodeUri(new StorageKey("foobar")), isEmpty());
    }
}
