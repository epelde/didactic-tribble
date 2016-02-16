package io.github.epelde.didactictribble.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import io.github.epelde.didactictribble.R;

/**
 * Created by epelde on 16/02/2016.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
