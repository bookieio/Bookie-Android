package us.bmark.android;

import android.os.Bundle;

import us.bmark.android.utils.ErrorHandler;
import us.bmark.bookieclient.BookieService;

public class MineBookmarkListFragment extends BookmarkListFragment {

    @Override
    void refresh() {
        int nextPage = pagesLoaded;
        getActivity().setProgressBarIndeterminateVisibility(true);
        service.recent(settings.getUsername(),
                settings.getApiKey(),
                countPP,
                nextPage,
                new ServiceCallback());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }
}
