package io.github.shadow578.yodel.ui.main

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.elevation.ElevationOverlayProvider
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.databinding.ActivityMainBinding
import io.github.shadow578.yodel.ui.base.BaseActivity
import io.github.shadow578.yodel.ui.more.MoreFragment
import io.github.shadow578.yodel.ui.tracks.TracksFragment

/**
 * the main activity
 */
class MainActivity : BaseActivity() {
    private val tracksFragment = TracksFragment()
    private val moreFragment = MoreFragment()

    /**
     * order of the sections
     */
    private val sectionOrder = listOf(
        Section.Tracks,
        Section.More
    )

    /**
     * the view model instance
     */
    private lateinit var model: MainViewModel

    /**
     * the view binding instance
     */
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // create model
        model = ViewModelProvider(this).get(MainViewModel::class.java)

        // init UI
        setupBottomNavigationAndPager()

        // select downloads dir
        maybeSelectDownloadsDir(false)
    }

    /**
     * set up the fragment view pager and bottom navigation so they work
     * together with each other & the view model
     */
    private fun setupBottomNavigationAndPager() {
        b.fragmentPager.adapter = SectionAdapter(this)

        // setup bottom navigation listener to update model
        b.bottomNav.setOnItemSelectedListener { item ->
            // find section with matching id
            for (section in Section.values()) {
                if (section.menuItemId == item.itemId) {
                    model.switchToSection(section)
                    return@setOnItemSelectedListener true
                }
            }
            true
        }

        // setup viewpager listener to update model
        b.fragmentPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                model.switchToSection(sectionOrder[position])
            }
        })

        // sync model with pager and bottom navigation
        model.section.observe(this) { section: Section ->
            b.bottomNav.selectedItemId = section.menuItemId
            b.fragmentPager.currentItem = sectionOrder.indexOf(section)
            b.fragmentPager.isUserInputEnabled = section.allowPagerInput
        }

        // set the color of the navigation bar to the color of the bottom navigation view
        window.navigationBarColor =
            ElevationOverlayProvider(this).compositeOverlayWithThemeSurfaceColorIfNeeded(b.bottomNav.elevation)
    }

    /**
     * get the fragment instance by section name
     *
     * @param section the section name
     * @return the fragment
     */
    private fun getSectionFragment(section: Section): Fragment {
        return when (section) {
            Section.Tracks -> tracksFragment
            Section.More -> moreFragment
        }
    }

    /**
     * fragments / sections of the main activity
     *
     * @param menuItemId      the menu item of this section
     * @param allowPagerInput allow user input on the view pager?
     */
    enum class Section(
        @field:IdRes
        @param:IdRes val menuItemId: Int,
        val allowPagerInput: Boolean
    ) {
        /**
         * the tracks library fragment
         */
        Tracks(R.id.nav_tracks, true),

        /**
         * the more / about fragment
         */
        More(R.id.nav_more, true);
    }

    /**
     * adapter for the view pager
     */
    private inner class SectionAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return getSectionFragment(sectionOrder[position])
        }

        override fun getItemCount(): Int {
            return sectionOrder.size
        }
    }
}