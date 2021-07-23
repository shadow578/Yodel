package io.github.shadow578.music_dl.util.storage;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
    public void shouldNotDecodeUri() {
        assertThat(StorageHelper.decodeUri(StorageKey.EMPTY), isEmpty());
    }

    /**
     * {@link StorageHelper#encodeFile(DocumentFile)} and {@link StorageHelper#decodeFile(Context, StorageKey)}
     */
    @Test
    public void shouldEncodeAndDecodeFile() {
        final Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final Uri uri = Uri.fromFile(new File(ctx.getCacheDir(), "test.bar"));
        final DocumentFile file = DocumentFile.fromSingleUri(ctx, uri);

        // check test setup
        assertThat(file, notNullValue());

        // encode
        final StorageKey key = StorageHelper.encodeFile(file);
        assertThat(key, notNullValue(StorageKey.class));

        // decode
        final Optional<DocumentFile> decodedFile = StorageHelper.decodeFile(ctx, key);
        assertThat(decodedFile.isPresent(), is(true));
        assertThat(decodedFile.get().getUri(), equalTo(file.getUri()));
    }

    /**
     * {@link StorageHelper#decodeFile(Context, StorageKey)} with invalid key
     */
    @Test
    public void shouldNotDecodeFile() {
        final Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        //empty key
        assertThat(StorageHelper.decodeFile(ctx, StorageKey.EMPTY), isEmpty());
    }
}