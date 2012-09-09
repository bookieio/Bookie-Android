package org.bookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserSettings {
	private static final String USER_PREFS_DEFAULT_USERNAME = "";
	private static final String USER_PREFS_DEFAULT_APIKEY = "";
	private static final String USER_PREFS_KEY_USERNAME = "username";
	private static final String USER_PREFS_KEY_APIKEY = "apikey";


	private SharedPreferences prefs;

	public UserSettings(Context context) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String getUsername() {
		return prefs.getString(USER_PREFS_KEY_USERNAME, USER_PREFS_DEFAULT_USERNAME);
	}

	public String getApiKey() {
		return prefs.getString(USER_PREFS_KEY_APIKEY, USER_PREFS_DEFAULT_APIKEY);
	}

}
