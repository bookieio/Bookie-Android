package org.bookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.bookie.model.BookMark;
import org.bookie.model.SystemNewest;
import org.bookie.service.BookieService;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
           // When clicked, show a toast with the TextView text
           Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
               Toast.LENGTH_SHORT).show();
         	}
 	    	      });

       SystemNewest systemNewest = SystemNewest.getSystemNewest();
       List<BookMark> bmarks = systemNewest.getList();
       List<String> urls = new ArrayList<String>(bmarks.size());
       for(BookMark item : bmarks) urls.add(item.url);

       ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item, urls);
       setListAdapter(arrayAdapter);

       final ListActivity thiz = this;

       Observer observer = new Observer() {

		@Override
		public void update(Observable observable, Object data) {
			List<BookMark> bmarks = ((SystemNewest)observable).getList();
			List<String> urls = new ArrayList<String>(bmarks.size());
			for(BookMark item : bmarks) urls.add(item.url);

			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(thiz,R.layout.list_item, urls);
			setListAdapter(arrayAdapter);

		}

       };
       systemNewest.addObserver(observer);

       BookieService.getService().refreshSystemNewest();

    }

}