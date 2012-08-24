package org.bookie;

import org.bookie.R.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class NewBookmarkActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// blah blah blah

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
	}

	private void handleSendText(Intent intent) {
		String url = intent.getStringExtra(Intent.EXTRA_TEXT);
		EditText uriField = (EditText) findViewById(id.newBookmarkUrlField);
		uriField.setText(url);
	}
}