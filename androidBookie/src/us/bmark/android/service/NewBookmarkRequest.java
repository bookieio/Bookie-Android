package us.bmark.android.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import us.bmark.android.model.BookMark;
import android.net.http.AndroidHttpClient;


// TODO make a sort of generalized "send json POST/PUT request"

public class NewBookmarkRequest extends AbstractBookieRequest<Boolean> {

	public interface RequestSuccessListener {
		void notify(Boolean requestWasSuccessful);
	}

	private static final String RESTPATH = "bmark";
	private String user;
	private Object apiKey;
	private BookMark bmark;
	private Set<RequestSuccessListener> registeredSuccessListeners = new HashSet<RequestSuccessListener>();

	public NewBookmarkRequest(String user, String apiKey, BookMark bmark) {
		this.user = user;
		this.apiKey = apiKey;
		this.bmark = bmark;
	}

	public void registerListener(RequestSuccessListener listener) {
		registeredSuccessListeners.add(listener);
	}

	public void unregisterListener(RequestSuccessListener listener) {
		registeredSuccessListeners.remove(listener);
	}

	@Override
	protected String getEndpoint(String baseUrl) {
		return baseUrl + API_PATH_PREFIX + '/' + user + '/' + RESTPATH;
	}

	@Override
	protected void notifyInterestedParties(Boolean success) {
		for(RequestSuccessListener listener : registeredSuccessListeners) {
			listener.notify(success);
		}
	}

	@Override
	protected Boolean executeRequest(String endpointUrl) {
		boolean success = false;
		final String urlForRequest = endpointUrl + "?api_key="+apiKey;
		HttpPost postRq = new HttpPost(urlForRequest);

		try {
			JSONObject jsonifiedBmark = BookieService.JSONifyBookmark(bmark);
			final StringEntity stringEntity = new StringEntity(jsonifiedBmark.toString(), "UTF8");

			postRq.setHeader("Content-type", "application/json");
	        postRq.setHeader("Accept", "application/json");

	        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			postRq.setEntity(stringEntity);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		}

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
			success = response.getStatusLine().getStatusCode() == 200;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.close();
		return Boolean.valueOf(success);
	}

	@Override
	protected Boolean postProcess(Boolean data) {
		return data;
	}
}
