package us.bmark.android;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import us.bmark.android.prefs.SettingsActivity;

public class BookmarkListActivity extends FragmentActivity {

    private static final String TAG = BookmarkListActivity.class.getName();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case android.R.id.home:
                pager.setCurrentItem(0);
            case R.id.action_settings:
                Intent settingsIntent =
                        new Intent(BookmarkListActivity.this, SettingsActivity.class);
                BookmarkListActivity.this.startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private class BookiePagerAdapter extends FragmentPagerAdapter {
        final Fragment[] fragments = {mineFragment,allFragment,searchFragment};

        public BookiePagerAdapter() {
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
        mineFragment = new MineBookmarkListFragment();
        allFragment = new AllBookmarkListFragment();
        searchFragment = new SearchBookmarkFragment();

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