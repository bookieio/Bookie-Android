package us.bmark.android;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.utils.ErrorHandler;
import us.bmark.bookieclient.BookieService;
import us.bmark.bookieclient.SearchResult;

import static java.net.URLEncoder.encode;

public class SearchBookmarkFragment extends BookmarkListFragment {

    private static final String TAG = SearchBookmarkFragment.class.getName();
    private String searchTerms;

    public void search(String searchTerms) {
        this.searchTerms = searchTerms;
        refresh();
    }

    @Override
    protected void refresh() {
        String terms;

        if(TextUtils.isEmpty(searchTerms)) return;

        try {
            terms = encode(searchTerms, "UTF-8");
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
