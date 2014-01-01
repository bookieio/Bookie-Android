package us.bmark.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.prefs.SharedPrefsBackedUserSettings;
import us.bmark.android.utils.ErrorHandler;
import us.bmark.android.utils.JustDisplayToastErrorHandler;
import us.bmark.bookieParserClient.BookeParserService.BookieParserService;
import us.bmark.bookieParserClient.BookeParserService.ParseResponse;
import us.bmark.bookieclient.BookieService;
import us.bmark.bookieclient.NewBookmark;
import us.bmark.bookieclient.NewBookmarkResponse;

public class NewBookmarkActivity extends Activity {

    private static final int RESULTS_MESSAGE_DURATION = Toast.LENGTH_SHORT;
    private static final String STATE_TAGS_KEY = "NEW-BOOKMARK-TAGS";
    private static final String TAG = NewBookmarkActivity.class.getName();

    private UserSettings settings;
    private BookieService service;
    private BookieParserService parserService;
    private String url;
    private ErrorHandler errorHandler;

    private final class DismissLater implements Runnable {
        @Override
        public void run() {
            final NewBookmarkActivity thisActivity = NewBookmarkActivity.this;
            thisActivity.finish();
        }
    }

    private final Callback<NewBookmarkResponse> newBookmarkCallback =
            new Callback<NewBookmarkResponse>() {
                @Override
                public void success(NewBookmarkResponse newBookmarkResponse, Response response) {
                    NewBookmarkActivity.this.requestFinishedWithSuccess();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.w(TAG, error.getMessage());
                    errorHandler.handleError(error);
                    NewBookmarkActivity.this.requestFinishedWithFailure();
                }
            };

    private final class SaveButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            saveButtonWasClicked();
        }
    }

    private class CancelButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            cancelButtonWasClicked();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_bookmark);
        settings = new SharedPrefsBackedUserSettings(this);
        errorHandler = new JustDisplayToastErrorHandler(this,settings);
        dealWithIntents();
        goFetchSuggestedTitleFromReadable();
        setUpSaveButton();
        setUpCancelButton();
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
                && (type != null)
                && "text/plain".equals(type);
    }


    private void goFetchSuggestedTitleFromReadable() {
        final ProgressBar titleFetchProgressBar = view(R.id.newBookmarkTitleProgressBar);

        if (url != null) {
            titleFetchProgressBar.setVisibility(View.VISIBLE);
            getParserService().parse(url, new Callback<ParseResponse>() {
                @Override
                public void success(ParseResponse parseResponse, Response response) {
                    if ((parseResponse != null)
                            && (parseResponse.data != null)
                            && !TextUtils.isEmpty(parseResponse.data.title)) {
                        EditText titleField = (EditText) findViewById(R.id.newBookmarkTitleField);
                        titleField.setText(parseResponse.data.title);
                        titleFetchProgressBar.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    titleFetchProgressBar.setVisibility(View.INVISIBLE);
                    // TODO
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T  view(int id) {
        return (T) findViewById(id);
    }

    private void setUpSaveButton() {
        Button save = (Button) findViewById(R.id.newBookmarkSaveButton);
        save.setOnClickListener(new SaveButtonListener());
    }

    private void setUpCancelButton() {
        Button save = (Button) findViewById(R.id.newBookmarkCancelButton);
        save.setOnClickListener(new CancelButtonListener());
    }

    void requestFinishedWithFailure() {
        final String message = getString(R.string.new_bookmark_save_failed);
        showMessageAboutResultsOfSave(message);
    }

    void requestFinishedWithSuccess() {
        final String message = getString(R.string.new_bookmark_save_success);
        showMessageAboutResultsOfSave(message);
        dismissThisActivity(RESULTS_MESSAGE_DURATION);
    }

    private void showMessageAboutResultsOfSave(CharSequence message) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, RESULTS_MESSAGE_DURATION);
        toast.show();
    }

    private void handleSendText(Intent intent) {
        url = intent.getStringExtra(Intent.EXTRA_TEXT);
        EditText uriField = (EditText) findViewById(R.id.newBookmarkUrlField);
        uriField.setText(url);
    }

    private void dismissThisActivity(int millisecDelay) {
        if (millisecDelay > 0) {
            Handler handler = new Handler();
            final Runnable dismissLater = new DismissLater();
            handler.postDelayed(dismissLater, millisecDelay);
        } else {
            finish();
        }
    }


    private void saveButtonWasClicked() {
        NewBookmark bmark = new NewBookmark();

        bmark.url = ((TextView) findViewById(R.id.newBookmarkUrlField)).getText().toString();
        bmark.description = ((TextView) findViewById(R.id.newBookmarkTitleField))
                .getText().toString();
        bmark.tags =  ((TextView) findViewById(R.id.newBookmarkTagsField)).getText().toString();
        getBookieService().bookmark(
                settings.getUsername(),
                settings.getApiKey(),
                bmark,
                newBookmarkCallback);
    }

    void cancelButtonWasClicked() {
        dismissThisActivity(0);
    }

    void dissmsissSoftKeyBoard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private BookieService getBookieService() {
        if (service == null) {
            String serverUrl = settings.getBaseUrl();
            RestAdapter adapter = new RestAdapter.Builder()
                    .setServer(serverUrl).build();
            adapter.setLogLevel(RestAdapter.LogLevel.FULL); // TODO loglevel should be set globally
            service = adapter.create(BookieService.class);
        }
        return service;
    }

    private BookieParserService getParserService() {
        if (parserService == null) {
            RestAdapter adapter = new RestAdapter.Builder()
                    .setServer(settings.getParserUrl()).build();
            adapter.setLogLevel(RestAdapter.LogLevel.FULL); // TODO loglevel should be set globally
            parserService = adapter.create(BookieParserService.class);
        }
        return parserService;
    }

}