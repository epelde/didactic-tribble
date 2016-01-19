package io.github.epelde.didactictribble.activity;

import android.support.v4.app.Fragment;

import io.github.epelde.didactictribble.fragment.BTDeviceListFragment;

/**
 * Created by epelde on 19/01/2016.
 */
public class BTDeviceListActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new BTDeviceListFragment();
    }
}
