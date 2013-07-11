package us.bmark.android.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import us.bmark.android.SharedPrefsBackedUserSettings;
import us.bmark.android.UserSettings;
import us.bmark.android.model.BookMark;
import us.bmark.android.service.NewBookmarkRequest.RequestSuccessListener;

import static android.text.TextUtils.join;
import static us.bmark.android.utils.Utils.equalButNotBlank;


/**
 * Encapsulates knowledge of the bookie API
 */
public class BookieService {
    private static final String TAGS_DELIMITER = " ";
    private String baseUrl;
    private String username;
    private String apiKey;
    private String clientName;

    private BookieService(String uri, String user, String apiKey, String clientName) {
        super();
        this.baseUrl = uri;
        this.username = user;
        this.apiKey = apiKey;
        this.clientName = clientName;
    }


    public static BookieService getService(String baseUrl, String user, String apiKey, String clientName) {
        return new BookieService(baseUrl, user, apiKey, clientName);
    }

    public static BookieService getService(Context context) {
        UserSettings settings = new SharedPrefsBackedUserSettings(context);
        PackageInfo info;
        String clientName = "";
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientName = info.packageName + "_" + info.versionName;
        } catch (NameNotFoundException e) {
            clientName = "SomeBugRiddenAndroidClient";
            e.printStackTrace();
        }
        return getService(settings.getBaseUrl(),
                settings.getUsername(),
                settings.getApiKey(),
                clientName);
    }

    public void refreshSystemNewest(int count) {
        GetBookmarksRequest getBookmarksRequest = new GetBookmarksRequest(count);
        getBookmarksRequest.execute(baseUrl);
    }

    public void refreshUserNewest(int count) {
        GetBookmarksRequest getBookmarksRequest = new GetUserBookmarksRequest(username, count);
        getBookmarksRequest.execute(baseUrl);
    }

    public static List<BookMark> parseBookmarkListResponse(String jsonString) throws JSONException {
        JSONObject jObj = new JSONObject(jsonString);
        JSONArray jsonBmarks = jObj.getJSONArray("bmarks");
        final int size = jsonBmarks.length();

        List<BookMark> bmarks = new ArrayList<BookMark>(size);
        for (int i = 0; i < size; i++) {
            BookMark item = new BookMark();
            JSONObject jsonBookmark = jsonBmarks.getJSONObject(i);
            item.description = jsonBookmark.getString("description");
            item.url = jsonBookmark.getString("url");
            item.apiHash = jsonBookmark.getString("hash_id");
            item.username = jsonBookmark.getString("username");
            item.stored = jsonBookmark.getString("stored");
            item.totalClicks = jsonBookmark.getInt("total_clicks");
            item.clicks = jsonBookmark.getInt("clicks");

            JSONArray jsonTags = jsonBookmark.getJSONArray("tags");

            for (int tagIndex = 0; tagIndex < jsonTags.length(); tagIndex++) {
                item.tags.add(jsonTags.getJSONObject(tagIndex).getString("name"));
            }
            bmarks.add(item);
        }
        return bmarks;
    }

    public void saveBookmark(BookMark bmark, RequestSuccessListener listener) {
        NewBookmarkRequest request = new NewBookmarkRequest(username, apiKey, JSONifyBookmark(bmark).toString());
        request.registerListener(listener);
        request.execute(baseUrl);
    }

    private JSONObject JSONifyBookmark(BookMark bmark) {
        JSONObject json = new JSONObject();
        if (bmark != null) {
            try {
                json.put("url", bmark.url);
                json.put("description", bmark.description);
                json.put("tags", makeTagsValue(bmark.tags));
                json.put("inserted_by", clientName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public Uri uriForRedirect(BookMark bmark) {
        String uriString = baseUrl;
        if (equalButNotBlank(bmark.username, username)) {
            uriString += "/" + username;
        }
        uriString += "/redirect/" + bmark.apiHash;
        return Uri.parse(uriString);
    }

    private static String makeTagsValue(Iterable<String> tags) {
        return join(TAGS_DELIMITER, tags);
    }

}
