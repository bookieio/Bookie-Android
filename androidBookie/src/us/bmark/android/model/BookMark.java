package us.bmark.android.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Data object for single Bookmark
 */


public class BookMark {
	public String url;
	public String description;
	public List<String> tags = new LinkedList<String>();
}
