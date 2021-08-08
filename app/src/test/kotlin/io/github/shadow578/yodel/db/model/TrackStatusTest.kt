package io.github.shadow578.yodel.db.model

import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * [TrackStatus]
 */
class TrackStatusTest {

    @Test
    fun shouldFindValidKeys() {
        withClue("TrackStatus values are identified by a key that is used in SQL queries. the keys have to be resolvable by TrackStatus.findByKey()")
        {
            TrackStatus.findByKey("pending") shouldBe TrackStatus.DownloadPending
            TrackStatus.findByKey("downloading") shouldBe TrackStatus.Downloading
            TrackStatus.findByKey("downloaded") shouldBe TrackStatus.Downloaded
            TrackStatus.findByKey("failed") shouldBe TrackStatus.DownloadFailed
            TrackStatus.findByKey("deleted") shouldBe TrackStatus.FileDeleted
        }
    }

    @Test
    fun shouldNotFindInvalidKeys() {
        withClue("TrackStatus.findByKey() should return null for invalid keys")
        {
            TrackStatus.findByKey("").shouldBeNull()
            TrackStatus.findByKey("ree").shouldBeNull()
            TrackStatus.findByKey("pendingDownload").shouldBeNull()
            TrackStatus.findByKey("Downloaded").shouldBeNull()
        }
    }
}