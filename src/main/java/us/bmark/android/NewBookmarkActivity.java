package us.bmark.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.prefs.SharedPrefsBackedUserSettings;
import us.bmark.android.views.TagListViewGroup;
import us.bmark.bookieParserClient.BookeParserService.BookieParserService;
import us.bmark.bookieParserClient.BookeParserService.ParseResponse;
import us.bmark.bookieclient.BookieService;
import us.bmark.bookieclient.NewBookmark;
import us.bmark.bookieclient.NewBookmarkResponse;

public class NewBookmarkActivity extends Activity {

    private static final int RESULTS_MESSAGE_DURATION = Toast.LENGTH_SHORT;
    private static final String STATE_TAGS_KEY = "NEW-BOOKMARK-TAGS";
    protected static final CharSequence TAG_SUBSTITUTE_CHARSEQ = "-";
    private static final String TAG = NewBookmarkActivity.class.getName();

    private Set<String> tags = new TreeSet<String>();
    private UserSettings settings;
    private BookieService service;
    private BookieParserService parserService;
    private String url;

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

    private final Callback<NewBookmarkResponse> newBookmarkCallback =
            new Callback<NewBookmarkResponse>() {
                @Override
                public void success(NewBookmarkResponse newBookmarkResponse, Response response) {
                    NewBookmarkActivity.this.requestFinishedWithSuccess();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.w(TAG, error.getMessage());
                    NewBookmarkActivity.this.requestFinishedWithFailure();
                }
            };

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

    private class TagInputFilter implements InputFilter {
        private AlertDialog tagDialog;

        public TagInputFilter(AlertDialog tagDialog) {
            this.tagDialog = tagDialog;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (source instanceof SpannableStringBuilder) {
                Editable sourceAsSpannableBuilder = (Editable) source;
                for (int i = end - 1; i >= start; i--) {
                    char currentChar = source.charAt(i);
                    if (!charIsAllowed(currentChar)) {
                        sourceAsSpannableBuilder.replace(dstart, dend, TAG_SUBSTITUTE_CHARSEQ);
                        visualBell();
                    }

                }
                return source;
            } else {
                StringBuilder filteredStringBuilder = new StringBuilder();
                for (int i = 0; i < end; i++) {
                    char currentChar = source.charAt(i);
                    if (charIsAllowed(currentChar)) {
                        filteredStringBuilder.append(currentChar);
                    } else {
                        filteredStringBuilder.append(TAG_SUBSTITUTE_CHARSEQ);
                        visualBell();
                    }
                }
                return filteredStringBuilder.toString();
            }
        }

        private boolean charIsAllowed(char suspect) {
            return !Character.isSpaceChar(suspect);
        }

        protected void visualBell() {
            tagDialog.setMessage(getString(
                    R.string.button_new_bookmark_new_tag_dialog_text_badchar));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_bookmark);
        settings = new SharedPrefsBackedUserSettings(this);

        dealWithIntents();
        goFetchSuggestedTitleFromReadable();
        setUpSaveButton();
        setUpCancelButton();
        setUpAddTagButton();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        ArrayList<String> tagsArrayList = new ArrayList<String>(tags);
        savedInstanceState.putStringArrayList(STATE_TAGS_KEY, tagsArrayList);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Collection<String> tagsArrayList = savedInstanceState
                .getStringArrayList(STATE_TAGS_KEY);
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
                && (type != null)
                && "text/plain".equals(type);
    }


    private void goFetchSuggestedTitleFromReadable() {
        if (url != null) {
            getParserService().parse(url, new Callback<ParseResponse>() {
                @Override
                public void success(ParseResponse parseResponse, Response response) {
                    if ((parseResponse != null)
                            && (parseResponse.data != null)
                            && !TextUtils.isEmpty(parseResponse.data.title)) {
                        EditText titleField = (EditText) findViewById(R.id.newBookmarkTitleField);
                        titleField.setText(parseResponse.data.title);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    // TODO
                }
            });
        }
    }

    private void setUpSaveButton() {
        Button save = (Button) findViewById(R.id.newBookmarkSaveButton);
        save.setOnClickListener(new SaveButtonListener());
    }

    private void setUpCancelButton() {
        Button save = (Button) findViewById(R.id.newBookmarkCancelButton);
        save.setOnClickListener(new CancelButtonListener());
    }

    private void setUpAddTagButton() {
        Button addTag = (Button) findViewById(R.id.newBookmarkAddTagButton);
        addTag.setOnClickListener(new addTagButtonListener());
    }

    protected void requestFinishedWithFailure() {
        final String message = getString(R.string.new_bookmark_save_failed);
        showMessageAboutResultsOfSave(message);
    }

    protected void requestFinishedWithSuccess() {
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
        bmark.tags = TextUtils.join(" ", tags);
        getBookieService().bookmark(
                settings.getUsername(),
                settings.getApiKey(),
                bmark,
                newBookmarkCallback);
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

        AlertDialog dialog = alert.show();

        aTextField.setFilters(new InputFilter[]{new TagInputFilter(dialog)});
    }

    protected void dissmsissSoftKeyBoard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        final TagListViewGroup tagsView = (TagListViewGroup) findViewById(R.id.tagList);
        tagsView.setTags(tags);
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