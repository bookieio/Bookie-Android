package us.bmark.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import us.bmark.android.UserSettings;

public class SharedPrefsBackedUserSettings implements UserSettings {
    private static final String USER_PREFS_DEFAULT_USERNAME = "";
    private static final String USER_PREFS_DEFAULT_APIKEY = "";
    private static final String USER_PREFS_KEY_USERNAME = "username";
    private static final String USER_PREFS_KEY_APIKEY = "apikey";
    private static final String USER_PREFS_KEY_BASE_URL = "base_url";
    private static final String USER_PREFS_KEY_PARSE_URL = "parse_url";

    // TODO: defaults duplicated with strings.xml, should live somewhere else?
    private static final String USER_PREFS_DEFAULT_PARSE_URL = "http://r.bmark.us";
    private static final String USER_PREFS_DEFAULT_BASE_URL = "https://bmark.us";


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
        return prefs.getString(USER_PREFS_KEY_BASE_URL, USER_PREFS_DEFAULT_BASE_URL);
    }

    @Override
    public String getParserUrl() {
        return prefs.getString(USER_PREFS_KEY_PARSE_URL, USER_PREFS_DEFAULT_PARSE_URL);
    }

}
