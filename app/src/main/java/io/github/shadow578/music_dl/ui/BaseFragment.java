package io.github.shadow578.music_dl.ui;

import android.transition.Slide;
import android.view.Gravity;

import androidx.fragment.app.Fragment;

/**
 * base fragment, with some common functionality
 */
public class BaseFragment extends Fragment {

    public BaseFragment() {
        setEnterTransition(new Slide(Gravity.END));
        setExitTransition(new Slide(Gravity.START));
    }
}
