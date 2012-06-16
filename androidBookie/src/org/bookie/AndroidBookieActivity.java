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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AndroidBookieActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.main);

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

       SystemNewest systemNewest = SystemNewest.getSystemNewest();
       List<BookMark> bmarks = systemNewest.getList();
       List<String> urls = new ArrayList<String>(bmarks.size());
       for(BookMark item : bmarks) urls.add(item.url);

       ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item, urls);
       setListAdapter(arrayAdapter);

       final ListActivity bmarkListActivity = this; // TODO best practice?

       systemNewest.addObserver(new Observer() {
		@Override
		public void update(Observable observable, Object data) {
			List<BookMark> bmarks = ((SystemNewest)observable).getList();
			List<String> urls = new ArrayList<String>(bmarks.size());
			for(BookMark item : bmarks) urls.add(item.url);

			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(bmarkListActivity,R.layout.list_item, urls);
			setListAdapter(arrayAdapter);
		}

       });

       BookieService.getService().refreshSystemNewest();
    }
}