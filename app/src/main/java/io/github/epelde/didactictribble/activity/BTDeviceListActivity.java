package io.github.epelde.didactictribble.activity;

import android.support.v4.app.Fragment;

import io.github.epelde.didactictribble.fragment.BTDeviceListFragment;

/**
 * Created by epelde on 19/01/2016.
 */
public class BTDeviceListActivity extends SingleFragmentActivity implements BTDeviceListFragment.SelectionListener {

    public static final String SELECTED_DEVICE = "io.github.epelde.didactictribble.fragment.SELECTED_DEVICE";
    public static final int SELECT_DEVICE = 1;

    @Override
    public Fragment createFragment() {
        return new BTDeviceListFragment();
    }

    @Override
    public void selected() {
        finish();
    }
}
