package org.bookie.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bookie.model.BookMark;
import org.bookie.model.SystemNewest;
import org.json.JSONException;


public class GetBookmarksRequest extends AbstractBookieRequest {

	private final String RESTPATH = "/api/v1/bmarks";

	@Override
	protected List<BookMark> executeRequest(String endpointUrl) {
		HttpGet getRq = new HttpGet(endpointUrl);
		String responseString = getResponseString(getRq);
		return parseStringToGetBookmarkList(responseString);
	}

	@Override
	protected void notifyInterestedParties(List<BookMark> bmarks) {
		SystemNewest.getSystemNewest().updateList(bmarks);
	}

	@Override
	protected String getEndpoint(String baseUrl) {
		return baseUrl + RESTPATH;
	}


	@Override
	protected List<BookMark> parseStringToGetBookmarkList(String responseString) {
		List<BookMark> bmarks = null;
		BookieService service = BookieService.getService();
		try {
			bmarks = service.parseBookmarkListResponse(responseString);
		} catch (JSONException e) {
			handleServerUnexpectedResponseError(e);
		}

		return bmarks;
	}

	@Override
	protected String getResponseString(HttpGet getRq) {
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			response = client.execute(getRq);
			response.getEntity().writeTo(out);
			out.close();
		} catch (IOException e) {
			handleTroubleConnectingError(e);
		}

		String responseString = out.toString();
		return responseString;
	}
}
