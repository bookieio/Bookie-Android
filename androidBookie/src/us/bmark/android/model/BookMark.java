package us.bmark.android.model;

import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data object for single Bookmark
 */

public class BookMark implements Parcelable {
	public String url;
	public String description;
	public List<String> tags = new LinkedList<String>();
	public String apiHash;

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
	};

	public BookMark(Parcel in) {
		this.description = in.readString();
		this.url = in.readString();
		this.apiHash = in.readString();
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
		dest.writeStringList(tags);
	}
}
