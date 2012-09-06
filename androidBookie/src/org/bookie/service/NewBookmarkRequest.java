package org.bookie.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.bookie.model.BookMark;

import android.net.http.AndroidHttpClient;
import android.util.Log;


public class NewBookmarkRequest extends AbstractBookieRequest<Boolean> {

	private static final String RESTPATH = "bmark";
	private String user;
	private Object apiKey;
	private BookMark bmark;

	public NewBookmarkRequest(String user, String apiKey, BookMark bmark) {
		this.user = user;
		this.apiKey = apiKey;
		this.bmark = bmark;
	}

	@Override
	protected String getEndpoint(String baseUrl) {
		return baseUrl + API_PATH_PREFIX + '/' + user + '/' + RESTPATH;
	}

	@Override
	protected void notifyInterestedParties(Boolean success) {
		// nothing to do
	}

	@Override
	protected Boolean executeRequest(String endpointUrl) {
		boolean success = false;
		final String urlForRequest = endpointUrl + "?api_key="+apiKey;
		HttpPost postRq = new HttpPost(urlForRequest);
		Log.v("NewBookmarkRequest", "request executing to " + urlForRequest);


		try {

			String json =
					"{\"url\":\""
					+ bmark.url
					+ "/\",\"description\":\""
					+ bmark.description
					+ "\"}";
			final StringEntity stringEntity = new StringEntity(json, "UTF8");

	        postRq.setHeader("Content-type", "application/json");

	        postRq.setHeader("Accept", "application/json");
	        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			postRq.setEntity(stringEntity);

		} catch (IOException e) {
		    // TODO Auto-generated catch block
		}

		Log.v("NewBookmarkRequest", postRq.toString());
		HttpResponse response;
		AndroidHttpClient client =  AndroidHttpClient.newInstance("New Bookmark Request");
		try {
			response = client.execute(postRq);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				response = client.execute(postRq);
				response.getEntity().writeTo(out);
				out.close();
				response.getEntity().consumeContent();
			} catch (IOException e) {
				handleTroubleConnectingError(e);
			};
			Log.v("NewBookmarkRequest","response is " + out.toString() );
			Log.v("NewBookmarkRequest","response code is " + response.getStatusLine().getStatusCode());

			Log.v("NewBookmarkRequest","response reason is " + response.getStatusLine().getReasonPhrase().toString() );
			success = response.getStatusLine().getStatusCode() == 200;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.close();
		return new Boolean(success);
	}

	@Override
	protected Boolean postProcess(Boolean data) {
		return data;
	}

}
