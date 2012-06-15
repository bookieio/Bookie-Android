package org.bookie;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bookie.service.GetBookmarksRequest;

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


       List<String> lst = new LinkedList<String>();



       lst.add("hello");

       ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item, lst);
       setListAdapter(arrayAdapter);

       GetBookmarksRequest getBookmarksRequest = new GetBookmarksRequest();
       getBookmarksRequest.execute("http://bmark.us/api/v1/bmarks");
       try {
    	   lst = getBookmarksRequest.get();  // FIXME blocks UI


    	   arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item, lst);
    	   setListAdapter(arrayAdapter);
       } catch (InterruptedException e) {
    	   e.printStackTrace();
       } catch (ExecutionException e) {
    	   e.printStackTrace();
       }




    }

}