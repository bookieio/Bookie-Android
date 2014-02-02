package us.bmark.android.watcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PausableMultiWatcher <T> implements Watchable.Watcher {
    private final Collection<Watchable<T>> watchables =
            new ArrayList<Watchable<T>>();

    public void addWatchable(Watchable<T> watchable) {
        watchables.add(watchable);
    }

    public void activateObservation() {
        for(Watchable<T> watchable : watchables) {
            watchable.addWatcher(this);
        }
    }

    public void pauseObservation() {
        for(Watchable<T> watchable : watchables) {
            watchable.removeWatcher(this);
        }
    }

    public List<T> getAllCurrentValues() {
        List<T> values = new ArrayList<T>(watchables.size());
        for(Watchable<T> watchable : watchables) {
            values.add(watchable.getCurrentValue());
        }
        return values;
    }

}
