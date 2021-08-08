package io.github.shadow578.yodel.downloader

import com.google.gson.Gson
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.*
import io.kotest.matchers.shouldBe
import org.junit.*
import java.time.LocalDate

/**
 * test for [TrackMetadata]
 */
class TrackMetadataTest {
    private lateinit var metaFull: TrackMetadata
    private lateinit var metaNoTrackArtistDate: TrackMetadata
    private lateinit var metaNoAltTitleCreatorBadDate: TrackMetadata
    private lateinit var metaNoTitleChannelDate: TrackMetadata

    /**
     * deserialize a mock metadata json object
     */
    @Before
    fun deserializeMetadataJson() {
        // prepare json (totally not based on real data)
        val jsonFull = """
{
    "track": "The Commerce",
    "tags": [
        "Otseit",
        "The Commerce"
    ],
    "view_count": 128898492,
    "average_rating": 4.888588,
    "upload_date": "20200924",
    "channel": "Otseit",
    "duration": 192,
    "creator": "Otseit",
    "dislike_count": 34855,
    "artist": "Otseit,AWatson",
    "album": "The Commerce",
    "title": "Otseit - The Commerce (Official Music Video)",
    "alt_title": "Otseit - The Commerce",
    "categories": [
        "Music"
    ],
    "like_count": 1216535
}
"""
        val jsonNoTrackArtistUpload = """
{
    "track": "",
    "tags": [
        "Otseit",
        "The Commerce"
    ],
    "view_count": 128898492,
    "average_rating": 4.888588,
    "upload_date": "",
    "channel": "Otseit",
    "duration": 192,
    "creator": "Otseit",
    "dislike_count": 34855,
    "artist": "",
    "album": "The Commerce",
    "title": "Otseit - The Commerce (Official Music Video)",
    "alt_title": "Otseit - The Commerce",
    "categories": [
        "Music"
    ],
    "like_count": 1216535
}
"""
        val jsonNoAltTitleCreatorBadDate = """
{
    "track": "",
    "tags": [
        "Otseit",
        "The Commerce"
    ],
    "view_count": 128898492,
    "average_rating": 4.888588,
    "upload_date": "foobar",
    "channel": "Otseit",
    "duration": 192,
    "creator": "",
    "dislike_count": 34855,
    "artist": "",
    "album": "The Commerce",
    "title": "Otseit - The Commerce (Official Music Video)",
    "alt_title": "",
    "categories": [
        "Music"
    ],
    "like_count": 1216535
}
"""
        val jsonNoTitleChannelDate = """
{
    "track": "",
    "tags": [
        "Otseit",
        "The Commerce"
    ],
    "view_count": 128898492,
    "average_rating": 4.888588,
    "upload_date": "",
    "channel": "",
    "duration": 192,
    "creator": "",
    "dislike_count": 34855,
    "artist": "",
    "album": "The Commerce",
    "title": "",
    "alt_title": "",
    "categories": [
        "Music"
    ],
    "like_count": 1216535
}
"""


        // deserialize the object using (a default) GSON
        val gson = Gson()
        metaFull = gson.fromJson(jsonFull, TrackMetadata::class.java)
        metaNoTrackArtistDate = gson.fromJson(jsonNoTrackArtistUpload, TrackMetadata::class.java)
        metaNoAltTitleCreatorBadDate =
            gson.fromJson(jsonNoAltTitleCreatorBadDate, TrackMetadata::class.java)
        metaNoTitleChannelDate = gson.fromJson(jsonNoTitleChannelDate, TrackMetadata::class.java)
    }

    /**
     * testing if fields are deserialized correctly
     */
    @Test
    fun shouldDeserialize() {
        // check fields are correct
        metaFull.shouldNotBeNull()
        with(metaFull) {
            track shouldBe "The Commerce"
            tags.shouldContainExactlyInAnyOrder("Otseit", "The Commerce")
            view_count shouldBe 128898492
            average_rating shouldBe 4.888588
            upload_date shouldBe "20200924"
            channel shouldBe "Otseit"
            duration shouldBe 192
            creator shouldBe "Otseit"
            dislike_count shouldBe 34855
            artist shouldBe "Otseit,AWatson"
            album shouldBe "The Commerce"
            title shouldBe "Otseit - The Commerce (Official Music Video)"
            alt_title shouldBe "Otseit - The Commerce"
            categories.shouldContainExactlyInAnyOrder("Music")
            like_count shouldBe 1216535
        }
    }

    /**
     * [TrackMetadata.getTrackTitle]
     */
    @Test
    fun shouldGetTitle() {
        metaFull.getTrackTitle() shouldBe "The Commerce"
        metaNoTrackArtistDate.getTrackTitle() shouldBe "Otseit - The Commerce"
        metaNoAltTitleCreatorBadDate.getTrackTitle() shouldBe "Otseit - The Commerce (Official Music Video)"
        metaNoTitleChannelDate.getTrackTitle().shouldBeNull()
    }

    /**
     * [TrackMetadata.getArtistName]
     */
    @Test
    fun shouldGetArtistName() {
        metaFull.getArtistName() shouldBe "Otseit"
        metaNoTrackArtistDate.getArtistName() shouldBe "Otseit"
        metaNoAltTitleCreatorBadDate.getArtistName() shouldBe "Otseit"
        metaNoTitleChannelDate.getArtistName().shouldBeNull()
    }

    /**
     * [TrackMetadata.getUploadDate]
     */
    @Test
    fun shouldGetUploadDate() {
        metaFull.getUploadDate() shouldBe LocalDate.of(2020, 9, 24)
        metaNoTrackArtistDate.getUploadDate().shouldBeNull()
        metaNoAltTitleCreatorBadDate.getUploadDate().shouldBeNull()
    }
}