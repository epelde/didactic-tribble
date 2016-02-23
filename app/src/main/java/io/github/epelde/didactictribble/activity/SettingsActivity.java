package io.github.epelde.didactictribble.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

import io.github.epelde.didactictribble.R;

/**
 * Created by epelde on 16/02/2016.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference codeEditTextPref;
    private EditTextPreference keyEditTextPref;

    private static final String CODE_PREF = "pref_param_code";
    private static final String KEY_PREF = "pref_param_key";
    private static final String DEFAULT_VALUE = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
        codeEditTextPref = (EditTextPreference) findPreference(CODE_PREF);
        Log.i("", "* * * VALUE:" + sp.getString(CODE_PREF, DEFAULT_VALUE));
        codeEditTextPref.setSummary(sp.getString(CODE_PREF, DEFAULT_VALUE));
        keyEditTextPref = (EditTextPreference) findPreference(KEY_PREF);
        keyEditTextPref.setSummary(sp.getString(KEY_PREF, DEFAULT_VALUE));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(CODE_PREF)) {
            codeEditTextPref.setSummary(sp.getString(CODE_PREF, DEFAULT_VALUE));
        }
        if (key.equals(KEY_PREF)) {
            keyEditTextPref.setSummary(sp.getString(KEY_PREF, DEFAULT_VALUE));
        }
    }
}
