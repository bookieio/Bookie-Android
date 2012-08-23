package org.bookie.service;

import java.util.ArrayList;
import java.util.List;

import org.bookie.model.BookMark;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Encapsulates knowledge of the bookie API
 */
public class BookieService {
	private static BookieService singleton; // TODO find something better
	private String uri;
	private String user;

	public BookieService(String uri, String user) {
		super();
		this.uri = uri;
		this.user = user;
	}


	public String getUri() {
		return uri;
	}


	public void setUri(String uri) {
		this.uri = uri;
	}

	public static BookieService getService() {
		if(singleton==null) singleton = new BookieService("https://bmark.us", "derek");
		return singleton;
	}

	public void refreshSystemNewest() {
		AbstractBookieRequest getBookmarksRequest = new GetBookmarksRequest();
		getBookmarksRequest.execute(uri);
	}

	public void refreshUserNewest() {
		AbstractBookieRequest getBookmarksRequest = new GetUserBookmarksRequest(user);
		getBookmarksRequest.execute(uri);
	}

	public List<BookMark> parseBookmarkListResponse(String jsonString) throws JSONException {
		JSONObject jObj = new JSONObject(jsonString);
		JSONArray jsonBmarks = jObj.getJSONArray("bmarks");
		final int size = jsonBmarks.length();

		List<BookMark> bmarks = new ArrayList<BookMark>(size);
		for(int i = 0; i < size; i++) {
			BookMark item = new BookMark();
			JSONObject jsonBookmark = jsonBmarks.getJSONObject(i);
			item.description = jsonBookmark.getString("description");
			item.url = jsonBookmark.getString("url");
			bmarks.add(item);
		}
		return bmarks;
	}

}
