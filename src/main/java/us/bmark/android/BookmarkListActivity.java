package us.bmark.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;

import retrofit.RestAdapter;
import us.bmark.android.prefs.SettingsActivity;
import us.bmark.android.prefs.SharedPrefsBackedUserSettings;
import us.bmark.android.utils.ErrorHandler;
import us.bmark.android.utils.JustDisplayToastErrorHandler;
import us.bmark.bookieclient.BookieService;

public class BookmarkListActivity extends Activity {

    private static final String TAG = BookmarkListActivity.class.getName();
    private BookieService service;
    private UserSettings settings;
    private String searchTerms;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    private ErrorHandler errorHandler;
    private BookmarkListFragment mineFragment;
    private BookmarkListFragment allFragment;
    private SearchBookmarkFragment searchFragment;

    private enum State {
        ALL, MINE, SEARCH
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new SharedPrefsBackedUserSettings(this);
        errorHandler = new JustDisplayToastErrorHandler(this,settings);
        setUpService();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        createFragments();
        getActionBar().setDisplayHomeAsUpEnabled(false);   // Hides the '<' button in the ActionBar
        getActionBar().setHomeButtonEnabled(true);         // Enables the 'B' icon to be tappable on the list Activity
    }

    private void createFragments() {
        mineFragment = new MineBookmarkListFragment(service,settings,errorHandler);
        allFragment = new AllBookmarkListFragment(service,settings,errorHandler);
        searchFragment = new SearchBookmarkFragment(service,settings,errorHandler);

        FragmentManager manager = getFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, allFragment);
        transaction.add(R.id.fragment_container, mineFragment);
        transaction.add(R.id.fragment_container, searchFragment);
        transaction.hide(mineFragment);
        transaction.hide(searchFragment);
        transaction.show(allFragment);
        transaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpService() {
        String serverUrl = settings.getBaseUrl();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(serverUrl).build();
        restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        service = restAdapter.create(BookieService.class);
    }
//
//    private void refreshWithNewestGlobal() {
//    }
//


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch( item.getItemId() ) {
            case android.R.id.home:
            case R.id.action_everyones_recent:
                Log.v(TAG, "global button clicked");
                flipState(State.ALL);
                return true;
            case R.id.action_recent:
                Log.v(TAG, "user button clicked");
                flipState(State.MINE);
                return true;
            case R.id.action_settings:
                Intent settingsIntent =
                        new Intent(BookmarkListActivity.this, SettingsActivity.class);
                BookmarkListActivity.this.startActivity(settingsIntent);
                return true;
            case R.id.action_search:
                displaySearchDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void flipState(State newState) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(allFragment);
        transaction.hide(mineFragment);
        transaction.hide(searchFragment);
        switch (newState) {
            case MINE:
                transaction.show(mineFragment);
                break;
            case SEARCH:
                transaction.show(searchFragment);
                searchFragment.search(searchTerms);
                break;
            case ALL:
                transaction.show(allFragment);
        }

        transaction.commit();
    }

    private void displaySearchDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.search_dialog_title);
        alert.setMessage(R.string.search_dialog_message);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.search_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                searchTerms = input.getText().toString();
                flipState(State.SEARCH);
            }
        });

        alert.setNegativeButton(R.string.search_dialog_cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing
            }
        });

        alert.show();
    }

}