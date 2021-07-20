package io.github.shadow578.music_dl.ui;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.ActivityMainBinding;
import io.github.shadow578.music_dl.ui.tracks.TracksFragment;
import io.github.shadow578.music_dl.ui.ytmusic.YoutubeMusicFragment;

public class MainActivity extends AppCompatActivity {

    private final YoutubeMusicFragment exploreFragment = new YoutubeMusicFragment();
    private final TracksFragment tracksFragment = new TracksFragment();

    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // setup bottom navigation
        navigateToFragment(R.id.nav_tracks);
        b.bottomNav.setOnItemSelectedListener(item -> {
            navigateToFragment(item.getItemId());
            return true;
        });
    }

    /**
     * navigate to one of the fragments, using the ids from the bottom navigation bar.
     * if the id is invalid, defaults to the nav_explore fragment
     *
     * @param navId the id from the bottom navigation bar
     */
    private void navigateToFragment(@IdRes int navId) {
        // select fragment to navigate to
        final Fragment targetFragment;
        if (navId == R.id.nav_more) {
            targetFragment = exploreFragment;
        } else if (navId == R.id.nav_explore) {
            targetFragment = exploreFragment;
        } else {
            // default to nav_tracks
            targetFragment = tracksFragment;
        }

        // load the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .commit();
    }
}