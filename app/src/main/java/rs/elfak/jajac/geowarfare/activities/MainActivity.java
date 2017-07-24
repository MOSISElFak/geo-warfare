package rs.elfak.jajac.geowarfare.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Observable;
import java.util.Observer;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.BaseFragment;
import rs.elfak.jajac.geowarfare.fragments.BuildFragment;
import rs.elfak.jajac.geowarfare.fragments.EditUserInfoFragment;
import rs.elfak.jajac.geowarfare.fragments.FriendsFragment;
import rs.elfak.jajac.geowarfare.fragments.GoldMineFragment;
import rs.elfak.jajac.geowarfare.fragments.MapFragment;
import rs.elfak.jajac.geowarfare.fragments.NoLocationFragment;
import rs.elfak.jajac.geowarfare.fragments.ProfileFragment;
import rs.elfak.jajac.geowarfare.fragments.StructureInfoFragment;
import rs.elfak.jajac.geowarfare.models.FriendModel;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureModel;
import rs.elfak.jajac.geowarfare.models.StructureType;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.utils.LocationSettingObservable;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        FragmentManager.OnBackStackChangedListener,
        EditUserInfoFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        FriendsFragment.OnListFragmentInteractionListener,
        BuildFragment.OnFragmentInteractionListener,
        NoLocationFragment.OnFragmentInteractionListener,
        StructureInfoFragment.OnFragmentInteractionListener,
        GoldMineFragment.OnFragmentInteractionListener,
        Observer {

    public static final int REQUEST_CHECK_SETTINGS = 1;

    private int mFriendRequestsCount = 0;
    private FirebaseUser mUser;
    private FragmentManager mFragmentManager;

    Spinner mFilterSpinner;
    TextView mFriendRequestsCountTv;

    DatabaseReference mMyFriendRequestsDbRef;
    ValueEventListener mFriendRequestsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup default shared preferences if they haven't been setup already
        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.preferences, false);

        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        mUser = firebaseProvider.getCurrentUser();
        mMyFriendRequestsDbRef = firebaseProvider.getFriendRequestsForUser(mUser.getUid());

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
        mFilterSpinner = (Spinner) findViewById(R.id.toolbar_filter_spinner);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(
                this,
                R.layout.toolbar_spinner_selected_item,
                getResources().getStringArray(R.array.filter_array)
        );
        spinAdapter.setDropDownViewResource(R.layout.toolbar_spinner_dropdown_item);
        mFilterSpinner.setAdapter(spinAdapter);

        LocationSettingObservable.getInstance().addObserver(this);
    }

    // Triggered when the user enables/disables Location
    @Override
    public void update(Observable o, Object arg) {
        boolean locationEnabled = (boolean) arg;
        if (locationEnabled) {
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
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, NoLocationFragment.newInstance(),
                        NoLocationFragment.FRAGMENT_TAG)
                .commit();
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

    /**
     * This is called when the up button is pressed. Just pop the back stack.
     */
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
        updateFriendRequestsCount(mFriendRequestsCount);

        // This will set up a listener for incoming friend requests if one isn't already attached
        listenForFriendRequests();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_profile_item:
                onOpenUserProfile(mUser.getUid());
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
        onOpenUserProfile(friendItem.id);
    }

    @Override
    public void onFriendRequestAccept(final FriendModel friend) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.addFriendship(mUser.getUid(), friend.id)
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
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.removeFriendRequest(fromUser.id, mUser.getUid())
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

    private void listenForFriendRequests() {
        if (mFriendRequestsListener == null) {
            // Save the event listener in a variable so we can remove it in onDestroy
            mFriendRequestsListener = mMyFriendRequestsDbRef
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mFriendRequestsCount = (int) dataSnapshot.getChildrenCount();
                            updateFriendRequestsCount(mFriendRequestsCount);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void updateFriendRequestsCount(final int newCount) {
        mFriendRequestsCount = newCount;
        if (mFriendRequestsCountTv == null) return;
        // Call the updating code on the main thread so we can call this asynchronously
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newCount == 0) {
                    mFriendRequestsCountTv.setVisibility(View.INVISIBLE);
                } else {
                    mFriendRequestsCountTv.setText(String.valueOf(newCount));
                    mFriendRequestsCountTv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void onOpenMap() {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, new MapFragment(), MapFragment.FRAGMENT_TAG)
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
    public void onOpenStructure(StructureModel structure) {
        String tag;
        BaseFragment fragment;

        switch (structure.type) {
            case GOLD_MINE:
                fragment = GoldMineFragment.newInstance(structure.id);
                tag = GoldMineFragment.FRAGMENT_TAG;
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

    @Override
    public void onEditFinished() {
        mFragmentManager.popBackStack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager = null;
        mUser = null;
    }

    @Override
    public void onBuildStructureClick(final StructureType structureType) {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        // First we have to get the current user location
        firebaseProvider.getUsersGeoFire().getLocation(mUser.getUid(), new LocationCallback() {
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

        switch (structureType) {
            case GOLD_MINE:
                GoldMineModel newGoldMine = new GoldMineModel(structureType, mUser.getUid());
                firebaseProvider.addGoldMine(newGoldMine, location).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFragmentManager.popBackStack();
                        Toast.makeText(MainActivity.this, "New gold mine constructed.", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    @Override
    public void onNoLocationContinueClick() {
        checkLocationSettings();
    }

    @Override
    public void onOwnerAvatarClick(String ownerUserId) {
        onOpenUserProfile(ownerUserId);
    }
}
