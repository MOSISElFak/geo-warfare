package rs.elfak.jajac.geowarfare.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.CoordsModel;
import rs.elfak.jajac.geowarfare.models.StructureModel;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.services.ForegroundLocationService;

public class MapFragment extends BaseFragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    public static final String FRAGMENT_TAG = "MapFragment";

    private Context mContext;
    private FloatingActionButton mBuildButton;

    private FirebaseProvider mFirebaseProvider;
    private FirebaseUser mUser;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Circle mCircle;
    private double mRadius = 300;
    private Map<String, Marker> mMarkers = new HashMap<>();
    private Map<Marker, GoogleMap.OnMarkerClickListener> mMarkerListeners = new HashMap<>();
    private CoordsModel mMyLocation;

    private GoogleMap.OnMarkerClickListener mUserMarkerListener;
    private GoogleMap.OnMarkerClickListener mStructureMarkerListener;

    private GeoQuery mUsersGeoQuery;
    private GeoQuery mStructuresGeoQuery;

    private BroadcastReceiver mUserLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            onNewLocation(new CoordsModel(latitude, longitude));
        }
    };

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onOpenUserProfile(String userId);
        void onOpenStructure(StructureModel structure);
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

        mFirebaseProvider = FirebaseProvider.getInstance();
        mUser = mFirebaseProvider.getCurrentFirebaseUser();

        mUserMarkerListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                onUserMarkerClick(marker);
                return true;
            }
        };

        mStructureMarkerListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                onStructureMarkerClick(marker);
                return true;
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_map, container, false);

        mBuildButton = (FloatingActionButton) inflatedView.findViewById(R.id.map_fragment_build_button);

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

        try {
            mGoogleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            // We're not handling the exception here because we receive locations from the service
            // that will only be enabled if we have location permission
            e.printStackTrace();
        }

        mBuildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuildClick();
            }
        });
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

    private void addStructuresGeoQueryEventListener() {
        mStructuresGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (mGoogleMap != null) {
                    addStructureMarker(key, location);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void addUserMarker(String userId, final GeoLocation location) {
        // Create a (temporary) invisible marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.latitude, location.longitude));
        markerOptions.visible(false);
        markerOptions.anchor(0.5f, 0.5f);
        final Marker marker = mGoogleMap.addMarker(markerOptions);


        mFirebaseProvider.getUserById(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Associate the user data with the marker and add the marker to the HashMaps
                UserModel user = dataSnapshot.getValue(UserModel.class);
                user.setId(dataSnapshot.getKey());

                marker.setTag(user);

                mMarkers.put(user.getId(), marker);
                mMarkerListeners.put(marker, mUserMarkerListener);

                // If the nearby detected user is not a friend, marker shows a simple icon
                if (!user.getFriends().containsKey(mUser.getUid())) {
                    BitmapDescriptor crownIcon = getBitmapFromVector(R.drawable.ic_crown,
                            ContextCompat.getColor(mContext, R.color.colorPrimary));
                    marker.setIcon(crownIcon);
                    marker.setVisible(true);
                } else {
                    // Load the user avatar and make the marker visible when the picture is in place
                    Glide.with(mContext)
                            .load(user.getAvatarUrl())
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

    private void addStructureMarker(final String structureId, final GeoLocation location) {
        // Create a (temporary) invisible marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.latitude, location.longitude));
        markerOptions.visible(false);
        markerOptions.anchor(0.5f, 0.5f);
        final Marker marker = mGoogleMap.addMarker(markerOptions);

        mFirebaseProvider.getStructureById(structureId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Associate the structure data with the marker and add the marker to the HashMaps
                StructureModel structure = dataSnapshot.getValue(StructureModel.class);
                structure.setId(dataSnapshot.getKey());

                marker.setTag(structure);

                mMarkers.put(structure.getId(), marker);
                mMarkerListeners.put(marker, mStructureMarkerListener);

                int iconResourceId = structure.getType().getIconResId();
                int iconColorId = R.color.colorPrimaryDark;
                if (mUser.getUid().equals(structure.getOwnerId())) {
                    iconColorId = R.color.colorPrimaryLight;
                }
                marker.setIcon(getBitmapFromVector(iconResourceId, ContextCompat.getColor(mContext, iconColorId)));
                marker.setVisible(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private BitmapDescriptor getBitmapFromVector(int resourceId, int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), resourceId, null);
        Bitmap bitmap = Bitmap.createBitmap(75, 75, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void onBuildClick() {
        getFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, BuildFragment.newInstance(), BuildFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    private void canBuildOnLocation(Location location) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return mMarkerListeners.get(marker).onMarkerClick(marker);
    }

    private void onUserMarkerClick(Marker marker) {
        UserModel user = (UserModel) marker.getTag();
        if (mListener != null) {
            mListener.onOpenUserProfile(user.getId());
        }

    }

    private void onStructureMarkerClick(Marker marker) {
        StructureModel structure = (StructureModel) marker.getTag();
        if (mListener != null) {
            mListener.onOpenStructure(structure);
        }
    }

    private void onNewLocation(CoordsModel loc) {
        LatLng center = new LatLng(loc.getLatitude(), loc.getLongitude());
        GeoLocation geoLoc = new GeoLocation(loc.getLatitude(), loc.getLongitude());

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        GeoFire usersGeoFire = firebaseProvider.getUsersGeoFire();
        GeoFire structuresGeoFire = firebaseProvider.getStructuresGeoFire();

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

            mUsersGeoQuery = usersGeoFire.queryAtLocation(geoLoc, mRadius / 1000);
            mStructuresGeoQuery = structuresGeoFire.queryAtLocation(geoLoc, mRadius / 1000);
            addUserGeoQueryEventListener();
            addStructuresGeoQueryEventListener();
        } else {
            mCircle.setCenter(center);
            // Update the center of the area we're querying for users
            mUsersGeoQuery.setCenter(geoLoc);
            mStructuresGeoQuery.setCenter(geoLoc);
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

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        // Register a receiver for any changes in the user data (eg. from ForegroundLocationService)
        localBroadcastManager.registerReceiver(mUserLocationReceiver,
                new IntentFilter(ForegroundLocationService.USER_LOCATION_UPDATED_INTENT_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        // Unregister the receivers in onPause because we can guarantee its execution
        localBroadcastManager.unregisterReceiver(mUserLocationReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

        mMyLocation = null;

        if (mCircle != null) {
            mCircle.remove();
            mCircle = null;
        }

        if (mUsersGeoQuery != null) {
            mUsersGeoQuery.removeAllListeners();
        }
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
