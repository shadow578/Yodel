package io.github.shadow578.music_dl.downloader;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * test for {@link TrackMetadata}
 */
public class TrackMetadataTest {

    private TrackMetadata meta;

    /**
     * deserialize a mock metadata json object
     */
    @Before
    public void deserializeMetadataJson() {
        // prepare json (totally not based on real data)
        final String json = "{\"track\":\"The Commerce\",\"tags\":[\"Otseit\",\"The Commerce\"],\"view_count\":128898492,\"average_rating\":4.888588,\"upload_date\":\"20200924\",\"channel\":\"Otseit\",\"duration\":192,\"creator\":\"Otseit\",\"dislike_count\":34855,\"artist\":\"Otseit,AWatson\",\"album\":\"The Commerce\",\"title\":\"Otseit - The Commerce (Official Music Video)\",\"alt_title\":\"Otseit - The Commerce\",\"categories\":[\"Music\"],\"like_count\":1216535}";
        /*
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
}*/

        // deserialize the object using (a default) GSON
        final Gson gson = new Gson();
        meta = gson.fromJson(json, TrackMetadata.class);
    }

    /**
     * testing if fields are deserialized correctly
     */
    @Test
    public void shouldDeserialize() {
        // check fields are correct
        assertThat(meta, notNullValue(TrackMetadata.class));
        assertThat(meta.track, equalTo("The Commerce"));
        assertThat(meta.tags, containsInAnyOrder("Otseit", "The Commerce"));
        assertThat(meta.view_count, equalTo(128898492L));
        assertThat(meta.average_rating, equalTo(4.888588));
        assertThat(meta.upload_date, equalTo("20200924"));
        assertThat(meta.channel, equalTo("Otseit"));
        assertThat(meta.duration, equalTo(192L));
        assertThat(meta.creator, equalTo("Otseit"));
        assertThat(meta.dislike_count, equalTo(34855L));
        assertThat(meta.artist, equalTo("Otseit,AWatson"));
        assertThat(meta.album, equalTo("The Commerce"));
        assertThat(meta.title, equalTo("Otseit - The Commerce (Official Music Video)"));
        assertThat(meta.alt_title, equalTo("Otseit - The Commerce"));
        assertThat(meta.categories, containsInAnyOrder("Music"));
        assertThat(meta.like_count, equalTo(1216535L));
    }

    /**
     * {@link TrackMetadata#getTrackTitle()}
     */
    @Test
    public void shouldGetTitle() {
        assertThat(meta.getTrackTitle(), isPresentAnd(equalTo("The Commerce")));

        meta.track = null;
        assertThat(meta.getTrackTitle(), isPresentAnd(equalTo("Otseit - The Commerce")));

        meta.alt_title = null;
        assertThat(meta.getTrackTitle(), isPresentAnd(equalTo("Otseit - The Commerce (Official Music Video)")));

        meta.title = null;
        assertThat(meta.getTrackTitle(), isEmpty());
    }

    /**
     * {@link TrackMetadata#getArtistName()}
     */
    @Test
    public void shouldGetArtistName() {
        assertThat(meta.getArtistName(), isPresentAnd(equalTo("Otseit")));

        meta.artist = null;
        assertThat(meta.getArtistName(), isPresentAnd(equalTo("Otseit")));

        meta.creator = null;
        assertThat(meta.getArtistName(), isPresentAnd(equalTo("Otseit")));

        meta.channel = null;
        assertThat(meta.getArtistName(), isEmpty());
    }

    /**
     * {@link TrackMetadata#getUploadDate()}
     */
    @Test
    public void shouldGetUploadDate() {
        assertThat(meta.getUploadDate(), isPresentAnd(equalTo(LocalDate.of(2020, 9, 24))));

        meta.upload_date = null;
        assertThat(meta.getUploadDate(), isEmpty());

        meta.upload_date = "foobar";
        assertThat(meta.getUploadDate(), isEmpty());
    }
}
