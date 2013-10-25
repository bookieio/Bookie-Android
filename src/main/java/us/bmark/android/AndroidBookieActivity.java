package us.bmark.android;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.model.BookMark;
import us.bmark.bookieclient.BookieService;
import us.bmark.bookieclient.BookieServiceUtils;
import us.bmark.bookieclient.Bookmark;
import us.bmark.bookieclient.BookmarkList;
import us.bmark.bookieclient.Tag;

public class AndroidBookieActivity extends ListActivity {

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
        setUpSettingsButton();
        setUpNewestGlobalButton();
        setUpNewestUserButton();
        refreshWithNewestGlobal();
        setUpListView();
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
                        new BookmarkArrayAdapter(AndroidBookieActivity.this, bmarks);
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
                        new BookmarkArrayAdapter(AndroidBookieActivity.this, bmarks);
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
                // open link in browser
                final Bookmark bmark = ((Bookmark) parent.getAdapter().getItem(position));
                final Bundle bundle = new Bundle();


                // TODO -- be gone
                BookMark bMark = new BookMark();
                bMark.description = bmark.description;
                bMark.apiHash = bmark.hash_id;
                bMark.clicks = bmark.clicks;
                bMark.stored = bmark.stored;
                bMark.url = bmark.url;
                bMark.username = bmark.username;
                for (Tag tag : bmark.tags) bMark.tags.add(tag.name);

                bundle.putParcelable("bmark", bMark);
                final Intent intent = new Intent(AndroidBookieActivity.this,
                        BookMarkDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
    }

    private void setUpSettingsButton() {
        Button settingsButton = (Button) findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent =
                        new Intent(AndroidBookieActivity.this, SettingsActivity.class);
                AndroidBookieActivity.this.startActivity(settingsIntent);
            }
        });
    }

    private void setUpNewestGlobalButton() {
        Button newestGlobalButton = (Button) findViewById(R.id.newestGlobalButton);
        newestGlobalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v("bmark", "glabal bttn clicked");
                refreshWithNewestGlobal();
            }
        });
    }

    private void setUpNewestUserButton() {
        Button newestUserButton = (Button) findViewById(R.id.newestUserButton);
        newestUserButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("bmark", "user bttn clicked");
                refreshWithNewestUser();
            }
        });
    }


}