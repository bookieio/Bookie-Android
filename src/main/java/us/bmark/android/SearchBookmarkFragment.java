package us.bmark.android;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import java.io.UnsupportedEncodingException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.bookieclient.SearchResult;

import static java.net.URLEncoder.encode;

public class SearchBookmarkFragment extends BookmarkListFragment {


    private static final String TAG = SearchBookmarkFragment.class.getName();


    private String currentQuery = "";
    private NetworkActivityListener progressListener;


    public interface NetworkActivityListener {
        void onNetworkActivityStarted();
        void onNetworkActivityEndedNormally();
        void onNetworkActivityEndedAbnormally();
    }

    public void refreshWithQuery(String query) {
        Log.i(TAG,"refreshWithQuery");
        this.currentQuery = query;
        if(isResumed() & !isDetached())
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
        progressListener = null;
    }

    @Override
    protected void refresh() {
        String terms;
        String searchBoxContents = currentQuery;
        if(TextUtils.isEmpty(searchBoxContents)) return;

        try {
            terms = encode(searchBoxContents, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported?", e);
            return;
        }
        notifyNetworkStarted();
        final int nextPage = pagesLoaded;
        service.search(settings.getUsername(),settings.getApiKey(),
                terms,countPP,nextPage,
                new Callback<SearchResult>() {

                    @Override
                    public void success(SearchResult searchResult, Response response) {
                        Log.w(TAG, "on success search :" + searchResult.result_count);
                        if(searchResult.result_count>0) {
                            bmarks.addAll(searchResult.search_results);
                            ((BookmarkArrayAdapter)getListAdapter()).notifyDataSetChanged();
                            pagesLoaded++;
                        }
                        notifyNetworkActivityEnded();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        notifyNetworkActivityFailed();
                        errorHandler.handleError(error);
                    }
                });
    }

    private void notifyNetworkActivityFailed() {
        if(progressListener!=null) progressListener.onNetworkActivityEndedAbnormally();
    }

    private void notifyNetworkActivityEnded() {
        if(progressListener!=null) progressListener.onNetworkActivityEndedNormally();
    }

    private void notifyNetworkStarted() {
        if(progressListener!=null) progressListener.onNetworkActivityStarted();
    }

}
