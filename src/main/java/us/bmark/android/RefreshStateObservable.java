package us.bmark.android;

import java.util.HashSet;
import java.util.Set;

public class RefreshStateObservable {

    private boolean inProgress = false;

    private final Set<RefreshActivityObserver> observers = new HashSet<RefreshActivityObserver>(1);

    public void addObserver(RefreshActivityObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(RefreshActivityObserver observer) {
        observers.remove(observer);
    }

    public void setStateInProgress() {
        if(!inProgress) {
            inProgress = true;
            notifyObservers();
        }
    }

    public void setStateDefault() {
        if(inProgress) {
            inProgress = false;
            notifyObservers();
        }
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public interface RefreshActivityObserver {
        void onChanged();
    }

    private void notifyObservers() {
        for(RefreshActivityObserver observer : observers) {
            observer.onChanged();
        }
    }
}
