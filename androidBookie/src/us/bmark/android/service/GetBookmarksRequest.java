package us.bmark.android.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import us.bmark.android.model.BookMark;
import us.bmark.android.model.SystemNewest;


public class GetBookmarksRequest extends AbstractBookieRequest<List<BookMark>> {

	protected final String RESTPATH = "/bmarks";

	private static int DEFAULT_COUNT = 50;

	private int count = DEFAULT_COUNT;

	GetBookmarksRequest(int count) {
		super();
		this.count = count;
	}

	@Override
	protected List<BookMark> executeRequest(String endpointUrl) {
		HttpGet getRq = constructHttpGet(endpointUrl);
		String responseString = getResponseString(getRq);
		return parseStringToGetBookmarkList(responseString);
	}

	private HttpGet constructHttpGet(String endpointUrl) {
		HttpGet getRq = new HttpGet(endpointUrl + "?count=" + count);
		final HttpParams params = getRq.getParams();
		getRq.setParams(params);
		return getRq;
	}

	@Override
	protected void notifyInterestedParties(List<BookMark> bmarks) {
		SystemNewest.getSystemNewest().updateList(bmarks);
	}

	@Override
	protected String getEndpoint(String baseUrl) {
		return baseUrl + API_PATH_PREFIX + RESTPATH;
	}

	protected List<BookMark> parseStringToGetBookmarkList(String responseString) {
		List<BookMark> bmarks = null;
		try {
			bmarks = BookieService.parseBookmarkListResponse(responseString);
		} catch (JSONException e) {
			handleServerUnexpectedResponseError(e);
		}

		return bmarks;
	}

	@Override
	protected List<BookMark> postProcess(final List<BookMark> data) {
		List<BookMark>postprocessed;
		if(data==null) {
			postprocessed = new ArrayList<BookMark>();
		} else {
			postprocessed = data;
		}
		return postprocessed;
	}
}
