package us.bmark.android;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.BaseAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.exception.UnsupportedPlatformException;
import us.bmark.bookieclient.SearchResult;

public class SearchBookmarkFragment extends BookmarkListFragment {
    private static final String TAG = SearchBookmarkFragment.class.getName();

    private String currentQuery = "";

    public void refreshWithQuery(String query) {
        Log.i(TAG, "refreshWithQuery");
        this.currentQuery = query;
        if (isResumed() && !isDetached())
            refresh();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void refresh() {
        String searchBoxContents = currentQuery;
        if (TextUtils.isEmpty(searchBoxContents)) return;

        String terms = urlEncode(currentQuery);
        refreshState.setStateInProgress();
        final int nextPage = pagesLoaded;
        service.search(settings.getUsername(), settings.getApiKey(),
                terms, countPP, nextPage,
                new SearchResultCallback());
    }

    private static String urlEncode(String originalText) {
        try {
            return URLEncoder.encode(originalText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.wtf(TAG, "UTF-8 not supported?", e);
            throw new UnsupportedPlatformException("Wow, so disappoint", e);
        }

    }

    private class SearchResultCallback implements Callback<SearchResult> {

        @Override
        public void success(SearchResult searchResult, Response response) {
            Log.w(TAG, "on success search :" + searchResult.result_count);
            if (searchResult.result_count > 0) {
                bmarks.addAll(searchResult.search_results);
                ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
                pagesLoaded++;
            }
            refreshState.setStateDefault();
        }

        @Override
        public void failure(RetrofitError error) {
            refreshState.setStateDefault();
            errorHandler.handleError(error);
        }
    }
}
