package rs.elfak.jajac.geowarfare.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.services.BackgroundLocationService;
import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.UserProvider;

public class MapFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String FRAGMENT_TAG = "MapFragment";

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private Context mContext;

    private UserProvider mUserProvider;
    private FirebaseUser mUser;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Circle mCircle;
    private double mRadius = 300;
    private Map<String, Marker> mMarkers = new HashMap<>();
    private Map<Marker, GoogleMap.OnMarkerClickListener> mMarkerListeners = new HashMap<>();
    private Location mMyLocation;

    private GoogleMap.OnMarkerClickListener mUserMarkerListener;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private GeoFire mUsersGeoFire;
    private GeoQuery mUsersGeoQuery;


    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onOpenUserProfile(String userId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserProvider = UserProvider.getInstance();
        mUser = mUserProvider.getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onNewLocation(locationResult);
            }
        };

        mUsersGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("usersGeoFire"));

        mUserMarkerListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                onUserMarkerClick(marker);
                return true;
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_map, container, false);

        FloatingActionButton buildFab = (FloatingActionButton) inflatedView.findViewById(
                R.id.map_fragment_build_button);
        buildFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuildClick();
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
        mGoogleMap.setOnMarkerClickListener(this);
    }

    // Check if the user's location SETTINGS (not permissions) are satisfying for our LocationRequest
    private void checkLocationSettingsAndPermission() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            onLocationSettingsSatisfied();
        }
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
                // User checked "never ask again", show explanation and switch to app settings
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).create().show();
            }
        } else {
            onLocationPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is granted, the results array won't be empty
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
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            mGoogleMap.setMyLocationEnabled(true);


            Intent i = new Intent(mContext, BackgroundLocationService.class);
            mContext.startService(i);
        } catch (SecurityException e) {
            // We're not handling the exception here because this won't be called without permission anyway.
            e.printStackTrace();
        }
    }

    private void onLocationPermissionDenied() {
        // TODO: Handle the map somehow when the user denies location access
    }

    private void addUserGeoQueryEventListener() {
        mUsersGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // We only add markers if the map is loaded and the key is from other users (not ourselves)
                if (mGoogleMap != null && !key.equals(mUser.getUid())) {
                    addUserMarker(key, location);
                }
            }

            @Override
            public void onKeyExited(String key) {
                if (mGoogleMap != null && !key.equals(mUser.getUid())) {
                    Marker marker = mMarkers.get(key);
                    mMarkers.remove(key);
                    mMarkerListeners.remove(marker);
                    marker.remove();
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                if (mGoogleMap != null && !key.equals(mUser.getUid())) {
                    Marker marker = mMarkers.get(key);
                    marker.setPosition(new LatLng(location.latitude, location.longitude));
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void addUserMarker(String userId, GeoLocation location) {
        // Create a (temporary) invisible marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.latitude, location.longitude));
        markerOptions.visible(false);
        markerOptions.anchor(0.5f, 0.5f);
        final Marker marker = mGoogleMap.addMarker(markerOptions);


        mUserProvider.getUserById(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Associate the user data with the marker and add the marker to the HashMaps
                UserModel user = dataSnapshot.getValue(UserModel.class);
                marker.setTag(user);

                mMarkers.put(user.id, marker);
                mMarkerListeners.put(marker, mUserMarkerListener);

                // Load the user avatar and make the marker visible when the picture is in place
                if (mContext != null) {
                    Glide.with(mContext)
                            .load(user.avatarUrl)
                            .asBitmap()
                            .listener(new RequestListener<String, Bitmap>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<Bitmap> target,
                                                           boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap>
                                        target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    Bitmap smallAvatar = Bitmap.createScaledBitmap(resource, 75, 75, false);
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallAvatar));
                                    marker.setVisible(true);
                                    return true;
                                }
                            })
                            .preload();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onBuildClick() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return mMarkerListeners.get(marker).onMarkerClick(marker);
    }

    private void onUserMarkerClick(Marker marker) {
        UserModel user = (UserModel) marker.getTag();
        if (mListener != null) {
            mListener.onOpenUserProfile(user.id);
        }

    }

    private void onNewLocation(LocationResult locationResult) {
        Location loc = locationResult.getLastLocation();
        LatLng center = new LatLng(loc.getLatitude(), loc.getLongitude());
        GeoLocation geoLoc = new GeoLocation(loc.getLatitude(), loc.getLongitude());

        // We need to setup some things only when we receive location for the first time,
        // such as to move camera there, create the circle, starting querying the area...
        if (mMyLocation == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 16.0f));

            mCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .radius(mRadius)
                    .strokeWidth(10)
                    .strokeColor(Color.argb(80, 69, 90, 100))
                    .fillColor(Color.argb(40, 255, 171, 0))
            );

            mUsersGeoQuery = mUsersGeoFire.queryAtLocation(geoLoc, mRadius / 1000);
            addUserGeoQueryEventListener();
        } else {
            mCircle.setCenter(center);
            // Send our new location to the server
            mUsersGeoFire.setLocation(mUser.getUid(), geoLoc);
            // Update the center of the area we're querying for users
            mUsersGeoQuery.setCenter(geoLoc);
        }

        mMyLocation = loc;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        checkLocationSettingsAndPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

        stopLocationUpdates();

        if (mUsersGeoQuery != null) {
            mUsersGeoQuery.removeAllListeners();
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
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

}
