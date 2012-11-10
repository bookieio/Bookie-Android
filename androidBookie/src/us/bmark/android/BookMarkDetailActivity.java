package us.bmark.android;

import java.util.List;

import us.bmark.android.model.BookMark;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
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
		refreshTagsTable(bmark.tags);
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

}
