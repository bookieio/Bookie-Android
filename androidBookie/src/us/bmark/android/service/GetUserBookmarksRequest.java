package us.bmark.android.service;

import java.security.InvalidParameterException;


public class GetUserBookmarksRequest extends GetBookmarksRequest {

	private String user;

	public GetUserBookmarksRequest(String user) {
		super();
		if(user==null) {
			throw new InvalidParameterException("Get User Bookmark requires a user string (received null)");
		}
		this.user = user;
	}

	@Override
	protected String getEndpoint(String baseUrl) {
		return baseUrl + API_PATH_PREFIX +  "/" + user  + RESTPATH;
	}
}
