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

	private static BookieService singleton;
	private String uri;

	public BookieService(String uri) {
		super();
		this.uri = uri;
	}


	public String getUri() {
		return uri;
	}


	public void setUri(String uri) {
		this.uri = uri;
	}

	public static BookieService getService() {
		if(singleton==null) singleton = new BookieService("https://bmark.us");
		return singleton;
	}

	public void refreshSystemNewest() {
       GetBookmarksRequest getBookmarksRequest = new GetBookmarksRequest();
       getBookmarksRequest.execute(uri);


	}

	public List<BookMark> parseBookmarkListResponse(String jsonString) throws JSONException {
		// TODO replace exception
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
