package io.github.shadow578.yodel.db

import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.util.storage.StorageKey
import io.kotest.matchers.nulls.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.*
import org.junit.Test
import java.time.LocalDate

/**
 * [DBTypeConverters]
 */
class DBTypeConvertersTest {

    private val conv = DBTypeConverters()

    /**
     * [DBTypeConverters.fromStorageKey]
     * [DBTypeConverters.toStorageKey]
     */
    @Test
    fun shouldConvertStorageKey() {
        // key -> string -> key
        val originalKey = StorageKey("aabbccdd")
        val string = conv.fromStorageKey(originalKey)
        string.shouldNotBeNull()
        string.shouldNotBeBlank()

        conv.toStorageKey(string) shouldBe originalKey

        // null -> string -> key
        conv.fromStorageKey(null).shouldBeNull()
        conv.toStorageKey(null).shouldBeNull()

        // emtpy string
        conv.toStorageKey("") shouldBe StorageKey.EMPTY
    }

    /**
     * [DBTypeConverters.fromTrackStatus]
     * [DBTypeConverters.toTrackStatus]
     */
    @Test
    fun shouldConvertTrackStatus() {
        // status -> string -> status
        val originalStatus = TrackStatus.Downloading
        val string = conv.fromTrackStatus(originalStatus)
        string.shouldNotBeNull()
        string shouldBe "downloading"

        conv.toTrackStatus(string) shouldBe TrackStatus.Downloading

        // null -> string -> status
        conv.fromTrackStatus(null).shouldBeNull()
        conv.toTrackStatus(null).shouldBeNull()

        // empty string
        conv.toTrackStatus("") shouldBe TrackStatus.DownloadPending
    }

    /**
     * [DBTypeConverters.fromLocalDate]
     * [DBTypeConverters.toLocalDate]
     */
    @Test
    fun shouldConvertLocalDate() {
        // date -> string -> date
        val originalDate = LocalDate.of(2021, 8, 6)
        val string = conv.fromLocalDate(originalDate)
        string.shouldNotBeNull()
        string.shouldNotBeEmpty()
        // info: no check for the serialized format as we don't really care about it

        conv.toLocalDate(string) shouldBe originalDate

        // null -> string -> date
        conv.fromLocalDate(null).shouldBeNull()
        conv.toLocalDate(null).shouldBeNull()

        // empty string
        conv.toLocalDate("").shouldBeNull()
    }
}