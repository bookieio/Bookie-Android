package us.bmark.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import retrofit.RetrofitError;
import retrofit.client.Response;
import us.bmark.android.R;
import us.bmark.android.UserSettings;

public class JustDisplayToastErrorHandler implements ErrorHandler {

    static final String TAG = JustDisplayToastErrorHandler.class.getName();
    static final int ERROR_TOAST_DURATION = 3000;

    private final UserSettings settings;
    private final Context ctx;

    public JustDisplayToastErrorHandler(@NotNull Context ctx, @NotNull UserSettings settings) {
        this.ctx = ctx;
        this.settings = settings;
    }

    @Override
    public void handleError(RetrofitError error) {
        Log.w(TAG, "Error received in callback");
        if(error.getCause()!=null)
            Log.w(TAG,error.getCause());
        if(!TextUtils.isEmpty(error.getMessage()))
            Log.w(TAG, error.getMessage());

        if(error.isNetworkError()) {
            handleNetworkError();
        } else if(error.getResponse() != null) {
            handleServerErrorResponse(error);
        } else {
            displayErrorMessage(R.string.error_unknown_error);
        }
    }

    private void handleServerErrorResponse(RetrofitError error) {
        Response response = error.getResponse();
        if(TextUtils.isEmpty(response.getReason())) {
            displayErrorMessage(R.string.error_unknown_error);
        } else {
            String reason = response.getReason();

            String message = ctx.getString(R.string.error_server_format,
                    settings.getBaseUrl(), reason);
            displayErrorMessage(message);
        }
    }

    private void handleNetworkError() {
        if(isNetworkConnected()) {
            displayErrorMessage(R.string.error_network_message);
        } else {
            displayErrorMessage(R.string.error_unconnected_message);
        }
    }

    private boolean isNetworkConnected() {
        Object uncast = ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager connectivity = (ConnectivityManager) uncast;
        return (connectivity.getActiveNetworkInfo() != null) &&
                (connectivity.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED);
    }

    private void displayErrorMessage(int message) {
        Toast.makeText(ctx, message, ERROR_TOAST_DURATION).show();
    }

    private void displayErrorMessage(String message) {
        Toast.makeText(ctx, message, ERROR_TOAST_DURATION).show();
    }
}
