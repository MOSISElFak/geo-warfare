package rs.elfak.jajac.geowarfare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.BuildFragment;
import rs.elfak.jajac.geowarfare.fragments.EditUserInfoFragment;
import rs.elfak.jajac.geowarfare.fragments.FriendsFragment;
import rs.elfak.jajac.geowarfare.fragments.MapFragment;
import rs.elfak.jajac.geowarfare.fragments.ProfileFragment;
import rs.elfak.jajac.geowarfare.models.FriendModel;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureType;
import rs.elfak.jajac.geowarfare.providers.UserProvider;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        FragmentManager.OnBackStackChangedListener,
        EditUserInfoFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        FriendsFragment.OnListFragmentInteractionListener,
        BuildFragment.OnFragmentInteractionListener {

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

        // Setup default shared preferences if they haven't been setup already
        PreferenceManager.setDefaultValues(MainActivity.this,
                R.xml.preferences, false);

        UserProvider userProvider = UserProvider.getInstance();
        mUser = userProvider.getCurrentUser();
        mMyFriendRequestsDbRef = userProvider.getFriendRequestsForUser(mUser.getUid());

        mFragmentManager = getSupportFragmentManager();
        // Display map as the initial fragment if nothing is on the stack yet
        if (mFragmentManager.getBackStackEntryCount() < 1) {
            onOpenMap();
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
    }

    // A public title setter so different fragments can set their own title
    public void setActionBarTitle(String newTitle) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (newTitle != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(newTitle);
            } else {
                actionBar.setDisplayShowTitleEnabled(false);
            }
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
        switch(item.getItemId()) {
            case R.id.action_bar_profile_item:
                onOpenUserProfile(mUser.getUid());
                return true;
            case R.id.action_bar_settings_item:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_bar_logout_item:
                UserProvider.getInstance().getAuthInstance().signOut();
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
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.addFriendship(mUser.getUid(), friend.id)
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
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.removeFriendRequest(fromUser.id, mUser.getUid())
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
    public void onBuildStructure(StructureType structureType) {
        if (structureType == StructureType.GOLD_MINE) {
            GoldMineModel newGoldMine = new GoldMineModel(structureType.getName(), mUser.getUid());
            UserProvider.getInstance().addGoldMine(newGoldMine).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFragmentManager.popBackStack();
                }
            });
        }
    }

}
