package us.bmark.android.watcher;

public interface PausableWatcher extends Watchable.Watcher {
    void activateObservation();
    void pauseObservation();
}
