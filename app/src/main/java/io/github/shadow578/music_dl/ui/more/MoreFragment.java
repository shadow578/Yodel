package io.github.shadow578.music_dl.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.music_dl.databinding.FragmentMoreBinding;
import io.github.shadow578.music_dl.ui.BaseFragment;

/**
 * more / about fragment
 */
public class MoreFragment extends BaseFragment {

    /**
     * view binding instance
     */
    @SuppressWarnings("FieldCanBeLocal")
    private FragmentMoreBinding b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentMoreBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}