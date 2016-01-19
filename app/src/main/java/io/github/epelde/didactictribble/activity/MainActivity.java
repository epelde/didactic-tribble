package io.github.epelde.didactictribble.activity;

import android.support.v4.app.Fragment;

import io.github.epelde.didactictribble.fragment.MainFragment;

/**
 * Created by epelde on 29/12/2015.
 */
public class MainActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new MainFragment();
    }
}
