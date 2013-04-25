package utils;

import static android.text.TextUtils.isEmpty;
import android.text.TextUtils;

public class Utils {
	public static boolean isBlank(String suspect) {
		return (isEmpty(suspect)) || (suspect.trim().isEmpty());
	}

	public static boolean equalButNotBlank(String lhs,
			String rhs) {
		return !(isBlank(lhs)) && TextUtils.equals(lhs,rhs);
	}
}
