package rs.elfak.jajac.geowarfare.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import rs.elfak.jajac.geowarfare.App;
import rs.elfak.jajac.geowarfare.services.BackgroundLocationService;

public class LocationProvidersChangedReceiver extends BroadcastReceiver {

    public static final String TAG = "LocationProviderChanged";

    public static final String PROVIDERS_CHANGED_INTENT_ACTION = "rs.elfak.jajac.geowarfare.providers-changed";
    public static final String PROVIDERS_STATUS_KEY = "is-enabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            Log.i(TAG, "Location providers changed");

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isAnyEnabled = isGpsEnabled || isNetworkEnabled;

            if (isAnyEnabled) {
                Log.i(TAG, "enabled");
                // Try start background service if some location provider is enabled
                Intent backgroundLocationIntent = new Intent(App.getContext(), BackgroundLocationService.class);
                context.startService(backgroundLocationIntent);
            } else {
                Log.i(TAG, "disabled");
                context.stopService(new Intent(App.getContext(), BackgroundLocationService.class));
            }

            // Broadcast a local intent for activities that are interested
            Intent providersChangedLocalIntent = new Intent(PROVIDERS_CHANGED_INTENT_ACTION);
            providersChangedLocalIntent.putExtra(PROVIDERS_STATUS_KEY, isAnyEnabled);
            LocalBroadcastManager.getInstance(context).sendBroadcast(providersChangedLocalIntent);
        }
    }
}
