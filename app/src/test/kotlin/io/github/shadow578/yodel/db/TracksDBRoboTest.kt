package io.github.shadow578.yodel.db

import androidx.room.Room
import io.github.shadow578.yodel.*
import io.github.shadow578.yodel.db.model.*
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.*

/**
 * [TracksDB]
 */
class TracksDBRoboTest : RoboTest() {
    private lateinit var db: TracksDB

    @Before
    fun initDb() {
        runArchSingleThreaded()

        // this uses the same config as TracksDB.get(), but in- memory and with allowMainThreadQueries()
        db = Room.inMemoryDatabaseBuilder(
            context,
            TracksDB::class.java
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        // insert some tracks
        db.tracks().insertAll(
            listOf(
                TrackInfo("aabbcc", "A Title"),
                TrackInfo("bbccdd", "B Title"),
                TrackInfo("ccddee", "C Title", status = TrackStatus.Downloaded),
                TrackInfo("ddeeff", "D Title", status = TrackStatus.Downloaded),
                TrackInfo("eeffgg", "E Title", status = TrackStatus.Downloading)
            )
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun shouldGetAll() {
        db.tracks().all shouldContainExactlyInAnyOrder listOf(
            TrackInfo("aabbcc", "A Title"),
            TrackInfo("bbccdd", "B Title"),
            TrackInfo("ccddee", "C Title", status = TrackStatus.Downloaded),
            TrackInfo("ddeeff", "D Title", status = TrackStatus.Downloaded),
            TrackInfo("eeffgg", "E Title", status = TrackStatus.Downloading)
        )
    }


    @Test
    fun observeShouldEqualAll() {
        val all = db.tracks().all
        db.tracks().observe().getOrAwaitValue() shouldContainExactlyInAnyOrder all
    }

    @Test
    fun observePendingShouldGetPending() {
        db.tracks().pending shouldContainExactlyInAnyOrder listOf(
            TrackInfo("aabbcc", "A Title"),
            TrackInfo("bbccdd", "B Title")
        )
    }

    @Test
    fun shouldGetDownloaded() {
        db.tracks().downloaded shouldContainExactlyInAnyOrder listOf(
            TrackInfo("ccddee", "C Title", status = TrackStatus.Downloaded),
            TrackInfo("ddeeff", "D Title", status = TrackStatus.Downloaded)
        )
    }

    @Test
    fun shouldResetDownloading() {
        db.tracks().resetDownloadingToPending()
        db.tracks().all shouldContainExactlyInAnyOrder listOf(
            TrackInfo("aabbcc", "A Title"),
            TrackInfo("bbccdd", "B Title"),
            TrackInfo("ccddee", "C Title", status = TrackStatus.Downloaded),
            TrackInfo("ddeeff", "D Title", status = TrackStatus.Downloaded),
            TrackInfo("eeffgg", "E Title", status = TrackStatus.DownloadPending)
        )
    }

    @Test
    fun shouldGet() {
        db.tracks()["aabbcc"] shouldBe TrackInfo("aabbcc", "A Title")
    }

    @Test
    fun shouldInsertOverwriting() {
        db.tracks().insertAllNew(
            listOf(
                TrackInfo("ccddee", "G Title", status = TrackStatus.Downloaded),
                TrackInfo("ffffgg", "F Title"),
            )
        )
        db.tracks().all shouldContainExactlyInAnyOrder listOf(
            TrackInfo("aabbcc", "A Title"),
            TrackInfo("bbccdd", "B Title"),
            TrackInfo("ccddee", "G Title", status = TrackStatus.Downloaded),
            TrackInfo("ddeeff", "D Title", status = TrackStatus.Downloaded),
            TrackInfo("eeffgg", "E Title", status = TrackStatus.Downloading),
            TrackInfo("ffffgg", "F Title"),
        )
    }

    @Test
    fun shouldInsertNew() {
        db.tracks().insertAllNew(
            listOf(
                TrackInfo("ccddee", "G Title", status = TrackStatus.Downloaded),
                TrackInfo("ffffgg", "F Title"),
            )
        )
        db.tracks().all shouldContainExactlyInAnyOrder listOf(
            TrackInfo("aabbcc", "A Title"),
            TrackInfo("bbccdd", "B Title"),
            TrackInfo("ccddee", "C Title", status = TrackStatus.Downloaded),
            TrackInfo("ddeeff", "D Title", status = TrackStatus.Downloaded),
            TrackInfo("eeffgg", "E Title", status = TrackStatus.Downloading),
            TrackInfo("ffffgg", "F Title"),
        )
    }

    @Test
    fun shouldUpdate() {
        db.tracks()["aabbcc"] shouldBe TrackInfo("aabbcc", "A Title")
        db.tracks().update(TrackInfo("aabbcc", "FooBar", status = TrackStatus.Downloaded))
        db.tracks()["aabbcc"] shouldBe TrackInfo(
            "aabbcc",
            "FooBar",
            status = TrackStatus.Downloaded
        )
    }

    @Test
    fun shouldRemove() {
        db.tracks()["aabbcc"] shouldBe TrackInfo("aabbcc", "A Title")
        db.tracks().remove(TrackInfo("aabbcc", "FooBar"))
        db.tracks()["aabbcc"].shouldBeNull()
    }
}