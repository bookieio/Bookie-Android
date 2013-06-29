package us.bmark.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import us.bmark.android.R.id;
import us.bmark.android.model.BookMark;
import us.bmark.android.service.BookieService;
import us.bmark.android.service.NewBookmarkRequest.RequestSuccessListener;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class NewBookmarkActivity extends Activity {

    private static final int RESULTS_MESSAGE_DURATION = Toast.LENGTH_SHORT;
    private static final String STATE_TAGS_KEY = "NEW-BOOKMARK-TAGS";
    protected static final CharSequence TAG_SUBSTITUTE_CHARSEQ = "-";

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
            if (requestWasSuccessful) {
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

    private class TagInputFilter implements InputFilter {
        private AlertDialog tagDialog;

        public TagInputFilter(AlertDialog tagDialog) {
            this.tagDialog = tagDialog;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (source instanceof SpannableStringBuilder) {
                SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
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

    ;

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
        savedInstanceState.putStringArrayList(STATE_TAGS_KEY, tagsArrayList);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> tagsArrayList = savedInstanceState
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
        String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        EditText uriField = (EditText) findViewById(id.newBookmarkUrlField);
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
        BookMark bmark = new BookMark();
        bmark.url = ((EditText) findViewById(id.newBookmarkUrlField)).getText().toString();
        bmark.description = ((EditText) findViewById(id.newBookmarkTitleField)).getText().toString();
        bmark.tags.addAll(tags);
        service().saveBookmark(bmark, NewBookmarkActivity.this.produceListenerForRequest());
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
        final TableLayout table = (TableLayout) findViewById(id.newBookmarkTabTable);
        table.removeAllViews();

        for (String tagText : tags) {
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

    private BookieService service() {
        return BookieService.getService(this);
    }

}