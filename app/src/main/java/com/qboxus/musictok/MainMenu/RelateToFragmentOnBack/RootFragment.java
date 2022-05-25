package com.qboxus.musictok.MainMenu.RelateToFragmentOnBack;


import androidx.fragment.app.Fragment;

/**
 * Created by qboxus on 3/30/2018.
 */

public class RootFragment extends Fragment implements OnBackPressListener {

    @Override
    public boolean onBackPressed() {
        return new BackPressImplimentation(this).onBackPressed();
    }
}