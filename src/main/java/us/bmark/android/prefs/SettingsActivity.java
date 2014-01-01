package us.bmark.android.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import us.bmark.android.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.settings);
    }
}
