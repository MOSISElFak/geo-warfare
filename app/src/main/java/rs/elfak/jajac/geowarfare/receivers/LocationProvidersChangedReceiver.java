package rs.elfak.jajac.geowarfare.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import rs.elfak.jajac.geowarfare.App;
import rs.elfak.jajac.geowarfare.services.BackgroundLocationService;
import rs.elfak.jajac.geowarfare.utils.LocationSettingObservable;

public class LocationProvidersChangedReceiver extends BroadcastReceiver {

    public static final String TAG = "LocationProviderChanged";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            Log.i(TAG, "Location providers changed");

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGpsEnabled || isNetworkEnabled) {
                Log.i(TAG, "enabled");
                // Try start background service if some location provider is enabled
                Intent backgroundLocationIntent = new Intent(App.getContext(), BackgroundLocationService.class);
                context.startService(backgroundLocationIntent);

                LocationSettingObservable.getInstance().updateValue(true);
            } else {
                Log.i(TAG, "disabled");
                LocationSettingObservable.getInstance().updateValue(false);
                context.stopService(new Intent(App.getContext(), BackgroundLocationService.class));
            }
        }
    }
}
