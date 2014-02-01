package us.bmark.android.exception;

public class UnsupportedPlatformException extends RuntimeException {
    public UnsupportedPlatformException(String message, Throwable originalCause) {
        super(message,originalCause);
    }
}
