package us.bmark.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.prefs.SettingsActivity;
import us.bmark.android.prefs.SharedPrefsBackedUserSettings;
import us.bmark.bookieclient.BookieService;
import us.bmark.bookieclient.BookieServiceUtils;
import us.bmark.bookieclient.Bookmark;
import us.bmark.bookieclient.BookmarkList;
import us.bmark.bookieclient.SearchResult;

import static java.net.URLEncoder.encode;
import static us.bmark.android.utils.Utils.isBlank;

public class BookmarkListActivity extends ListActivity {

    private BookieService service;
    private UserSettings settings;

    private class BookmarkArrayAdapter extends ArrayAdapter<Bookmark> {

        private static final int ROW_VIEW_ID = R.layout.list_item;

        public BookmarkArrayAdapter(Context context, List<Bookmark> objects) {
            super(context, ROW_VIEW_ID, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            Bookmark bmark = this.getItem(position);

            if (row == null) {
                LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
                row = inflater.inflate(ROW_VIEW_ID, parent, false);
            }

            TextView textView = (TextView) row.findViewById(R.id.bookmarkListRowTextView);
            final String description = isBlank(bmark.description) ? bmark.url : bmark.description;
            textView.setText(description);

            return row;
        }
    }


    private class ServiceCallback implements Callback<BookmarkList> {

        @Override
        public void success(BookmarkList bookmarkList, Response response) {
            List<Bookmark> bmarks = bookmarkList.bmarks;
            Log.w("bmark", "on success for bookmark list, fetched " + bmarks.size());
            ListAdapter arrayAdapter =
                    new BookmarkArrayAdapter(BookmarkListActivity.this, bmarks);
            setListAdapter(arrayAdapter);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.w("bmark", error.getMessage());
            // TODO
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new SharedPrefsBackedUserSettings(this);
        setUpService();
        setContentView(R.layout.main);
        refreshWithNewestGlobal();
        setUpListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpService() {
        String serverUrl = settings.getBaseUrl();
        RestAdapter adapter = new RestAdapter.Builder()
                .setServer(serverUrl).build();
        adapter.setLogLevel(RestAdapter.LogLevel.FULL);
        service = adapter.create(BookieService.class);
    }

    private void refreshWithNewestGlobal() {
        int count = desiredCountForSystemNewest();
        service.everyonesRecent(count, 0, new ServiceCallback());
    }

    private void refreshWithNewestUser() {
        int count = desiredCountForUserNewest();
        service.recent(settings.getUsername(),
                settings.getApiKey(),
                count,
                0,
                new ServiceCallback());
    }

    private int desiredCountForSystemNewest() {
        return R.integer.default_number_of_bookmarks_to_get;
    }


    private int desiredCountForUserNewest() {
        return R.integer.default_number_of_bookmarks_to_get;
    }

    private void setUpListView() {
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // open link in browser
                final Bookmark bmark = ((Bookmark) parent.getAdapter().getItem(position));

                final Uri uri = Uri.parse(BookieServiceUtils.urlForRedirect(bmark,
                        settings.getBaseUrl(),
                        settings.getUsername()));
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                final Bookmark bmark = ((Bookmark) parent.getAdapter().getItem(position));
                final Bundle bundle = new Bundle();
                String bmarkJson = (new Gson()).toJson(bmark);
                bundle.putString("bmark", bmarkJson);
                final Intent intent = new Intent(BookmarkListActivity.this,
                        BookMarkDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.action_everyones_recent:
                Log.v("bmark", "glabal bttn clicked");
                refreshWithNewestGlobal();
                return true;
            case R.id.action_recent:
                Log.v("bmark", "user bttn clicked");
                refreshWithNewestUser();
                return true;
            case R.id.action_settings:
                Intent settingsIntent =
                        new Intent(BookmarkListActivity.this, SettingsActivity.class);
                BookmarkListActivity.this.startActivity(settingsIntent);
                return true;
            case R.id.action_search:
                displaySearchDialog();
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void displaySearchDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.search_dialog_title);
        alert.setMessage(R.string.search_dialog_message);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                refreshWithSearch(input.getText().toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing
            }
        });

        alert.show();
    }

    private void refreshWithSearch(String value) {
        String terms = null;
        try {
            terms = encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }
        service.search(settings.getUsername(),settings.getApiKey(),
                terms,desiredCountForUserNewest(),0,
                new Callback<SearchResult>() {

                    @Override
                    public void success(SearchResult searchResult, Response response) {
                        List<Bookmark> bmarks = searchResult.search_results;
                        Log.w("bmark", "on success search :" + bmarks.size());
                        ListAdapter arrayAdapter =
                                new BookmarkArrayAdapter(BookmarkListActivity.this, bmarks);
                        setListAdapter(arrayAdapter);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.w("bmark", error.getMessage());
                        // TODO
                    }
                });
    }


}