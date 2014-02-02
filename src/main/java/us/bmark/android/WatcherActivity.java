package us.bmark.android;

import android.support.v4.app.FragmentActivity;

import java.util.Collection;
import java.util.HashSet;

import us.bmark.android.watcher.PausableWatcher;

public class WatcherActivity extends FragmentActivity {

    private Collection<PausableWatcher> managedWatchers = new HashSet<PausableWatcher>();

    public void registerActivityManagedWatcher(PausableWatcher w) {
        managedWatchers.add(w);
    }

    public void removeActivityManagedWatcher(PausableWatcher w) {
        managedWatchers.remove(w);
    }

    @Override
    protected void onResume() {
        super.onResume();
        for(PausableWatcher watcher : managedWatchers) {
            watcher.activateObservation();
        }
    }

    @Override
    protected void onPause() {
        super.onResume();
        for(PausableWatcher watcher : managedWatchers) {
            watcher.pauseObservation();
        }
    }
}
