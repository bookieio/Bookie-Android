package us.bmark.android;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SearchView;

import us.bmark.android.prefs.SettingsActivity;
import us.bmark.android.watcher.PausableMultiWatcher;

public class BookmarkListsActivity extends FragmentActivity {

    private static final String TAG = BookmarkListsActivity.class.getName();
    private BookmarkListFragment mineFragment;
    private BookmarkListFragment allFragment;
    private SearchBookmarkFragment searchFragment;
    private ViewPager pager;

    final private ActionBar.TabListener tabListener = new ActionBar.TabListener() {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            pager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // do nothing ?
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // do nothing
        }
    };
    private PausableMultiWatcher watcher;
    private BookmarkListsActivity.BookiePagerAdapter tabsPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        pager = (ViewPager) findViewById(R.id.pager);
        createFragments();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);   // Hides the '<' button in the ActionBar
        actionBar.setHomeButtonEnabled(true);         // Enables the 'B' icon to be tappable on the list Activity
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        pager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        watcher.activateObservation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        watcher.pauseObservation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                searchMenuItem.expandActionView();
                return true;
            }
        });

        SearchView searchWidget = (SearchView) searchMenuItem.getActionView();
        searchWidget.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG,"SEARCH");
                pager.setCurrentItem(3);
                searchFragment.refreshWithQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem refreshButton = menu.findItem(R.id.action_refresh);
        refreshButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                refreshActiveFragment();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case android.R.id.home:
                pager.setCurrentItem(0);
            case R.id.action_settings:
                Intent settingsIntent =
                        new Intent(BookmarkListsActivity.this, SettingsActivity.class);
                BookmarkListsActivity.this.startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class BookiePagerAdapter extends FragmentPagerAdapter {
        final Fragment[] fragments = {mineFragment,allFragment,searchFragment};

        private BookiePagerAdapter() {
            super(getSupportFragmentManager());
        }

        public Fragment[] getFragments() {
            return fragments;
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(titleIds[position]);
        }
    }

    private final static int[] iconIds = {R.drawable.fa_tag,R.drawable.fa_tags,R.drawable.ic_action_search};
    private final static int[] titleIds = {R.string.title_mine, R.string.title_all, R.string.title_search};

    private void createFragments() {
        watcher = new RefreshStatePausableMultiWatcher();

        mineFragment = new MineBookmarkListFragment();
        allFragment = new AllBookmarkListFragment();
        searchFragment = new SearchBookmarkFragment();

        watcher.addWatchable(mineFragment.getRefreshState());
        watcher.addWatchable(allFragment.getRefreshState());
        watcher.addWatchable(searchFragment.getRefreshState());

        tabsPagerAdapter = new BookiePagerAdapter();
        pager.setAdapter(tabsPagerAdapter);

        ActionBar actionBar = getActionBar();
        for(int i = 0; i < titleIds.length; i++) {
            ActionBar.Tab tab = actionBar.newTab();
            tab.setText(titleIds[i]);
            tab.setIcon(iconIds[i]);
            tab.setTabListener(tabListener);
            actionBar.addTab(tab);
        }
    }

    private void refreshActiveFragment() {
        BookmarkListFragment activeFragment =
                (BookmarkListFragment) tabsPagerAdapter.getFragments()[pager.getCurrentItem()];
        activeFragment.refresh();
    }

    private class RefreshStatePausableMultiWatcher extends PausableMultiWatcher<Boolean> {
        @Override
        public void onValueChanged() {
            updateProgressBar();
        }

        private void updateProgressBar() {
            setProgressBarIndeterminateVisibility(isAnyInProgress());
        }

        private boolean isAnyInProgress() {
            for(Boolean watchableInProgress : getAllCurrentValues()) {
                if(watchableInProgress) return true;
            }
            return false;
        }
    }
}