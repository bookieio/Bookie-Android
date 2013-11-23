package us.bmark.android;

public interface UserSettings {
    public abstract String getUsername();

    public abstract String getApiKey();

    public abstract String getBaseUrl();

    String getParserUrl();
}