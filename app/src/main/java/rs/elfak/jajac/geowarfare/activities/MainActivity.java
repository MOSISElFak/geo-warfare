package rs.elfak.jajac.geowarfare.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
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
import com.tolstykh.textviewrichdrawable.TextViewRichDrawable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rs.elfak.jajac.geowarfare.R;
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
import rs.elfak.jajac.geowarfare.models.FriendModel;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureModel;
import rs.elfak.jajac.geowarfare.models.StructureType;
import rs.elfak.jajac.geowarfare.models.UnitType;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.receivers.LocationProvidersChangedReceiver;
import rs.elfak.jajac.geowarfare.services.UserUpdatesService;

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
        ServiceConnection {

    public static final int REQUEST_CHECK_SETTINGS = 1;

    private FragmentManager mFragmentManager;
    private boolean mUserUpdatesBound = false;
    private UserUpdatesService mUserUpdatesService;

    private String mLoggedUserId;
    private UserModel mLoggedUser;
    private Map<UnitType, TextView> mUnitCountTvs = new HashMap<>();

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
        // Check location settings if nothing is on the stack yet and display
        // MapFragment if they are satisfied or NoLocationFragment if they are not
        if (mFragmentManager.getBackStackEntryCount() < 1) {
            checkLocationSettings();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the user updates service so it starts running if it's not already
        Intent userUpdatesIntent = new Intent(this, UserUpdatesService.class);
        bindService(userUpdatesIntent, this, Context.BIND_AUTO_CREATE);
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
            unbindService(this);
            mUserUpdatesBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager = null;
        mLoggedUserId = null;
    }

    private void drawInfoBarUnits() {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup parentView = (ViewGroup) findViewById(R.id.info_bar_units_container);
        List<UnitType> unitTypes = Arrays.asList(UnitType.values());

        for (UnitType unitType : unitTypes) {
            View view = layoutInflater.inflate(R.layout.info_bar_unit_item, null, false);
            ImageView icon = (ImageView) view.findViewById(R.id.info_bar_units_item_icon);
            TextView countTv = (TextView) view.findViewById(R.id.info_bar_units_item_count);
            icon.setImageResource(unitType.getIconResourceId());
            mUnitCountTvs.put(unitType, countTv);

            parentView.addView(view);
        }
    }

    private void onLocationProvidersChanged(boolean isEnabled) {
        if (isEnabled) {
            onLocationSettingsSatisfied();
        } else {
            onLocationSettingsUnsatisfied();
        }
    }

    // Check if the user's location SETTINGS (not permissions) are satisfying for our LocationRequest
    private void checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
        onOpenMap();
    }

    private void onLocationSettingsUnsatisfied() {
        onOpenNoLocationScreen();
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
        boolean isLoggedUserOwner = structure.ownerId.equals(mLoggedUserId);
        if (isLoggedUserOwner) {
            onOpenMyStructure(structure);
        } else {
            onOpenAttackStructure(structure);
        }
    }

    private void onOpenMyStructure(StructureModel structure) {
        String tag;
        BaseFragment fragment;

        switch (structure.type) {
            case GOLD_MINE:
                fragment = GoldMineFragment.newInstance(structure.id);
                tag = GoldMineFragment.FRAGMENT_TAG;
                break;
            case BARRACKS:
                fragment = BarracksFragment.newInstance(
                        BarracksFragment.class,
                        R.layout.fragment_barracks,
                        BarracksModel.class,
                        structure.id
                );
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
        Toast.makeText(this, "Attack not implemented yet.", Toast.LENGTH_SHORT).show();
    }

    // This is called when a specific structure is chosen, not when the build FAB is clicked
    @Override
    public void onBuildStructureClick(final StructureType structureType) {

        if (mLoggedUser.gold < structureType.getBaseCost()) {
            Toast.makeText(this, getString(R.string.structure_no_gold_message), Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        // First we have to get the current user location
        firebaseProvider.getUsersGeoFire().getLocation(mLoggedUserId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                buildStructure(structureType, location);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void buildStructure(StructureType structureType, GeoLocation location) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        int newUserGoldValue = mLoggedUser.gold - structureType.getBaseCost();
        switch (structureType) {
            case GOLD_MINE:
                GoldMineModel newGoldMine = new GoldMineModel(structureType, mLoggedUserId);
                firebaseProvider.addGoldMine(newGoldMine, location, newUserGoldValue)
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
                BarracksModel newBarracks = new BarracksModel(structureType, mLoggedUserId);
                firebaseProvider.addBarracks(newBarracks, location, newUserGoldValue)
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
        updateGold();
    }

    private void updateFriendRequestsCount() {
        // Exit if for some reason the UI element is not present
        if (mFriendRequestsCountTv == null) return;

        if (mLoggedUser == null || mLoggedUser.friendRequests.size() == 0) {
            mFriendRequestsCountTv.setVisibility(View.INVISIBLE);
        } else {
            mFriendRequestsCountTv.setText(String.valueOf(mLoggedUser.friendRequests.size()));
            mFriendRequestsCountTv.setVisibility(View.VISIBLE);
        }
    }

    private void updateUnitCounts() {
        for (UnitType unitType : UnitType.values()) {
            int count = mLoggedUser.units.get(unitType.toString());
            mUnitCountTvs.get(unitType).setText(String.valueOf(count));
        }
    }

    private void updateGold() {
        mGoldTv.setText(String.valueOf(mLoggedUser.gold));
    }

    @Override
    public void onNoLocationContinueClick() {
        checkLocationSettings();
    }

    @Override
    public void onOwnerAvatarClick(String ownerUserId) {
        onOpenUserProfile(ownerUserId);
    }

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
}
