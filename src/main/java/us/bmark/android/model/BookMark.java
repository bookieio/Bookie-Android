package us.bmark.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;
import java.util.List;

public class BookMark implements Parcelable {
    public String url;
    public String description;
    public String apiHash;
    public String username;
    public String stored;
    public int totalClicks;
    public int clicks;
    public List<String> tags = new LinkedList<String>();

    public static final Parcelable.Creator<BookMark> CREATOR = new Parcelable.Creator<BookMark>() {
        public BookMark createFromParcel(Parcel in) {
            final BookMark bookMark = new BookMark(in);
            return bookMark;
        }

        public BookMark[] newArray(int size) {
            return new BookMark[size];
        }
    };

    public BookMark() {
    }

    ;

    public BookMark(Parcel in) {
        this.description = in.readString();
        this.url = in.readString();
        this.apiHash = in.readString();
        this.username = in.readString();
        this.stored = in.readString();
        this.totalClicks = in.readInt();
        this.clicks = in.readInt();
        in.readStringList(this.tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(apiHash);
        dest.writeString(username);
        dest.writeString(stored);
        dest.writeInt(totalClicks);
        dest.writeInt(clicks);
        dest.writeStringList(tags);
    }
}
