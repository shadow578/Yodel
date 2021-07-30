package io.github.shadow578.music_dl.ui.main;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.ActivityMainBinding;
import io.github.shadow578.music_dl.ui.base.BaseActivity;
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
     * order of the sections
     */
    private final List<Section> sectionOrder = Arrays.asList(
            Section.Tracks,
            Section.Explore,
            Section.More
    );

    /**
     * the view model instance
     */
    private MainViewModel model;

    /**
     * the view binding instance
     */
    private ActivityMainBinding b;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // create model
        model = new ViewModelProvider(this).get(MainViewModel.class);

        // init UI
        setupBottomNavigationAndPager();

        // select downloads dir
        maybeSelectDownloadsDir(false);
    }

    /**
     * set up the fragment view pager and bottom navigation so they work
     * together with each other & the view model
     */
    private void setupBottomNavigationAndPager() {
        b.fragmentPager.setAdapter(new SectionAdapter(this));

        // setup bottom navigation listener to update model
        b.bottomNav.setOnItemSelectedListener(item -> {
            // find section with matching id
            for (Section section : Section.values()) {
                if (section.menuItemId == item.getItemId()) {
                    model.switchToSection(section);
                    return true;
                }
            }

            return true;
        });

        // setup viewpager listener to update model
        b.fragmentPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                model.switchToSection(sectionOrder.get(position));
            }
        });

        // sync model with pager and bottom navigation
        model.getSection().observe(this, section ->
        {
            b.bottomNav.setSelectedItemId(section.menuItemId);
            b.fragmentPager.setCurrentItem(sectionOrder.indexOf(section));
            b.fragmentPager.setUserInputEnabled(section.allowPagerInput);
        });
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
        Tracks(R.id.nav_tracks, false),

        /**
         * the explore fragment
         */
        Explore(R.id.nav_explore, false),

        /**
         * the more / about fragment
         */
        More(R.id.nav_more, false);

        /**
         * the id of the menu item for this section (in bottom navigation)
         */
        @IdRes
        private final int menuItemId;

        /**
         * enable user input on the view pager for this section?
         */
        private final boolean allowPagerInput;

        /**
         * create a new section in the main activity
         *
         * @param menuItemId      the menu item of this section
         * @param allowPagerInput allow user input on the view pager?
         */
        Section(@IdRes int menuItemId, boolean allowPagerInput) {
            this.menuItemId = menuItemId;
            this.allowPagerInput = allowPagerInput;
        }
    }

    /**
     * adapter for the view pager
     */
    private class SectionAdapter extends FragmentStateAdapter {
        public SectionAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return getSectionFragment(sectionOrder.get(position));
        }

        @Override
        public int getItemCount() {
            return sectionOrder.size();
        }
    }
}