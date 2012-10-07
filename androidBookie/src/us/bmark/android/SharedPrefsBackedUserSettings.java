package us.bmark.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsBackedUserSettings implements UserSettings {
	private static final String USER_PREFS_DEFAULT_USERNAME = "";
	private static final String USER_PREFS_DEFAULT_APIKEY = "";
	private static final String USER_PREFS_KEY_USERNAME = "username";
	private static final String USER_PREFS_KEY_APIKEY = "apikey";


	private SharedPreferences prefs;

	public SharedPrefsBackedUserSettings(Context context) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/* (non-Javadoc)
	 * @see us.bmark.android.UserSetting#getUsername()
	 */
	@Override
	public String getUsername() {
		return prefs.getString(USER_PREFS_KEY_USERNAME, USER_PREFS_DEFAULT_USERNAME);
	}

	/* (non-Javadoc)
	 * @see us.bmark.android.UserSetting#getApiKey()
	 */
	@Override
	public String getApiKey() {
		return prefs.getString(USER_PREFS_KEY_APIKEY, USER_PREFS_DEFAULT_APIKEY);
	}

}
