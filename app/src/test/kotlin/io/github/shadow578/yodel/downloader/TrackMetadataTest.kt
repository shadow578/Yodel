package io.github.shadow578.yodel.downloader

import com.google.gson.Gson
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
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
        assertThat(metaFull, notNullValue(TrackMetadata::class.java))
        assertThat(metaFull.track, equalTo("The Commerce"))
        assertThat(metaFull.tags, containsInAnyOrder("Otseit", "The Commerce"))
        assertThat(metaFull.view_count, equalTo(128898492L))
        assertThat(metaFull.average_rating, equalTo(4.888588))
        assertThat(metaFull.upload_date, equalTo("20200924"))
        assertThat(metaFull.channel, equalTo("Otseit"))
        assertThat(metaFull.duration, equalTo(192L))
        assertThat(metaFull.creator, equalTo("Otseit"))
        assertThat(metaFull.dislike_count, equalTo(34855L))
        assertThat(metaFull.artist, equalTo("Otseit,AWatson"))
        assertThat(metaFull.album, equalTo("The Commerce"))
        assertThat(
            metaFull.title,
            equalTo("Otseit - The Commerce (Official Music Video)")
        )
        assertThat(metaFull.alt_title, equalTo("Otseit - The Commerce"))
        assertThat<Iterable<String>?>(
            metaFull.categories,
            containsInAnyOrder("Music")
        )
        assertThat(metaFull.like_count, equalTo(1216535L))
    }

    /**
     * [TrackMetadata.getTrackTitle]
     */
    @Test
    fun shouldGetTitle() {
        assertThat(metaFull.getTrackTitle(), equalTo("The Commerce"))
        assertThat(metaNoTrackArtistDate.getTrackTitle(), equalTo("Otseit - The Commerce"))
        assertThat(
            metaNoAltTitleCreatorBadDate.getTrackTitle(),
            equalTo("Otseit - The Commerce (Official Music Video)")
        )
        assertThat(metaNoTitleChannelDate.getTrackTitle(), nullValue())
    }

    /**
     * [TrackMetadata.getArtistName]
     */
    @Test
    fun shouldGetArtistName() {
        assertThat(metaFull.getArtistName(), equalTo("Otseit"))
        assertThat(metaNoTrackArtistDate.getArtistName(), equalTo("Otseit"))
        assertThat(metaNoAltTitleCreatorBadDate.getArtistName(), equalTo("Otseit"))
        assertThat(metaNoTitleChannelDate.getArtistName(), nullValue())
    }

    /**
     * [TrackMetadata.getUploadDate]
     */
    @Test
    fun shouldGetUploadDate() {
        assertThat(metaFull.getUploadDate(), equalTo(LocalDate.of(2020, 9, 24)))
        assertThat(metaNoTrackArtistDate.getUploadDate(), nullValue())
        assertThat(metaNoAltTitleCreatorBadDate.getUploadDate(), nullValue())
    }
}
