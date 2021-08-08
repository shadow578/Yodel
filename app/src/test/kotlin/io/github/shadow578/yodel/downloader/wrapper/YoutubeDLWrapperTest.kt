package io.github.shadow578.yodel.downloader.wrapper

import com.yausername.youtubedl_android.YoutubeDLRequest
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
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
        request.shouldNotBeNull()

        // technically, this could be done with only one shouldContainAll
        // but this makes it more structured (every shouldContainAll == a parameter with (optional) value)
        with(request.buildCommand())
        {
            shouldContainAll("--no-continue")
            shouldContainAll("--no-check-certificate", "--prefer-insecure")
            shouldContainAll("-f", "best")
            shouldContainAll("--write-info-json")
            shouldContainAll("--write-thumbnail")
            shouldContainAll("-o", targetFile.absolutePath)
            shouldContainAll("--cache-dir", cacheDir.absolutePath)
            shouldContainAll(videoUrl)
        }

        // audio only
        session.audioOnly("mp3")
        with(session.request.buildCommand())
        {
            shouldContainAll("-f", "bestaudio")
            shouldContainAll("--extract-audio")
            shouldContainAll("--audio-quality", "0")
            shouldContainAll("--audio-format", "mp3")
        }

        // video only
        session.videoOnly()
        with(session.request.buildCommand())
        {
            shouldContainAll("-f", "bestvideo")
        }

        // custom option
        session.setOption("--foo", "bar")
            .setOption("--yee", null)
        with(session.request.buildCommand())
        {
            shouldContainAll("--foo", "bar")
            shouldContainAll("--yee")
        }
    }
}
