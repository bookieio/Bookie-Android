package us.bmark.android;

import android.os.Bundle;

import us.bmark.android.utils.ErrorHandler;
import us.bmark.bookieclient.BookieService;

public class AllBookmarkListFragment extends BookmarkListFragment {

    public AllBookmarkListFragment(BookieService service, UserSettings settings, ErrorHandler errorHandler) {
        super(service, settings, errorHandler);
    }

    @Override
    void refresh() {
        int nextPage = pagesLoaded;
        getActivity().setProgressBarIndeterminateVisibility(true);
        service.everyonesRecent(countPP, nextPage, new ServiceCallback());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }
}
