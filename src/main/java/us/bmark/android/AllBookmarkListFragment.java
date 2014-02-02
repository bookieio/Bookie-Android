package us.bmark.android;

import android.os.Bundle;

public class AllBookmarkListFragment extends BookmarkListFragment {

    @Override
    void refresh() {
        int nextPage = pagesLoaded;
        refreshState.setStateInProgress();
        service.everyonesRecent(countPP, nextPage, new ServiceCallback());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }
}
