package rs.elfak.jajac.geowarfare.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rs.elfak.jajac.geowarfare.Constants;
import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.AttackFragment;
import rs.elfak.jajac.geowarfare.fragments.BarracksFragment;
import rs.elfak.jajac.geowarfare.fragments.BaseFragment;
import rs.elfak.jajac.geowarfare.fragments.BuildFragment;
import rs.elfak.jajac.geowarfare.fragments.DefenseFragment;
import rs.elfak.jajac.geowarfare.fragments.EditUserInfoFragment;
import rs.elfak.jajac.geowarfare.fragments.FriendsFragment;
import rs.elfak.jajac.geowarfare.fragments.GoldMineFragment;
import rs.elfak.jajac.geowarfare.fragments.MapFragment;
import rs.elfak.jajac.geowarfare.fragments.NoLocationFragment;
import rs.elfak.jajac.geowarfare.fragments.ProfileFragment;
import rs.elfak.jajac.geowarfare.fragments.StructureFragment;
import rs.elfak.jajac.geowarfare.fragments.StructureInfoFragment;
import rs.elfak.jajac.geowarfare.models.BarracksModel;
import rs.elfak.jajac.geowarfare.models.CoordsModel;
import rs.elfak.jajac.geowarfare.models.FriendModel;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureModel;
import rs.elfak.jajac.geowarfare.models.StructureType;
import rs.elfak.jajac.geowarfare.models.UnitType;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.receivers.LocationProvidersChangedReceiver;
import rs.elfak.jajac.geowarfare.services.ForegroundLocationService;
import rs.elfak.jajac.geowarfare.services.UserUpdatesService;
import rs.elfak.jajac.geowarfare.utils.NumTextView;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        FragmentManager.OnBackStackChangedListener,
        EditUserInfoFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        FriendsFragment.OnListFragmentInteractionListener,
        BuildFragment.OnFragmentInteractionListener,
        NoLocationFragment.OnFragmentInteractionListener,
        StructureFragment.OnFragmentInteractionListener,
        StructureInfoFragment.OnFragmentInteractionListener,
        DefenseFragment.OnFragmentInteractionListener,
        GoldMineFragment.OnFragmentInteractionListener,
        BarracksFragment.OnFragmentInteractionListener,
        AttackFragment.OnFragmentInteractionListener {

    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int REQUEST_LOCATION_PERMISSION = 2;

    private FragmentManager mFragmentManager;

    private boolean mUserUpdatesBound = false;
    private UserUpdatesService mUserUpdatesService;

    private boolean mUserLocationsBound = false;
    private ForegroundLocationService mUserLocationService;

    private String mLoggedUserId;
    private UserModel mLoggedUser;
    private Map<UnitType, NumTextView> mUnitCountTvs = new HashMap<>();

    TextView mGoldTv;
    Spinner mFilterSpinner;
    TextView mFriendRequestsCountTv;

    private BroadcastReceiver mUserUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onUserDataUpdated();
        }
    };
    private BroadcastReceiver mLocationProvidersChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isEnabled = intent.getBooleanExtra(LocationProvidersChangedReceiver.PROVIDERS_STATUS_KEY, false);
            onLocationProvidersChanged(isEnabled);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mGoldTv = (TextView) findViewById(R.id.info_gold_tv);
        mFilterSpinner = (Spinner) findViewById(R.id.toolbar_filter_spinner);

        // Draw the units part of the info bar on the bottom
        drawInfoBarUnits();

        // Setup default shared preferences if they haven't been setup already
        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.preferences, false);

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        mLoggedUserId = firebaseProvider.getCurrentFirebaseUser().getUid();

        mFragmentManager = getSupportFragmentManager();

        // Monitor the backstack in order to show/hide the back button
        mFragmentManager.addOnBackStackChangedListener(this);
        shouldDisplayHomeUp();

        // Initialize the action bar spinner for filtering map markers
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(
                this,
                R.layout.toolbar_spinner_selected_item,
                getResources().getStringArray(R.array.filter_array)
        );
        spinAdapter.setDropDownViewResource(R.layout.toolbar_spinner_dropdown_item);
        mFilterSpinner.setAdapter(spinAdapter);
    }

    // Check if the user's location SETTINGS (not permissions) are satisfying for our LocationRequest
    private void checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(Constants.LOCATION_REQUEST_PRIORITY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        onLocationSettingsSatisfied();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case CommonStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog to change them.
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) e;
                                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                onLocationSettingsSatisfied();
            } else {
                onLocationSettingsUnsatisfied();
            }
        }
    }

    private void onLocationSettingsSatisfied() {
        checkLocationPermission();
    }

    private void onLocationSettingsUnsatisfied() {
        onOpenNoLocationScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        // Register a receiver for any changes in the user data (eg. from UserUpdatesService)
        localBroadcastManager.registerReceiver(mUserUpdatesReceiver,
                new IntentFilter(UserUpdatesService.USER_UPDATED_INTENT_ACTION));
        // Register a receiver for any changes in location providers (eg. from LocationProvidersChangedReceiver)
        localBroadcastManager.registerReceiver(mLocationProvidersChangedReceiver,
                new IntentFilter(LocationProvidersChangedReceiver.PROVIDERS_CHANGED_INTENT_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        // Unregister the receivers in onPause because we can guarantee its execution
        localBroadcastManager.unregisterReceiver(mUserUpdatesReceiver);
        localBroadcastManager.unregisterReceiver(mLocationProvidersChangedReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from any services this activity is bound to
        if (mUserUpdatesBound) {
            unbindService(mUserUpdatesConnection);
            mUserUpdatesBound = false;
        }
        if (mUserLocationsBound) {
            unbindService(mUserLocationConnection);
            mUserLocationsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager = null;
        mLoggedUserId = null;
    }

    private void checkLocationPermission() {
        final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int userPermission = ContextCompat.checkSelfPermission(this, locationPermission);
        boolean permissionGranted = userPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            // Explain the user why the app requires location permission and then ask for it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, locationPermission)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{locationPermission},
                                REQUEST_LOCATION_PERMISSION);
                            }
                        }).create().show();
            } else {
                // User checked "never ask again", show explanation and switch to app settings
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
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
            }
        }
    }

    private void onLocationPermissionGranted() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(Constants.LOCATION_REQUEST_PRIORITY);

        Intent userLocationIntent = new Intent(this, ForegroundLocationService.class);
        bindService(userLocationIntent, mUserLocationConnection, Context.BIND_AUTO_CREATE);
        Intent userUpdatesIntent = new Intent(this, UserUpdatesService.class);
        bindService(userUpdatesIntent, mUserUpdatesConnection, Context.BIND_AUTO_CREATE);

        // If there's nothing on the stack (no fragment loaded), load map
        if (mFragmentManager.getBackStackEntryCount() < 1) {
            onOpenMap();
        }
    }

    private void onLocationPermissionDenied() {
        // Treat denied location permission as if the device location is disabled
        onOpenNoLocationScreen();
    }

    private void onLocationProvidersChanged(boolean isEnabled) {
        if (isEnabled) {
            onLocationSettingsSatisfied();
        } else {
            onLocationSettingsUnsatisfied();
        }
    }

    private void drawInfoBarUnits() {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup parentView = (ViewGroup) findViewById(R.id.info_bar_units_container);
        List<UnitType> unitTypes = Arrays.asList(UnitType.values());

        for (UnitType unitType : unitTypes) {
            View view = layoutInflater.inflate(R.layout.info_bar_unit_item, null, false);
            ImageView icon = (ImageView) view.findViewById(R.id.info_bar_units_item_icon);
            NumTextView countTv = (NumTextView) view.findViewById(R.id.info_bar_units_item_count);
            icon.setImageResource(unitType.getIconResourceId());
            mUnitCountTvs.put(unitType, countTv);

            parentView.addView(view);
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    private void shouldDisplayHomeUp() {
        boolean canBack = mFragmentManager.getBackStackEntryCount() > 0;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(canBack);
        }
    }

    // This is called when the up button is pressed. Just pop the back stack.
    @Override
    public boolean onSupportNavigateUp() {
        mFragmentManager.popBackStack();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_main_menu, menu);

        View friendsItemView = menu.findItem(R.id.action_friends_item).getActionView();
        // We need to manually set the click listener for our custom options item
        // because we have used the "actionLayout" parameter in the xml
        friendsItemView.setOnClickListener(this);
        mFriendRequestsCountTv = (TextView) friendsItemView.findViewById(R.id.friend_requests_count_tv);
        updateFriendRequestsCount();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_profile_item:
                onOpenUserProfile(mLoggedUserId);
                return true;
            case R.id.action_bar_settings_item:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_bar_logout_item:
                FirebaseProvider.getInstance().getAuthInstance().signOut();
                Intent logoutIntent = new Intent(MainActivity.this, LauncherActivity.class);
                startActivity(logoutIntent);
                finish();
                return true;
        }

        // If none of the 'case' statements return true, we return false to let a specific fragment handle the option
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_friends_item:
                onOpenFriends();
                break;
        }
    }

    @Override
    public void onFriendItemClick(FriendModel friendItem) {
        onOpenUserProfile(friendItem.userId);
    }

    @Override
    public void onFriendRequestAccept(final FriendModel friend) {
        FirebaseProvider.getInstance().addFriendship(mLoggedUserId, friend.userId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsFragment friendsFragment = (FriendsFragment) mFragmentManager
                                .findFragmentByTag(FriendsFragment.FRAGMENT_TAG);
                        if (friendsFragment != null) {
                            friendsFragment.removeFriendRequest(friend);
                            friendsFragment.addFriend(friend);
                        }
                    }
                });
    }

    @Override
    public void onFriendRequestDecline(final FriendModel fromUser) {
        FirebaseProvider.getInstance().removeFriendRequest(fromUser.userId, mLoggedUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsFragment friendsFragment = (FriendsFragment) mFragmentManager
                                .findFragmentByTag(FriendsFragment.FRAGMENT_TAG);
                        if (friendsFragment != null) {
                            friendsFragment.removeFriendRequest(fromUser);
                        }
                    }
                });
    }

    private void onOpenFriends() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, FriendsFragment.newInstance(), FriendsFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void onOpenMap() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, new MapFragment(), MapFragment.FRAGMENT_TAG)
                .commit();
    }

    private void onOpenNoLocationScreen() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, NoLocationFragment.newInstance(),
                        NoLocationFragment.FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onOpenUserProfile(String userId) {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, ProfileFragment.newInstance(userId))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onEditFinished() {
        mFragmentManager.popBackStack();
    }

    @Override
    public void onOpenStructure(StructureModel structure) {
        boolean isLoggedUserOwner = structure.getOwnerId().equals(mLoggedUserId);
        if (isLoggedUserOwner) {
            onOpenMyStructure(structure);
        } else {
            onOpenAttackStructure(structure);
        }
    }

    private void onOpenMyStructure(StructureModel structure) {
        String tag;
        StructureFragment fragment;

        switch (structure.getType()) {
            case GOLD_MINE:
                fragment = GoldMineFragment.newInstance(
                        GoldMineFragment.class,
                        R.layout.fragment_gold_mine,
                        GoldMineModel.class,
                        structure.getId());
                tag = GoldMineFragment.FRAGMENT_TAG;
                break;
            case BARRACKS:
                fragment = BarracksFragment.newInstance(
                        BarracksFragment.class,
                        R.layout.fragment_barracks,
                        BarracksModel.class,
                        structure.getId());
                tag = BarracksFragment.FRAGMENT_TAG;
                break;
            default:
                // TODO: add some empty frag or something
                tag = "";
                fragment = null;
        }

        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    private void onOpenAttackStructure(StructureModel structure) {
        String tag;
        Class modelClass = null;

        switch (structure.getType()) {
            case GOLD_MINE:
                modelClass = GoldMineModel.class;
                tag = GoldMineFragment.FRAGMENT_TAG;
                break;
            case BARRACKS:
                modelClass = BarracksModel.class;
                tag = BarracksFragment.FRAGMENT_TAG;
                break;
            default:
                // TODO: add some empty frag or something
                tag = "";
        }

        AttackFragment fragment = AttackFragment.newInstance(
                AttackFragment.class, R.layout.fragment_attack, modelClass, structure.getId()
        );

        fragment.setUnitCounts(mLoggedUser.getUnits());

        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    // This is called when a specific structure is chosen, not when the build FAB is clicked
    @Override
    public void onBuildStructureClick(final StructureType structureType) {

        if (mLoggedUser.getGold() < structureType.getBaseCost()) {
            Toast.makeText(this, getString(R.string.structure_no_gold_message), Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        // We can use the constantly-updated mLoggedUser to get the location for building
        buildStructure(structureType, mLoggedUser.getCoords());
    }

    private void buildStructure(StructureType structureType, CoordsModel coords) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        int newUserGoldValue = mLoggedUser.getGold() - structureType.getBaseCost();
        switch (structureType) {
            case GOLD_MINE:
                GoldMineModel newGoldMine = new GoldMineModel(structureType, mLoggedUserId, coords);
                firebaseProvider.addGoldMine(newGoldMine, coords, newUserGoldValue)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFragmentManager.popBackStack();
                                Toast.makeText(MainActivity.this, "New gold mine constructed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
            case BARRACKS:
                BarracksModel newBarracks = new BarracksModel(structureType, mLoggedUserId, coords);
                firebaseProvider.addBarracks(newBarracks, coords, newUserGoldValue)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFragmentManager.popBackStack();
                                Toast.makeText(MainActivity.this, "New barracks constructed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }

    private void onUserDataUpdated() {
        mLoggedUser = mUserUpdatesService.getUser();
        updateFriendRequestsCount();
        updateUnitCounts();
        updateGoldAmount();
    }

    private void updateFriendRequestsCount() {
        // Exit if for some reason the UI element is not present
        if (mFriendRequestsCountTv == null) return;

        if (mLoggedUser == null || mLoggedUser.getFriendRequests().size() == 0) {
            mFriendRequestsCountTv.setVisibility(View.INVISIBLE);
        } else {
            mFriendRequestsCountTv.setText(String.valueOf(mLoggedUser.getFriendRequests().size()));
            mFriendRequestsCountTv.setVisibility(View.VISIBLE);
        }
    }

    private void updateUnitCounts() {
        for (UnitType unitType : UnitType.values()) {
            int count = mLoggedUser.getUnitCount(unitType);
            mUnitCountTvs.get(unitType).setText(String.valueOf(count));
        }
    }

    private void updateGoldAmount() {
        mGoldTv.setText(String.valueOf(mLoggedUser.getGold()));
    }

    @Override
    public void onNoLocationContinueClick() {
        checkLocationSettings();
    }

    @Override
    public void onOwnerAvatarClick(String ownerUserId) {
        onOpenUserProfile(ownerUserId);
    }

    private ServiceConnection mUserUpdatesConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UserUpdatesService.LocalBinder binder = (UserUpdatesService.LocalBinder) service;
            mUserUpdatesService = binder.getService();
            mUserUpdatesBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUserUpdatesBound = false;
        }
    };

    private ServiceConnection mUserLocationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundLocationService.LocalBinder binder = (ForegroundLocationService.LocalBinder) service;
            mUserLocationService = binder.getService();
            mUserLocationsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUserLocationsBound = false;
        }
    };

    @Override
    public void onAttackFinished() {
        mFragmentManager.popBackStack();
    }
}
