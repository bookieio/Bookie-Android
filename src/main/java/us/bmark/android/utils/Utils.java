package us.bmark.android.utils;

import android.text.TextUtils;

import static android.text.TextUtils.isEmpty;

public class Utils {
    public static boolean isBlank(String suspect) {
        return (isEmpty(suspect)) || (suspect.trim().isEmpty());
    }

    public static boolean equalButNotBlank(String lhs,
                                           CharSequence rhs) {
        return !(isBlank(lhs)) && TextUtils.equals(lhs, rhs);
    }
}
