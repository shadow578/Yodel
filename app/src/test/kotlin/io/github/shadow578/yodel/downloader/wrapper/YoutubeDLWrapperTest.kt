package io.github.shadow578.yodel.downloader.wrapper

import com.yausername.youtubedl_android.YoutubeDLRequest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import java.io.File

/**
 * test for [YoutubeDLWrapper] parameter creation
 */
class YoutubeDLWrapperTest {
    /**
     * test parameter list resulting from calls to wrapper function
     */
    @Test
    fun shouldBuildParameterList() {
        // create session with some parameters
        val videoUrl = "aaBBccDD"
        val targetFile = File("/tmp/download/test.mp3")
        val cacheDir = File("/tmp/download/cache/")
        val session: YoutubeDLWrapper = YoutubeDLWrapper(videoUrl)
            .overwriteExisting()
            .fixSsl()
            .audioAndVideo()
            .writeMetadata()
            .writeThumbnail()
            .output(targetFile)
            .cacheDir(cacheDir)

        // check internal request has correct parameters
        val request: YoutubeDLRequest = session.request
        assertThat(request, notNullValue(YoutubeDLRequest::class.java))
        var args = request.buildCommand()
        assertThat(args, hasItem("--no-continue"))
        assertThat(
            args,
            hasItems("--no-check-certificate", "--prefer-insecure")
        )
        assertThat(args, hasItems("-f", "best"))
        assertThat(args, hasItem("--write-info-json"))
        assertThat(args, hasItem("--write-thumbnail"))
        assertThat(args, hasItems("-o", targetFile.absolutePath))
        assertThat(args, hasItems("--cache-dir", cacheDir.absolutePath))
        assertThat(args, hasItem(videoUrl))

        // audio only
        session.audioOnly("mp3")
        args = session.request.buildCommand()
        assertThat(args, hasItems("-f", "bestaudio"))
        assertThat(args, hasItems("--extract-audio"))
        assertThat(args, hasItems("--audio-quality", "0"))
        assertThat(args, hasItems("--audio-format", "mp3"))


        // video only
        session.videoOnly()
        args = session.request.buildCommand()
        assertThat(args, hasItems("-f", "bestvideo"))

        // custom option
        session.setOption("--foo", "bar")
            .setOption("--yee", null)
        args = session.request.buildCommand()
        assertThat(args, hasItems("--foo", "bar", "--yee"))
    }
}