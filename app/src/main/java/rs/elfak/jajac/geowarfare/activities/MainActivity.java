package rs.elfak.jajac.geowarfare.activities;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.EditUserInfoFragment;
import rs.elfak.jajac.geowarfare.fragments.FriendsFragment;
import rs.elfak.jajac.geowarfare.fragments.MapFragment;
import rs.elfak.jajac.geowarfare.fragments.ProfileFragment;
import rs.elfak.jajac.geowarfare.models.FriendModel;
import rs.elfak.jajac.geowarfare.providers.UserProvider;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        FragmentManager.OnBackStackChangedListener,
        ProfileFragment.OnFragmentInteractionListener,
        EditUserInfoFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        FriendsFragment.OnListFragmentInteractionListener {

    private int mFriendRequestsCount = 0;

    Spinner mFilterSpinner;
    TextView mFriendRequestsCountTv;

    private FirebaseUser mUser;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

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

    public void setActionBarTitle(String newTitle) {
        ActionBar actionBar = getSupportActionBar();
        if (newTitle != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(newTitle);
        } else {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    private void shouldDisplayHomeUp() {
        boolean canBack = mFragmentManager.getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canBack);
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
        friendsItemView.setOnClickListener(this);
        mFriendRequestsCountTv = (TextView) friendsItemView.findViewById(R.id.friend_requests_count_tv);
        updateFriendRequestsCount(mFriendRequestsCount);
        listenForFriendRequests();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_bar_profile_item:
                onOpenUserProfile(mUser.getUid());
                return true;
            case R.id.action_bar_logout_item:
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(MainActivity.this, LauncherActivity.class);
                startActivity(i);
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
    public void onFriendRequestAccept(final FriendModel item) {
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.addFriendship(mUser.getUid(), item.id)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsFragment friendsFragment = (FriendsFragment) mFragmentManager
                                .findFragmentByTag(FriendsFragment.FRAGMENT_TAG);
                        if (friendsFragment != null) {
                            friendsFragment.removeFriendRequest(item);
                            friendsFragment.addFriend(item);
                        }
                    }
                });
    }

    @Override
    public void onFriendRequestDecline(final FriendModel item) {
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.removeFriendRequest(item.id, mUser.getUid())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsFragment friendsFragment = (FriendsFragment) mFragmentManager
                                .findFragmentByTag(FriendsFragment.FRAGMENT_TAG);
                        if (friendsFragment != null) {
                            friendsFragment.removeFriendRequest(item);
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
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.getFriendRequestsForUser(mUser.getUid()).addValueEventListener(new ValueEventListener() {
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
        mFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, new MapFragment())
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
    protected void onStart() {
        super.onStart();
        setupUI(mUser);
    }

    private void setupUI(FirebaseUser user) {

    }

    @Override
    public void onAddFriend(String userId) {
        // TODO: Send friend request to user with id = userId
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // Since Activity catches result before Fragment does, we pass it along
//        if (requestCode == MapFragment.REQUEST_CHECK_SETTINGS) {
//            mMapFragment.onActivityResult(requestCode, resultCode, data);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager = null;
    }
}
