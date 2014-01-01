package us.bmark.android.utils;

import android.content.Context;

import retrofit.RetrofitError;

public interface ErrorHandler {
    public void handleError(RetrofitError error);
}
