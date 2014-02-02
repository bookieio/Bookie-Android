package us.bmark.android.watcher;

import com.sun.javafx.beans.annotations.NonNull;

public interface Watchable <T> {
    interface Watcher {
        void onValueChanged();
    }

    @NonNull
    T getCurrentValue();

    void addWatcher(Watcher watcher);
    void removeWatcher(Watcher watcher);
}
