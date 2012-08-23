package org.bookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.bookie.model.BookMark;
import org.bookie.model.SystemNewest;
import org.bookie.service.BookieService;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class AndroidBookieActivity extends ListActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> arrayAdapter = createPopulatedArrayAdapter();
		setListAdapter(arrayAdapter);
		setContentView(R.layout.main);
		setUpSettingsButton();
		setUpNewestGlobalButton();
		setUpNewestUserButton();
		refreshWithNewestGlobal();
		setUpListView();
	}

	private void refreshWithNewestGlobal() {
		BookieService.getService().refreshSystemNewest();
	}

	private void refreshWithNewestUser() {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String user = sharedPreferences.getString("username", null);
		BookieService.getService().refreshUserNewest(user);
	}

	private ArrayAdapter<String> createPopulatedArrayAdapter() {
		SystemNewest systemNewest = SystemNewest.getSystemNewest();
		List<BookMark> bmarks = systemNewest.getList();
		List<String> urls = new ArrayList<String>(bmarks.size());
		for(BookMark item : bmarks) urls.add(item.url);

		systemNewest.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				Log.e("Android Bookie", "system newest observer activated");
				List<BookMark> bmarks = ((SystemNewest)observable).getList();
				List<String> urls = new ArrayList<String>(bmarks.size());
				for(BookMark item : bmarks) urls.add(item.url);

				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AndroidBookieActivity.this,R.layout.list_item, urls);
				setListAdapter(arrayAdapter);
			}

		});

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item, urls);
		return arrayAdapter;
	}

	private void setUpListView() {
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String url = (String) parent.getAdapter().getItem(position);
				// open link in browser
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(browserIntent);

			}
		});
	}

	private void setUpSettingsButton() {
		Button settingsButton = (Button) findViewById(R.id.settingsButton);

		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent settingsIntent = new Intent(AndroidBookieActivity.this, Settings.class);
				AndroidBookieActivity.this.startActivity(settingsIntent);
			}
		});
	}


	private void setUpNewestGlobalButton() {
		Button newestGlobalButton = (Button) findViewById(R.id.newestGlobalButton);
		newestGlobalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("bookie","newest global button pressed");
				refreshWithNewestGlobal();
			}
		});
	}




	private void setUpNewestUserButton() {
		Button newestUserButton = (Button) findViewById(R.id.newestUserButton);
		newestUserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("bookie","newest User button pressed");
				refreshWithNewestUser();
			}
		});
	}
}