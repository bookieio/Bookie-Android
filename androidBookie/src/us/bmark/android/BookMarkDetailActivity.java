package us.bmark.android;

import us.bmark.android.model.BookMark;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class BookMarkDetailActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_mark_detail);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		dealWithIntents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_book_mark_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void dealWithIntents() {
		Intent intent = getIntent();
		//if (isIntentForUs(intent)) {
			BookMark bmark = intent.getExtras().getParcelable("bmark");
			populateFields(bmark);
		//}
	}

	private void populateFields(BookMark bmark) {
		TextView tv = (TextView) findViewById(R.id.BookmarkDetailTextView);
		tv.setText(bmark.url);
	}

	private boolean isIntentForUs(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		return Intent.ACTION_SEND.equals(action) && type != null
				&& "text/plain".equals(type);
	}
/*
	private SharedPrefsBackedUserSettings userSettings() {
		return new SharedPrefsBackedUserSettings(this);
	}

	private BookieService service() {
		final UserSettings settings = userSettings();
		final String baseUrl = settings.getBaseUrl();
		BookieService service = BookieService.getService(baseUrl);
		return service;
	}
*/
}
