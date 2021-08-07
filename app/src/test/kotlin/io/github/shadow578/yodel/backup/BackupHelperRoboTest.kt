package io.github.shadow578.yodel.backup

import androidx.documentfile.provider.DocumentFile
import androidx.room.Room
import io.github.shadow578.yodel.RoboTest
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.TrackInfo
import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.runArchSingleThreaded
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowContentResolver
import java.io.File

/**
 * [BackupHelper]
 */
class BackupHelperRoboTest : RoboTest() {
    lateinit var db: TracksDB

    lateinit var shadowContentResolver: ShadowContentResolver

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

        // we need a shadowed content resolver
        shadowContentResolver = Shadows.shadowOf(context.contentResolver)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun shouldRestoreBackup() {
        val backupHelper = BackupHelper(context, db)
        val backupFile = File.createTempFile("test_backup", ".json", context.cacheDir)
        val backupDocFile = DocumentFile.fromFile(backupFile)

        withClue("create the backup")
        {
            backupFile.outputStream().use {
                shadowContentResolver.registerOutputStream(backupDocFile.uri, it)

                backupHelper.createBackup(backupDocFile) shouldBe true
                backupDocFile.exists() shouldBe true
            }
        }

        withClue("clear database before restore test")
        {
            db.clearAllTables()
            db.tracks().all.shouldBeEmpty()
        }

        withClue("restore backup from file, replacing existing")
        {
            // insert one track into db to overwrite
            db.tracks().insert(TrackInfo("bbccdd", "FooBar"))

            // read backup
            backupFile.inputStream().use {
                shadowContentResolver.registerInputStream(backupDocFile.uri, it)

                val backup = backupHelper.readBackup(backupDocFile)
                backup.shouldNotBeNull()

                backupHelper.restoreBackup(backup, true)
                db.tracks().all shouldContainExactlyInAnyOrder listOf(
                        TrackInfo("aabbcc", "A Title"),
                        TrackInfo("bbccdd", "B Title"),
                        TrackInfo("ccddee", "C Title"),
                        TrackInfo("ddeeff", "D Title"),
                        TrackInfo("eeffgg", "E Title")
                )
            }
        }

        withClue("restore backup from file, keeping existing")
        {
            // update one of the entries to be different than the backup
            db.tracks().update(TrackInfo("bbccdd", "FooBar"))

            // read backup
            backupFile.inputStream().use {
                shadowContentResolver.registerInputStream(backupDocFile.uri, it)

                val backup = backupHelper.readBackup(backupDocFile)
                backup.shouldNotBeNull()

                backupHelper.restoreBackup(backup, false)
                db.tracks().all shouldContainExactlyInAnyOrder listOf(
                        TrackInfo("aabbcc", "A Title"),
                        TrackInfo("bbccdd", "FooBar"),
                        TrackInfo("ccddee", "C Title"),
                        TrackInfo("ddeeff", "D Title"),
                        TrackInfo("eeffgg", "E Title")
                )
            }
        }
    }
}