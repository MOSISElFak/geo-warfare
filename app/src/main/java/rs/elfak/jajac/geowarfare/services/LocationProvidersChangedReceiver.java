package rs.elfak.jajac.geowarfare.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import rs.elfak.jajac.geowarfare.App;

public class LocationProvidersChangedReceiver extends BroadcastReceiver {

    public static final String TAG = "LocationProviderChanged";

    boolean isGpsEnabled;
    boolean isNetworkEnabled;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            Log.i(TAG, "Location providers changed");

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Try start background service if some location provider is enabled
            if (isGpsEnabled || isNetworkEnabled) {
                Intent backgroundLocationIntent = new Intent(App.getContext(), BackgroundLocationService.class);
                context.startService(backgroundLocationIntent);

                Log.i(TAG, "providers enabled, trying to start service...");
            } else {
                Log.i(TAG, "providers disabled");
            }
        }
    }
}
