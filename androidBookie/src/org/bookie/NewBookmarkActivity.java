package org.bookie;

import org.bookie.R.id;
import org.bookie.model.BookMark;
import org.bookie.service.BookieService;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewBookmarkActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("NewBookemarkActivity", "A new NewBookmarkActivity is created");
		setContentView(R.layout.new_bookmark);

	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if ("text/plain".equals(type)) {
	            handleSendText(intent); // Handle text being sent
	        }
		}

		setUpSaveButton();
	}

	private void setUpSaveButton() {
		Button save = (Button) findViewById(id.newBookmarkSaveButton);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NewBookmarkActiviy","Save Button Pressed");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewBookmarkActivity.this);
				String user = prefs.getString("username", ""); // TODO default
				String apiKey = prefs.getString("apikey", ""); // TODO constants
				BookMark bmark = new BookMark();
				bmark.url = ((EditText) findViewById(id.newBookmarkUrlField)).getText().toString();
				bmark.description = ((EditText) findViewById(id.newBookmarkTitleField)).getText().toString();
				BookieService.getService().saveBookmark(user,apiKey,bmark);
			}

		});

	}

	private void handleSendText(Intent intent) {
		String url = intent.getStringExtra(Intent.EXTRA_TEXT);
		EditText uriField = (EditText) findViewById(id.newBookmarkUrlField);
		uriField.setText(url);
	}
}