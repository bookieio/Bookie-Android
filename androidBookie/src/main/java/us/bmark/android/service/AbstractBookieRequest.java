package us.bmark.android.service;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractBookieRequest<T> extends
        AsyncTask<String, String, T> {

    protected static final String APIKEY_PARAM_KEY = "api_key";
    protected final String API_PATH_PREFIX = "/api/v1";

    public AbstractBookieRequest() {
        super();
    }

    protected abstract String getEndpoint(String baseUrl);

    protected abstract void notifyInterestedParties(T content);

    protected abstract T executeRequest(String uri);

    protected abstract T postProcess(T data);

    protected void handleServerUnexpectedResponseError(JSONException e) {
        e.printStackTrace();
    }

    protected void handleTroubleConnectingError(Exception e) {
        e.printStackTrace();
    }

    protected String getResponseString(HttpUriRequest rq) {
        HttpResponse response;
        HttpClient client = new DefaultHttpClient();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            response = client.execute(rq);
            response.getEntity().writeTo(out);
            out.close();
            response.getEntity().consumeContent();
        } catch (IOException e) {
            handleTroubleConnectingError(e);
        }

        String responseString = out.toString();
        return responseString;
    }

    @Override
    protected T doInBackground(String... params) {
        String uri = params[0];
        return executeRequest(getEndpoint(uri));
    }

    @Override
    protected void onPostExecute(final T data) {
        T postprocessedData = postProcess(data);
        notifyInterestedParties(postprocessedData);
    }


}