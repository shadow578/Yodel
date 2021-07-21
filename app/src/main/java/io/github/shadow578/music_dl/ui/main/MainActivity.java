package io.github.shadow578.music_dl.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.ActivityMainBinding;
import io.github.shadow578.music_dl.ui.BaseActivity;
import io.github.shadow578.music_dl.ui.more.MoreFragment;
import io.github.shadow578.music_dl.ui.tracks.TracksFragment;
import io.github.shadow578.music_dl.ui.ytmusic.YoutubeMusicFragment;

/**
 * the main activity
 */
public class MainActivity extends BaseActivity {

    private final YoutubeMusicFragment exploreFragment = new YoutubeMusicFragment();
    private final TracksFragment tracksFragment = new TracksFragment();
    private final MoreFragment moreFragment = new MoreFragment();

    /**
     * the view model instance
     */
    private MainViewModel model;

    /**
     * the view binding instance
     */
    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        initFragments();

        // create model
        model = new ViewModelProvider(this).get(MainViewModel.class);

        // sync model section with UI
        model.getSection().observe(this, this::navigateToSection);

        // setup bottom navigation listener
        b.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_more) {
                model.switchToSection(Section.More);
            } else if (item.getItemId() == R.id.nav_explore) {
                model.switchToSection(Section.Explore);
            } else {
                // default to nav_tracks
                model.switchToSection(Section.Tracks);
            }
            return true;
        });

        // select downloads dir
        maybeSelectDownloadsDir();
    }

    /**
     * add all fragments to the container, but hidden
     */
    private void initFragments() {
        // start the transaction
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // add all fragments, but hide them initially
        for (Section section : Section.values()) {
            final Fragment fragment = getSectionFragment(section);
            transaction.add(R.id.fragment_container, fragment, section.name())
                    .hide(fragment);
        }

        // execute the transaction
        transaction.commit();
    }

    /**
     * navigate to one of the section fragments
     *
     * @param section the section to navigate to
     */
    private void navigateToSection(@NonNull Section section) {
        // start the transaction
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // hide all other sections
        for (Section s : Section.values()) {
            if (!s.equals(section)) {
                transaction.hide(getSectionFragment(s));
            }
        }

        // show the selected section
        transaction.show(getSectionFragment(section));

        // execute the transaction
        transaction.commit();
    }

    /**
     * get the fragment instance by section name
     *
     * @param section the section name
     * @return the fragment
     */
    @NonNull
    private Fragment getSectionFragment(@NonNull Section section) {
        switch (section) {
            default:
            case Tracks:
                return tracksFragment;
            case Explore:
                return exploreFragment;
            case More:
                return moreFragment;
        }
    }

    /**
     * fragments / sections of the main activity
     */
    public enum Section {
        /**
         * the tracks library fragment
         */
        Tracks,

        /**
         * the explore fragment
         */
        Explore,

        /**
         * the more / about fragment
         */
        More
    }
}