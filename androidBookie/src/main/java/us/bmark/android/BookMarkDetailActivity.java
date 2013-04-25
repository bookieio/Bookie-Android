package us.bmark.android;

import static utils.Utils.equalButNotBlank;

import java.util.List;

import us.bmark.android.model.BookMark;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
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
		BookMark bmark = intent.getExtras().getParcelable("bmark");
		populateFields(bmark);
	}

	private void populateFields(BookMark bmark) {
		((TextView) findViewById(R.id.bookmarkDetailTextviewDescription)).setText(bmark.description);
		((TextView) findViewById(R.id.bookmarkDetailTextviewUrl)).setText(bmark.url);
		((TextView) findViewById(R.id.bookmarkDetailTextviewUsername)).setText(bmark.username);
		((TextView) findViewById(R.id.bookmarkDetailTextviewStored)).setText(bmark.stored);
		((TextView) findViewById(R.id.bookmarkDetailTextviewTotalClicks)).setText(Integer.toString(bmark.totalClicks));
		final TextView myClicksTextView = (TextView) findViewById(R.id.bookmarkDetailTextviewClicks);
		if(isMyBookmark(bmark)){
			myClicksTextView.setText(Integer.toString(bmark.clicks));
		} else {
			myClicksTextView.setVisibility(View.INVISIBLE);
			((TextView) findViewById(R.id.bookMarkDetailTextviewCountLabel)).setVisibility(View.INVISIBLE);
		}
		refreshTagsTable(bmark.tags);
	}

	private boolean isMyBookmark(BookMark bmark) {
		return equalButNotBlank(bmark.username, userSettings().getUsername() );
	}

	private void refreshTagsTable(List<String> tags) {
		final TableLayout table = (TableLayout) findViewById(R.id.bookmarkDetailTagTable);
		table.removeAllViews();

		for(String tagText: tags) {
			final TableRow tagRow = createTagRow(tagText);
			table.addView(tagRow);
		}
	}

	private TableRow createTagRow(String tagText) {
		TableRow rowView = new TableRow(this);
		TextView tagTextView = new TextView(this);
		tagTextView.setText(tagText);
		rowView.addView(tagTextView);
		return rowView;
	}

	private SharedPrefsBackedUserSettings userSettings() {
		return new SharedPrefsBackedUserSettings(this);
	}

}
