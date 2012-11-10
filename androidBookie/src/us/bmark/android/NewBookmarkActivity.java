package us.bmark.android;


import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import us.bmark.android.R.id;
import us.bmark.android.model.BookMark;
import us.bmark.android.service.BookieService;
import us.bmark.android.service.NewBookmarkRequest.RequestSuccessListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class NewBookmarkActivity extends Activity {

	private static final int RESULTS_MESSAGE_DURATION = Toast.LENGTH_SHORT;
	private static final String STATE_TAGS_KEY = "NEW-BOOKMARK-TAGS";

	private Set<String> tags = new TreeSet<String>();

	private final class RemoveTagButtonListener implements
			OnClickListener {
		@Override
		public void onClick(View buttonClicked) {
			final String tagText = (String) buttonClicked.getTag();
			removeTag(tagText);
		}
	}

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

	public class CancelButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			cancelButtonWasClicked();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_bookmark);
	    dealWithIntents();
		setUpSaveButton();
		setUpCancelButton();
		setUpAddTagButton();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	 	ArrayList<String> tagsArrayList = new ArrayList<String>(tags);
	    savedInstanceState.putStringArrayList(STATE_TAGS_KEY,  tagsArrayList);
	    super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    ArrayList<String> tagsArrayList = savedInstanceState.getStringArrayList(STATE_TAGS_KEY);
		tags.addAll(tagsArrayList);
		refreshTagsTable();
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

	private void setUpCancelButton() {
		Button save = (Button) findViewById(id.newBookmarkCancelButton);
		save.setOnClickListener(new CancelButtonListener());
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
		UserSettings settings = new SharedPrefsBackedUserSettings(NewBookmarkActivity.this);
		String user = settings.getUsername();
		String apiKey = settings.getApiKey();
		BookMark bmark = new BookMark();
		bmark.url = ((EditText) findViewById(id.newBookmarkUrlField)).getText().toString();
		bmark.description = ((EditText) findViewById(id.newBookmarkTitleField)).getText().toString();
		bmark.tags.addAll(tags);
		service().saveBookmark(user,apiKey,bmark,NewBookmarkActivity.this.produceListenerForRequest());
	}

	public void cancelButtonWasClicked() {
		dismissThisActivity(0);
	}

	public void addTagButtonWasClicked() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.button_new_bookmark_new_tag_dialog_title));
		alert.setMessage(R.string.button_new_bookmark_new_tag_dialog_text);

		// Set an EditText view to get user input
		final EditText aTextField = new EditText(this);
		alert.setView(aTextField);

		alert.setPositiveButton(getString(R.string.button_new_bookmark_new_tag_dialog_ok_button_text),
				new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String tagValue = aTextField.getText().toString();
			addNewTag(tagValue);
			dissmsissSoftKeyBoard(aTextField);
		  }
		});

		alert.setNegativeButton(getString(R.string.button_new_bookmark_new_tag_dialog_cancel_button_text),
				new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    dissmsissSoftKeyBoard(aTextField);
		  }
		});



		alert.show();
	}

	protected void dissmsissSoftKeyBoard(EditText editText) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	private void addNewTag(String tagValue) {
		tags.add(tagValue);
		refreshTagsTable();
	}


	private void removeTag(String tagText) {
		tags.remove(tagText);
		refreshTagsTable();
	}

	private void refreshTagsTable() {
		final TableLayout table = (TableLayout) findViewById(id.newBookmarkTabTable);
		table.removeAllViews();

		for(String tagText: tags) {
			final TableRow tagRow = createTagRow(tagText);
			table.addView(tagRow);
		}
	}

	private TableRow createTagRow(String tagText) {
		final TableRow tagRow = new TableRow(this);

		final TextView tagTextView = new TextView(this);
		tagTextView.setText(tagText);
		tagTextView.setPadding(8, 8, 8, 8);
		tagRow.addView(tagTextView);

		final Button delButton = new Button(this);
		delButton.setTag(tagText);
		delButton.setOnClickListener(new RemoveTagButtonListener());
		delButton.setBackgroundResource(R.drawable.delete_button);
		delButton.setText("X");
		delButton.setPadding(8, 8, 8, 8);
		tagRow.addView(delButton);

		tagRow.setPadding(8, 8, 8, 8);
		return tagRow;
	}


	private SharedPrefsBackedUserSettings userSettings() {
		return new SharedPrefsBackedUserSettings(this);
	}

	private BookieService service() {
		final UserSettings settings = userSettings();
		final String baseUrl = settings.getBaseUrl();
		BookieService service = BookieService.getService(baseUrl);
		return service;
	}

}