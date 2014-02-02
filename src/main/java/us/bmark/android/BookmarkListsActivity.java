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

import java.util.ArrayList;
import java.util.Collection;

import us.bmark.android.prefs.SettingsActivity;

public class BookmarkListsActivity extends FragmentActivity {

    private static final String TAG = BookmarkListsActivity.class.getName();
    private BookmarkListFragment mineFragment;
    private BookmarkListFragment allFragment;
    private SearchBookmarkFragment searchFragment;
    private ViewPager pager;

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

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
    private MultiRefreshStateObserver observer;

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
        observer.activateObservation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        observer.pauseObservation();
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

    private class MultiRefreshStateObserver
            implements RefreshStateObservable.RefreshActivityObserver {
        private final Collection<RefreshStateObservable> observables =
                new ArrayList<RefreshStateObservable>();

        void add(BookmarkListFragment fragment) {
            RefreshStateObservable refreshState = fragment.getRefreshState();
            refreshState.addObserver(this);
        }

        void activateObservation() {
            for(RefreshStateObservable observable : observables) {
                observables.add(observable);
            }
            updateProgressBar();
        }

        void pauseObservation() {
            for(RefreshStateObservable observable : observables) {
                observable.removeObserver(this);
            }
        }

        boolean isAnyInProgress() {
            for(RefreshStateObservable observable : observables) {
                if(observable.isInProgress()) return true;
            }
            return false;
        }

        @Override
        public void onChanged() {
            updateProgressBar();
        }

        private void updateProgressBar() {
            setProgressBarVisibility(isAnyInProgress());
        }
    }



    private class BookiePagerAdapter extends FragmentPagerAdapter {
        final Fragment[] fragments = {mineFragment,allFragment,searchFragment};

        private BookiePagerAdapter() {
            super(getSupportFragmentManager());
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

    final static int[] iconIds = {R.drawable.fa_tag,R.drawable.fa_tags,R.drawable.ic_action_search};
    final static int[] titleIds = {R.string.title_mine, R.string.title_all, R.string.title_search};

    private void createFragments() {
        observer = new MultiRefreshStateObserver();
        mineFragment = new MineBookmarkListFragment();
        allFragment = new AllBookmarkListFragment();
        searchFragment = new SearchBookmarkFragment();

        observer.add(mineFragment);
        observer.add(allFragment);
        observer.add(searchFragment);

        pager.setAdapter(new BookiePagerAdapter());

        ActionBar actionBar = getActionBar();
        for(int i = 0; i < titleIds.length; i++) {
            ActionBar.Tab tab = actionBar.newTab();
            tab.setText(titleIds[i]);
            tab.setIcon(iconIds[i]);
            tab.setTabListener(tabListener);
            actionBar.addTab(tab);
        }

    }
}