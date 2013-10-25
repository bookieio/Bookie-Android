package us.bmark.android;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

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

public class BookmarkListActivity extends ListActivity {

    private BookieService service;
    private UserSettings settings;
    private ListView listView;

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
            textView.setText(bmark.description);

            return row;

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
        service.everyonesRecent(count, 0, new Callback<BookmarkList>() {

            @Override
            public void success(BookmarkList bookmarkList, Response response) {
                List<Bookmark> bmarks = bookmarkList.bmarks;
                Log.w("bmark", "on success global :" + bmarks.size());
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

    private void refreshWithNewestUser() {
        int count = desiredCountForUserNewest();
        service.recent(settings.getUsername(),
                settings.getApiKey(),
                count,
                0,
                new Callback<BookmarkList>() {

            @Override
            public void success(BookmarkList bookmarkList, Response response) {
                List<Bookmark> bmarks = bookmarkList.bmarks;
                Log.w("bmark", "on success user :" + bmarks.size());
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
            default:
                return super.onOptionsItemSelected(item);
        }


    }



}