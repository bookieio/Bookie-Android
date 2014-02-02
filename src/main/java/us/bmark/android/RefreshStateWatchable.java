package us.bmark.android;

import com.sun.javafx.beans.annotations.NonNull;

import java.util.Collection;
import java.util.HashSet;

import us.bmark.android.watcher.Watchable;

public class RefreshStateWatchable implements Watchable<Boolean> {

    private boolean inProgress = false;

    private final Collection<Watcher> watchers = new HashSet<Watcher>(1);

    @Override
    public void addWatcher(Watcher watcher) {
        watchers.add(watcher);
    }

    @Override
    public void removeWatcher(Watcher watcher) {
        watchers.remove(watcher);
    }

    public void setStateInProgress() {
        if(!inProgress) {
            inProgress = true;
            showWatchers();
        }
    }

    public void setStateDefault() {
        if(inProgress) {
            inProgress = false;
            showWatchers();
        }
    }


    @NonNull
    @Override
    public Boolean getCurrentValue() {
        return inProgress;
    }

    private void showWatchers() {
        for(Watcher watcher : watchers) {
            watcher.onValueChanged();
        }
    }
}
