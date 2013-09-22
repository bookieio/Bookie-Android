package us.bmark.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsBackedUserSettings implements UserSettings {
    private static final String USER_PREFS_DEFAULT_USERNAME = "";
    private static final String USER_PREFS_DEFAULT_APIKEY = "";
    private static final String USER_PREFS_KEY_USERNAME = "username";
    private static final String USER_PREFS_KEY_APIKEY = "apikey";
    private static final String USER_PREFS_KEY_BASEURL = "base_url";
    private static final String USER_PREFS_DEFAULT_BASEURL = "https://bmark.us";


    private SharedPreferences prefs;

    public SharedPrefsBackedUserSettings(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getUsername() {
        return prefs.getString(USER_PREFS_KEY_USERNAME, USER_PREFS_DEFAULT_USERNAME);
    }

    @Override
    public String getApiKey() {
        return prefs.getString(USER_PREFS_KEY_APIKEY, USER_PREFS_DEFAULT_APIKEY);
    }

    @Override
    public String getBaseUrl() {
        return prefs.getString(USER_PREFS_KEY_BASEURL, USER_PREFS_DEFAULT_BASEURL);
    }

}
