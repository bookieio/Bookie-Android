package org.bookie.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bookie.model.BookMark;
import org.bookie.model.SystemNewest;

import android.os.AsyncTask;

public class GetBookmarksRequest extends AsyncTask<String, String, List<BookMark>> {

	private final String RESTPATH = "/api/v1/bmarks";

	@Override
	protected List<BookMark> doInBackground(String... params) {
		String uri = params[0];

		HttpClient client = new DefaultHttpClient();
		HttpGet getRq = new HttpGet(uri + RESTPATH);
		List<BookMark> bmarks = null;
		try {
			HttpResponse response = client.execute(getRq);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			String responseString = out.toString();
			BookieService service = BookieService.getService();
			bmarks = service.parseBookmarkListResponse(responseString);
		} catch (Exception e) {
			// TODO some sort of error status icon?
					e.printStackTrace();
		}

		return bmarks;
	}


	@Override
	protected void onPostExecute(List<BookMark> bmarks) {
		SystemNewest.getSystemNewest().updateList(bmarks);
	}
}
