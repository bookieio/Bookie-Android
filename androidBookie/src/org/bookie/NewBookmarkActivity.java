package org.bookie;

import java.util.LinkedList;
import java.util.List;

import org.bookie.R.id;
import org.bookie.model.BookMark;
import org.bookie.service.BookieService;
import org.bookie.service.NewBookmarkRequest.RequestSuccessListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

public class NewBookmarkActivity extends Activity {
	private List<String> tags = new LinkedList<String>();

	public class addTagButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			addTagButtonWasClicked();
		}
	}

	private final class DismissLater implements Runnable {
		public void run() {
			final NewBookmarkActivity thisActivity = NewBookmarkActivity.this;
			thisActivity.finish();
		}
	}

	private final class RequestSuccessListenerImpl implements
			RequestSuccessListener {
		@Override
		public void notify(Boolean requestWasSuccessful) {
			if(requestWasSuccessful) {
				NewBookmarkActivity.this.requestFinishedWithSuccess();
			} else {
				NewBookmarkActivity.this.requestFinishedWithFailure();
			}
		}
	}

	private final class SaveButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			saveButtonWasClicked();
		}
	}

	private static final String TAG = NewBookmarkActivity.class.getSimpleName();
	private static final int RESULTS_MESSAGE_DURATION = Toast.LENGTH_SHORT;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "A new NewBookmarkActivity is created");
		setContentView(R.layout.new_bookmark);
	    dealWithIntents();
		setUpSaveButton();
		setUpAddTagButton();
	}

	private void dealWithIntents() {
		Intent intent = getIntent();
	    if (isIntentForUs(intent)) {
	    	handleSendText(intent);
	    }
	}

	private boolean isIntentForUs(Intent intent) {
	    String action = intent.getAction();
	    String type = intent.getType();
		return Intent.ACTION_SEND.equals(action)
				&& type != null
				&& "text/plain".equals(type);
	}


	private void setUpSaveButton() {
		Button save = (Button) findViewById(id.newBookmarkSaveButton);
		save.setOnClickListener(new SaveButtonListener());
	}

	private void setUpAddTagButton() {
		Button addTag = (Button) findViewById(id.newBookmarkAddTagButton);
		addTag.setOnClickListener(new addTagButtonListener());
	}

	private RequestSuccessListener produceListenerForRequest() {
		return new RequestSuccessListenerImpl();
	}

	protected void requestFinishedWithFailure() {
		final String message = getString( R.string.new_bookmark_save_failed );
		showMessageAboutResultsOfSave(message);
	}

	protected void requestFinishedWithSuccess() {
		final String message = getString( R.string.new_bookmark_save_success );
		showMessageAboutResultsOfSave(message);
		dismissThisActivity(RESULTS_MESSAGE_DURATION);
	}

	private void showMessageAboutResultsOfSave(CharSequence message) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, message, RESULTS_MESSAGE_DURATION);
		toast.show();
	}

	private void handleSendText(Intent intent) {
		String url = intent.getStringExtra(Intent.EXTRA_TEXT);
		EditText uriField = (EditText) findViewById(id.newBookmarkUrlField);
		uriField.setText(url);
	}

	private void dismissThisActivity(int millisecDelay) {
		if(millisecDelay>0) {
			Handler handler = new Handler();
			final Runnable dismissLater = new DismissLater();
			handler.postDelayed(dismissLater, millisecDelay);
		} else {
			finish();
		}
	}

	private void saveButtonWasClicked() {
		Log.i(TAG,"Save Button Pressed");
		UserSettings settings = new SharedPrefsBackedUserSettings(NewBookmarkActivity.this);
		String user = settings.getUsername();
		String apiKey = settings.getApiKey();
		BookMark bmark = new BookMark();
		bmark.url = ((EditText) findViewById(id.newBookmarkUrlField)).getText().toString();
		bmark.description = ((EditText) findViewById(id.newBookmarkTitleField)).getText().toString();
		bmark.tags.addAll(tags);
		BookieService.getService().saveBookmark(user,apiKey,bmark,NewBookmarkActivity.this.produceListenerForRequest());
	}



	public void addTagButtonWasClicked() {
		Log.i(TAG,"Add Tag button was pressed");
		TableLayout table = (TableLayout) findViewById(id.newBookmarkTabTable);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Title");
		alert.setMessage("Message");

		// Set an EditText view to get user input
		final EditText aTextField = new EditText(this);
		alert.setView(aTextField);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String tagValue = aTextField.getText().toString();
			NewBookmarkActivity.this.tags.add(tagValue);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled -- nothing to do
		  }
		});

		alert.show();
//		TableRow child = new TableRow(this);
//		child.addView(aTextField);
//		table.addView(child);
	}
}