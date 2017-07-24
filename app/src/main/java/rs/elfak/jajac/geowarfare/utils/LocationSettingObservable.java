package rs.elfak.jajac.geowarfare.utils;

import java.util.Observable;

public class LocationSettingObservable extends Observable {

    private static LocationSettingObservable instance = new LocationSettingObservable();

    public static LocationSettingObservable getInstance() {
        return instance;
    }

    private LocationSettingObservable() {
        // Required for singleton
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

}
