package org.bookie.test.service;

import java.util.List;

import junit.framework.TestCase;

import org.bookie.model.BookMark;
import org.bookie.service.BookieService;
import org.json.JSONException;

public class BookieServiceTest extends TestCase {
	
	// from the bookie doc
	private final static String bmarkList1 = 	
			"	{"+
					"    \"count\": 2,"+
					"    \"bmarks\": ["+
					"        {"+
					"            \"username\": \"admin\","+
					"            \"updated\": \"2011-07-29 22:23:42\","+
					"            \"extended\": \"\","+
					"            \"description\": \"Bookie: Recent Bookmarks\","+
					"            \"tags\": ["+
					"                {"+
					"                    \"tid\": 3,"+
					"                    \"name\": \"test\""+
					"                },"+
					"                {"+
					"                    \"tid\": 2,"+
					"                    \"name\": \"bookmarks\""+
					"                }"+
					"            ],"+
					"            \"bid\": 2,"+
					"            \"stored\": \"2011-06-21 13:20:26\","+
					"            \"inserted_by\": null,"+
					"            \"tag_str\": \"test bookmarks\","+
					"            \"clicks\": 1,"+
					"            \"hash_id\": \"c605a21cf19560\","+
					"            \"url\": \"https://bmark.us/recent\","+
					"            \"total_clicks\": 5"+
					"        },"+
					"        {"+
					"            \"username\": \"admin\","+
					"            \"updated\": \"2011-07-15 14:25:16\","+
					"            \"extended\": \"Bookie Documentation Home\","+
					"            \"description\": \"Bookie Website\","+
					"            \"tags\": ["+
					"                {"+
					"                    \"tid\": 2,"+
					"                    \"name\": \"bookmarks\""+
					"                }"+
					"            ],"+
					"            \"bid\": 1,"+
					"            \"stored\": \"2011-06-20 11:42:47\","+
					"            \"inserted_by\": null,"+
					"            \"tag_str\": \"bookmarks\","+
					"            \"clicks\": 1,"+
					"            \"hash_id\": \"c5c21717c99797\","+
					"            \"url\":\"http://docs.bmark.us\","+
					"            \"total_clicks\": 4"+
					"        }"+
					"    ],"+
					"    \"tag_filter\": null,"+
					"    \"page\": 0,"+
					"    \"max_count\": 10"+
					"}";



	public void testParseBookmarkListResponse() throws JSONException {
		BookieService bServ = new BookieService();
			List<BookMark> result = bServ.parseBookmarkListResponse(bmarkList1);
			assertNotNull(result);
			assertFalse(result.isEmpty());
			assertEquals(2, result.size());
			BookMark b1 = result.get(0);
			BookMark b2 = result.get(1);
			
			
			assertEquals("Bookie: Recent Bookmarks", b1.description);
			assertEquals("https://bmark.us/recent", b1.url);
			//TODO tags
			//TODO username
			assertEquals("Bookie Website", b2.description);
			assertEquals("http://docs.bmark.us", b2.url);
		}

}
