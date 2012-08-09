package org.bookie.service;

import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.bookie.model.BookMark;
import org.json.JSONException;

import android.os.AsyncTask;

public abstract class AbstractBookieRequest extends
		AsyncTask<String, String, List<BookMark>> {

	public AbstractBookieRequest() {
		super();
	}

	protected abstract String getResponseString(HttpGet getRq);

	protected abstract List<BookMark> parseStringToGetBookmarkList(String responseString);

	protected abstract String getEndpoint(String baseUrl);

	protected abstract void notifyInterestedParties(List<BookMark> bmarks);

	protected abstract List<BookMark> executeRequest(String uri);

	@Override
	protected List<BookMark> doInBackground(String... params) {
		String uri = params[0];
		return executeRequest(getEndpoint(uri));
	}

	@Override
	protected void onPostExecute(List<BookMark> bmarks) {
		notifyInterestedParties(bmarks);
	}

	protected void handleServerUnexpectedResponseError(JSONException e) {
		e.printStackTrace();
	}

	protected void handleTroubleConnectingError(Exception e) {
		e.printStackTrace();
	}

}