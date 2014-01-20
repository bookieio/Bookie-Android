package us.bmark.android;

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
    private SearchView searchBox;


    private class RefreshWhenUserHitsSubmit implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            refresh();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false; // no nothing
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchBox = (SearchView) getView().findViewById(R.id.search_box);
        searchBox.setVisibility(View.VISIBLE);
        searchBox.setOnQueryTextListener(new RefreshWhenUserHitsSubmit());
    }

    @Override
    protected void refresh() {
        String terms;
        String searchBoxContents = searchBox.getQuery().toString();
        if(TextUtils.isEmpty(searchBoxContents)) return;

        try {
            terms = encode(searchBoxContents, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported?", e);
            return;
        }
        getActivity().setProgressBarIndeterminateVisibility(true);
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
                        getActivity().setProgressBarIndeterminateVisibility(false);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        getActivity().setProgressBarIndeterminateVisibility(false);
                        errorHandler.handleError(error);
                    }
                });
    }

}
