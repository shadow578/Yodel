package io.github.shadow578.music_dl.downloader.wrapper;

import com.yausername.youtubedl_android.YoutubeDLRequest;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

/**
 * test for {@link YoutubeDLWrapper} parameter creation
 */
public class YoutubeDLWrapperTest {

    /**
     * test parameter list resulting from calls to wrapper function
     */
    @Test
    public void shouldBuildParameterList() {
        // create session with some parameters
        final String videoUrl = "aaBBccDD";
        final File targetFile = new File("/tmp/download/test.mp3");
        final File cacheDir = new File("/tmp/download/cache/");

        final YoutubeDLWrapper session = YoutubeDLWrapper.create(videoUrl)
                .overwriteExisting()
                .fixSsl()
                .audioAndVideo()
                .writeMetadata()
                .writeThumbnail()
                .output(targetFile)
                .cacheDir(cacheDir);

        // check internal request has correct parameters
        final YoutubeDLRequest request = session.getRequest();
        assertThat(request, notNullValue(YoutubeDLRequest.class));

        List<String> args = request.buildCommand();
        assertThat(args, hasItem("--no-continue"));
        assertThat(args, hasItems("--no-check-certificate", "--prefer-insecure"));
        assertThat(args, hasItems("-f", "best"));
        assertThat(args, hasItem("--write-info-json"));
        assertThat(args, hasItem("--write-thumbnail"));
        assertThat(args, hasItems("-o", targetFile.getAbsolutePath()));
        assertThat(args, hasItems("--cache-dir", cacheDir.getAbsolutePath()));
        assertThat(args, hasItem(videoUrl));

        // audio only
        session.audioOnly("mp3");
        args = session.getRequest().buildCommand();
        assertThat(args, hasItems("-f", "bestaudio"));
        assertThat(args, hasItems("--extract-audio"));
        assertThat(args, hasItems("--audio-quality", "0"));
        assertThat(args, hasItems("--audio-format", "mp3"));


        // video only
        session.videoOnly();
        args = session.getRequest().buildCommand();
        assertThat(args, hasItems("-f", "bestvideo"));

        // custom option
        session.setOption("--foo", "bar")
                .setOption("--yee", null);
        args = session.getRequest().buildCommand();
        assertThat(args, hasItems("--foo", "bar", "--yee"));
    }
}
