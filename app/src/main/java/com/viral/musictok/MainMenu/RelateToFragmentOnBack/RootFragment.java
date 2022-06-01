package com.viral.musictok.MainMenu.RelateToFragmentOnBack;


import androidx.fragment.app.Fragment;

/**
 * Created by viral on 3/30/2018.
 */

public class RootFragment extends Fragment implements OnBackPressListener {

    @Override
    public boolean onBackPressed() {
        return new BackPressImplimentation(this).onBackPressed();
    }
}