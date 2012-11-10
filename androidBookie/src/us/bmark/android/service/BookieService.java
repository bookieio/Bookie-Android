package us.bmark.android.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import us.bmark.android.model.BookMark;
import us.bmark.android.service.NewBookmarkRequest.RequestSuccessListener;
import android.net.Uri;
import android.text.TextUtils;


/**
 * Encapsulates knowledge of the bookie API
 */
public class BookieService {
	private static final String TAGS_DELIMITER = " ";
	private static BookieService singleton; // TODO find something better
	private String baseUrl;

	public BookieService(String uri) {
		super();
		this.baseUrl = uri;
	}

	public String getUri() {
		return baseUrl;
	}


	public void setUri(String uri) {
		this.baseUrl = uri;
	}

	public static BookieService getService() {
		if(singleton==null) singleton = new BookieService("https://bmark.us");
		return singleton;
	}

	public void refreshSystemNewest(int count) {
		GetBookmarksRequest getBookmarksRequest = new GetBookmarksRequest(count);
		getBookmarksRequest.execute(baseUrl);
	}

	public void refreshUserNewest(String user, int count) {
		GetBookmarksRequest getBookmarksRequest = new GetUserBookmarksRequest(user, count);
		getBookmarksRequest.execute(baseUrl);
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
			item.apiHash = jsonBookmark.getString("hash_id");
			bmarks.add(item);
		}
		return bmarks;
	}

	public void saveBookmark(String user, String apiKey, BookMark bmark, RequestSuccessListener listener) {
		NewBookmarkRequest request = new NewBookmarkRequest(user, apiKey, bmark);
		request.registerListener(listener);
		request.execute(baseUrl);
	}

	public static JSONObject JSONifyBookmark(BookMark bmark) {
		JSONObject json = new JSONObject();
		if(bmark!=null) {
			try {
				json.put("url", bmark.url);
				json.put("description", bmark.description);
				json.put("tags", makeTagsValue(bmark.tags));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return json;
	}


	public Uri uriForRedirect(BookMark bmark) {
		return Uri.parse(baseUrl+"/redirect/"+bmark.apiHash);
	}

	private static String makeTagsValue(List<String> tags) {
		return TextUtils.join(TAGS_DELIMITER,tags);
	}



}
