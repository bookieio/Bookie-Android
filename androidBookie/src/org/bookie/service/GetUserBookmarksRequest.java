package org.bookie.service;


public class GetUserBookmarksRequest extends GetBookmarksRequest {
	@Override
	protected String getEndpoint(String baseUrl) {
		String user = getUser();
		return baseUrl + "/" + user + RESTPATH;
	}

	private String getUser() {
		return "";  // FIXME
	}

}
