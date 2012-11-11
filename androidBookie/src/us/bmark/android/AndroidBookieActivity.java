package us.bmark.android;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import us.bmark.android.model.BookMark;
import us.bmark.android.model.SystemNewest;
import us.bmark.android.service.BookieService;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class AndroidBookieActivity extends ListActivity {

	private class BookmarkArrayAdapter extends ArrayAdapter<BookMark> {

		private static final int ROW_VIEW_ID = R.layout.list_item;

		public BookmarkArrayAdapter(Context context,List<BookMark> objects) {
			super(context, ROW_VIEW_ID, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = convertView;
	        BookMark bmark = this.getItem(position);

	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
	            row = inflater.inflate(ROW_VIEW_ID, parent, false);
	        }


            TextView textView = (TextView)row.findViewById(R.id.bookmarkListRowTextView);
            textView.setText(bmark.description);

	        return row;

		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<BookMark> arrayAdapter = createPopulatedArrayAdapter();
		setListAdapter(arrayAdapter);
		setContentView(R.layout.main);
		setUpSettingsButton();
		setUpNewestGlobalButton();
		setUpNewestUserButton();
		refreshWithNewestGlobal();
		setUpListView();
	}

	private void refreshWithNewestGlobal() {
		service().refreshSystemNewest(desiredCountForSystemNewest());
	}

	private int desiredCountForSystemNewest() {
		return R.integer.default_number_of_bookmarks_to_get;
	}

	private void refreshWithNewestUser() {
		service().refreshUserNewest(desiredCountForUserNewest());
	}

	private int desiredCountForUserNewest() {
		return R.integer.default_number_of_bookmarks_to_get;
	}

	private ArrayAdapter<BookMark> createPopulatedArrayAdapter() {
		SystemNewest systemNewest = SystemNewest.getSystemNewest();
		List<BookMark> bmarks = systemNewest.getList();

		systemNewest.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				List<BookMark> bmarks = ((SystemNewest)observable).getList();
				ArrayAdapter<BookMark> arrayAdapter = new BookmarkArrayAdapter(AndroidBookieActivity.this, bmarks);
				setListAdapter(arrayAdapter);
			}

		});

		ArrayAdapter<BookMark> arrayAdapter = new BookmarkArrayAdapter(this, bmarks);
		return arrayAdapter;
	}

	private void setUpListView() {
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// open link in browser
				final BookMark bmark = ((BookMark) parent.getAdapter().getItem(position));
				final Uri uri = service().uriForRedirect(bmark);
				Log.i("BMARK CLICK!",uri.toString());
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// open link in browser
				final BookMark bmark = ((BookMark) parent.getAdapter().getItem(position));
				final Bundle bundle = new Bundle();
				bundle.putParcelable("bmark", bmark);
				Log.i("BMARK LONGCLICK!",bmark.description);
				final Intent intent = new Intent(AndroidBookieActivity.this,BookMarkDetailActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				return true;
			}
		});
	}

	private void setUpSettingsButton() {
		Button settingsButton = (Button) findViewById(R.id.settingsButton);

		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent settingsIntent = new Intent(AndroidBookieActivity.this, SettingsActivity.class);
				AndroidBookieActivity.this.startActivity(settingsIntent);
			}
		});
	}

	private void setUpNewestGlobalButton() {
		Button newestGlobalButton = (Button) findViewById(R.id.newestGlobalButton);
		newestGlobalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshWithNewestGlobal();
			}
		});
	}

	private void setUpNewestUserButton() {
		Button newestUserButton = (Button) findViewById(R.id.newestUserButton);
		newestUserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshWithNewestUser();
			}
		});
	}
	private BookieService service() {
		return BookieService.getService(this);
	}
}