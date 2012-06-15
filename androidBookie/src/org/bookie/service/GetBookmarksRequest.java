package org.bookie.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bookie.model.BookMark;
import org.json.JSONException;

import android.os.AsyncTask;

public class GetBookmarksRequest extends AsyncTask<String, String, List<String>> {

	@Override
	protected List<String> doInBackground(String... params) {
		String uri = params[0];

       HttpClient client = new DefaultHttpClient();
       HttpGet getRq = new HttpGet(uri);
       List<String> lst = new LinkedList<String>();
	try {
    	   HttpResponse response = client.execute(getRq);
    	   ByteArrayOutputStream out = new ByteArrayOutputStream();
    	   response.getEntity().writeTo(out);
    	   out.close();
    	   String responseString = out.toString();
    	   BookieService service = new BookieService();
    	   List<BookMark> bmarks = service.parseBookmarkListResponse(responseString);
    	   for(BookMark item: bmarks) {
    		   lst .add(item.url);
    	   }
       } catch (ClientProtocolException e) {
    	   e.printStackTrace();
    	   lst.add("Protocol Error");
       } catch (IOException e) {
    	   e.printStackTrace();
    	   lst.add("IO Error");
       } catch (JSONException e) {
    	   e.printStackTrace();
    	   lst.add("JSON Error");
       }

		return lst;
	}


}
