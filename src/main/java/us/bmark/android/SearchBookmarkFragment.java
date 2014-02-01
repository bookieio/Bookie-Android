package us.bmark.android;

import android.app.Activity;
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
    private static final NetworkActivityListener DO_NOTHING = new NullNetworkActivityListener();


    private String currentQuery = "";
    private NetworkActivityListener progressListener = DO_NOTHING;


    public interface NetworkActivityListener {
        void onNetworkActivityStarted();
        void onNetworkActivityEndedNormally();
        void onNetworkActivityEndedAbnormally();
    }

    public void refreshWithQuery(String query) {
        Log.i(TAG, "refreshWithQuery");
        this.currentQuery = query;
        if(isResumed() && !isDetached())
            refresh();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        progressListener = (NetworkActivityListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        progressListener = DO_NOTHING;
    }

    @Override
    protected void refresh() {
        String searchBoxContents = currentQuery;
        if(TextUtils.isEmpty(searchBoxContents)) return;

        String terms = urlEncode(currentQuery);
        progressListener.onNetworkActivityStarted();
        final int nextPage = pagesLoaded;
        service.search(settings.getUsername(),settings.getApiKey(),
                terms,countPP,nextPage,
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
            if(searchResult.result_count>0) {
                bmarks.addAll(searchResult.search_results);
                ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
                pagesLoaded++;
            }
            progressListener.onNetworkActivityEndedNormally();
        }

        @Override
        public void failure(RetrofitError error) {
            progressListener.onNetworkActivityEndedAbnormally();
            errorHandler.handleError(error);
        }
    }
    private static class NullNetworkActivityListener implements NetworkActivityListener {

        @Override
        public void onNetworkActivityStarted() {
            // do nothing
        }

        @Override
        public void onNetworkActivityEndedNormally() {
            // do nothing
        }

        @Override
        public void onNetworkActivityEndedAbnormally() {
            // do nothing
        }
    }

}
