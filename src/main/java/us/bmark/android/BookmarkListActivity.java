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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

    private int countPP;
    private BookieService service;
    private UserSettings settings;
    private List<Bookmark> bmarks =
            new ArrayList<Bookmark>();
    private String searchTerms;
    private int pagesLoaded = 0;
    private State state = State.ALL;
    private BookmarkArrayAdapter adapter;

    private enum State {
        ALL, MINE, SEARCH;
    };


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


    private class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold))) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                loadMoreData();
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    private class ServiceCallback implements Callback<BookmarkList> {

        @Override
        public void success(BookmarkList bookmarkList, Response response) {
            bmarks.addAll(bookmarkList.bmarks);
            Log.w("bmark", "on success for bookmark list, fetched " + bmarks.size());
            adapter.notifyDataSetChanged();
            pagesLoaded++;
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
        countPP = getResources().getInteger(R.integer.default_number_of_bookmarks_to_get);
        setUpService();
        setContentView(R.layout.main);
        adapter = new BookmarkArrayAdapter(this,bmarks);
        setListAdapter(adapter);
        setUpListView();
        loadMoreData();
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
        int nextPage = pagesLoaded+1;
        service.everyonesRecent(countPP, nextPage, new ServiceCallback());
    }

    private void refreshWithNewestUser() {
        int nextPage = pagesLoaded+1;
        service.recent(settings.getUsername(),
                settings.getApiKey(),
                countPP,
                nextPage,
                new ServiceCallback());
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

        lv.setOnScrollListener(new EndlessScrollListener());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch( item.getItemId() ) {
            case R.id.action_everyones_recent:
                Log.v("bmark", "glabal bttn clicked");
                flipState(State.ALL);
                return true;
            case R.id.action_recent:
                Log.v("bmark", "user bttn clicked");
                flipState(State.MINE);
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

    private void flipState(State desiredState) {
        this.state = desiredState;
        bmarks = new ArrayList<Bookmark>(countPP);
        adapter = new BookmarkArrayAdapter(this,bmarks);
        setListAdapter(adapter);
        pagesLoaded = 0;
        getListView().setOnScrollListener(new EndlessScrollListener());

        loadMoreData();
    }

    private void displaySearchDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.search_dialog_title);
        alert.setMessage(R.string.search_dialog_message);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                searchTerms = input.getText().toString();
                flipState(State.SEARCH);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing
            }
        });

        alert.show();
    }

    private void refreshWithSearch() {
        String terms = null;
        try {
            terms = encode(searchTerms, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }
        final int nextPage = pagesLoaded+1;
        service.search(settings.getUsername(),settings.getApiKey(),
                terms,countPP,nextPage,
                new Callback<SearchResult>() {

                    @Override
                    public void success(SearchResult searchResult, Response response) {
                        bmarks.addAll(searchResult.search_results);

                        Log.w("bmark", "on success search :" + bmarks.size());
                        adapter.notifyDataSetChanged();
                        pagesLoaded=nextPage;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.w("bmark", error.getMessage());
                        // TODO
                    }
                });
    }


    private void loadMoreData() {
        switch(state) {
            case ALL : refreshWithNewestGlobal(); break;
            case MINE : refreshWithNewestUser(); break;
            case SEARCH : refreshWithSearch(); break;
        }
    }

}