package us.bmark.android;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import static us.bmark.android.utils.Utils.isBlank;

public class MineBookmarkListFragment extends BookmarkListFragment {

    @Override
    void refresh() {
        if (!userIsLoggedIn()) {
            populateNotLoggedInTextView();
            ListView bookieList = getListView();
            bookieList.setVisibility(View.GONE);
        }
        int nextPage = pagesLoaded;
        refreshState.setStateInProgress();
        service.recent(settings.getUsername(),
                settings.getApiKey(),
                countPP,
                nextPage,
                new ServiceCallback());
    }

    private boolean userIsLoggedIn() {
        return !isBlank(settings.getApiKey()) && !isBlank(settings.getUsername());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    private void populateNotLoggedInTextView() {
        TextView userNotLoggedInMessagae = findView(R.id.userNotLoggedInMessagae);
        userNotLoggedInMessagae.setVisibility(View.VISIBLE);
        userNotLoggedInMessagae.setText(R.string.message_user_not_logged_in);
        userNotLoggedInMessagae.setGravity(Gravity.CENTER);
    }
}
