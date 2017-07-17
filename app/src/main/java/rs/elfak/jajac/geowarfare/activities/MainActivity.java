package rs.elfak.jajac.geowarfare.activities;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.MapFragment;
import rs.elfak.jajac.geowarfare.fragments.ProfileFragment;
import rs.elfak.jajac.geowarfare.models.UserModel;

public class MainActivity extends AppCompatActivity implements
        ProfileFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener {

    private int mFriendRequestsCount = 0;

    Spinner mFilterSpinner;
    TextView mFriendRequestsCountTv;

    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mFilterSpinner = (Spinner) findViewById(R.id.toolbar_filter_spinner);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, new MapFragment())
                .commit();

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(
                this,
                R.layout.toolbar_spinner_selected_item,
                getResources().getStringArray(R.array.filter_array)
        );
        spinAdapter.setDropDownViewResource(R.layout.toolbar_spinner_dropdown_item);
        mFilterSpinner.setAdapter(spinAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);

        View friendRequestsView = menu.findItem(R.id.action_friend_requests_item).getActionView();
        mFriendRequestsCountTv = (TextView) friendRequestsView.findViewById(R.id.friend_requests_count_tv);
        updateFriendRequestsCount(mFriendRequestsCount);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_bar_profile_item:
                onOpenUserProfile(mUser.getUid());
                return true;
        }

        return false;
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

    @Override
    public void onOpenUserProfile(String userId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, ProfileFragment.newInstance(userId))
                .addToBackStack(null)
                .commit();
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
}
