package rs.elfak.jajac.geowarfare.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.activities.LauncherActivity;

public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    public static final int REQUEST_CHECK_SETTINGS = 1;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Circle mCircle;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onNewLocation(locationResult);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_map, container, false);

        FloatingActionButton buildFab = (FloatingActionButton) inflatedView.findViewById(R.id.map_fragment_build_button);
        buildFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getContext(), LauncherActivity.class);
                startActivity(i);
            }
        });

        mMapView = (MapView) inflatedView.findViewById(R.id.map_fragment_map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        return inflatedView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
    }

    // Check if the user's location SETTINGS (not permissions) are satisfying for our LocationRequest
    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        onLocationSettingsSatisfied();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case CommonStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog to change them.
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) e;
                                    resolvable.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sendEx) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.
                                onLocationSettingsUnsatisfied();
                                break;
                        }
                    }
                });
    }

    private void onLocationSettingsSatisfied() {
        // If all location settings are satisfied, we proceed to check/ask permission.
        checkLocationPermission();
    }

    private void onLocationSettingsUnsatisfied() {
        // TODO: Tell user the app might not perform as expected.
        mCircle.setVisible(false);
    }

    private void checkLocationPermission() {
        final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int userPermission = ContextCompat.checkSelfPermission(getContext(), locationPermission);
        boolean permissionGranted = userPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            // Explain the user why the app requires location permission and then ask for it
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), locationPermission)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{locationPermission}, REQUEST_LOCATION_PERMISSION);
                            }
                        }).create().show();
            } else {
                // User checked "never ask again", show explanation and switch to profile view
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: redirect to profile view or to app settings activity
                            }
                        }).create().show();
            }
        } else {
            onLocationPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is granted, the result arrays won't be empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationPermissionGranted();
                } else {
                    onLocationPermissionDenied();
                }
                return;
            }
        }
    }

    private void onLocationPermissionGranted() {
        // Get the latest location, center map on it, create and show circle and start receiving location updates
        try {
            LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 16.0f));

                            mCircle = mGoogleMap.addCircle(new CircleOptions()
                                    .center(new LatLng(0.0, 0.0))
                                    .radius(300)
                                    .strokeWidth(10)
                                    .strokeColor(Color.argb(80, 69, 90, 100))
                                    .fillColor(Color.argb(40, 255, 87, 34))
                            );
                        }
                    });

            mGoogleMap.setMyLocationEnabled(true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        } catch (SecurityException e) {
            // We're not handling the exception here because this won't be called without permission anyway.
            e.printStackTrace();
        }
    }

    private void onLocationPermissionDenied() {
        // TODO: Handle the map somehow when the user denies location access
        mCircle.setVisible(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            onLocationSettingsSatisfied();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        checkLocationSettings();
    }

    private void onNewLocation(LocationResult locationResult) {
        Location latestLocation = locationResult.getLastLocation();
        LatLng center = new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude());

        if (mCircle != null) {
            mCircle.setCenter(center);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onClick(View v) {

    }
}
