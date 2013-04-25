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

import android.net.http.AndroidHttpClient;


// TODO make a sort of generalized "send json POST/PUT request"

public class NewBookmarkRequest extends AbstractBookieRequest<Boolean> {

	public interface RequestSuccessListener {
		void notify(Boolean requestWasSuccessful);
	}

	private static final String RESTPATH = "bmark";
	private String user;
	private Object apiKey;
	private Set<RequestSuccessListener> registeredSuccessListeners = new HashSet<RequestSuccessListener>();
	private String bmarkAsJson;

	public NewBookmarkRequest(String user, String apiKey, String bmarkAsJson) {
		this.user = user;
		this.apiKey = apiKey;
		this.bmarkAsJson = bmarkAsJson;
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
		final String urlForRequest = endpointUrl + "?api_key=" + apiKey;
		HttpPost postRq = new HttpPost(urlForRequest);

		try {
			final StringEntity stringEntity = new StringEntity(
					bmarkAsJson, "UTF8");

			postRq.setHeader("Content-type", "application/json");
			postRq.setHeader("Accept", "application/json");

			stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
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
