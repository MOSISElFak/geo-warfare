package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.activities.MainActivity;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.UserProvider;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private static final int STATUS_MYSELF = 0;
    private static final int STATUS_NOT_FRIEND = 1;
    private static final int STATUS_REQUEST_SENT = 2;
    private static final int STATUS_REQUEST_RECEIVED = 3;
    private static final int STATUS_FRIEND = 4;

    private static final String ARG_USER_ID = "user_id";

    private Context mContext;

    private String mUserId;
    private UserModel mUser;
    private int mStatus;

    // UI elements
    ImageView mAvatarImage;
    TextView mDisplayName;
    TextView mFullName;
    LinearLayout mFriendRequestGroup;
    Button mFriendRequestBtn;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAvatarImage = (ImageView) inflatedView.findViewById(R.id.profile_fragment_avatar_image);
        mDisplayName = (TextView) inflatedView.findViewById(R.id.profile_fragment_display_name);
        mFullName = (TextView) inflatedView.findViewById(R.id.profile_fragment_full_name);
        mFriendRequestGroup = (LinearLayout) inflatedView.findViewById(R.id.profile_fragment_friend_request_group);
        mFriendRequestBtn = (Button) inflatedView.findViewById(R.id.profile_fragment_friend_request_btn);
        mFriendRequestBtn.setCompoundDrawablePadding(16);
        mFriendRequestBtn.setOnClickListener(this);

        getUserDataAndSetupUI(mUserId);

        return inflatedView;
    }

    private void getUserDataAndSetupUI(String userId) {
        UserProvider.getInstance().getUserById(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(UserModel.class);
                        setupUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setupUI() {
        Glide.with(ProfileFragment.this)
                .load(mUser.avatarUrl)
                .into(mAvatarImage);
        mDisplayName.setText(mUser.displayName);
        mFullName.setText(mUser.fullName);

        checkStatusAndSetupFriendRequestButton();
    }

    private void checkStatusAndSetupFriendRequestButton() {
        final String loggedUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (loggedUserId.equals(mUserId)) {
            mStatus = STATUS_MYSELF;
        } else {
            if (mUser.friends.containsKey(loggedUserId)) {
                mStatus = STATUS_FRIEND;
                setupFriendRequestButton();
            } else {
                UserProvider userProvider = UserProvider.getInstance();
                userProvider.getReceivedFriendRequests(loggedUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(mUserId)) {
                                    mStatus = STATUS_REQUEST_RECEIVED;
                                } else if (mUser.friendRequests.containsKey(loggedUserId)) {
                                    mStatus = STATUS_REQUEST_SENT;
                                } else {
                                    mStatus = STATUS_NOT_FRIEND;
                                }
                                setupFriendRequestButton();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        }
    }

    private void setupFriendRequestButton() {
        switch (mStatus) {
            case STATUS_FRIEND:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_unfriend));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.colorPrimaryLight),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                Drawable removeIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_remove_circle_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);
                break;
            case STATUS_REQUEST_SENT:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_cancel));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.colorPrimaryLight),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                Drawable cancelIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_cancel_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(cancelIcon, null, null, null);
                break;
            case STATUS_REQUEST_RECEIVED:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_accept));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.colorAccent),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                Drawable acceptIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_check_circle_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(acceptIcon, null, null, null);
                break;
            case STATUS_NOT_FRIEND:
                mFriendRequestBtn.setText(getString(R.string.profile_friend_request_button_add));
                mFriendRequestBtn.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.colorAccent),
                        PorterDuff.Mode.MULTIPLY
                );
                mFriendRequestBtn.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                Drawable addIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_person_add_24dp);
                mFriendRequestBtn.setCompoundDrawablesWithIntrinsicBounds(addIcon, null, null, null);
                break;
        }
        mFriendRequestGroup.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_fragment_friend_request_btn:
                String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                switch (mStatus) {
                    case STATUS_FRIEND:
                        removeFriendship(myUserId, mUserId);
                        break;
                    case STATUS_REQUEST_SENT:
                        removeFriendRequest(myUserId, mUserId);
                        break;
                    case STATUS_REQUEST_RECEIVED:
                        acceptFriendRequest(mUserId, myUserId);
                        break;
                    case STATUS_NOT_FRIEND:
                        sendFriendRequest(myUserId, mUserId);
                        break;
                }
                break;
        }
    }

    private void sendFriendRequest(String fromUserId, String toUserId) {
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.sendFriendRequest(fromUserId, mUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_REQUEST_SENT;
                        setupFriendRequestButton();
                    }
                });
    }

    private void removeFriendRequest(String fromUserId, String toUserId) {
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.removeFriendRequest(fromUserId, mUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_NOT_FRIEND;
                        setupFriendRequestButton();
                    }
                });
    }

    private void acceptFriendRequest(String fromUserId, String toUserId) {
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.addFriendship(fromUserId, toUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_FRIEND;
                        setupFriendRequestButton();
                    }
                });
    }

    private void removeFriendship(String firstUserId, String secondUserId) {
        UserProvider userProvider = UserProvider.getInstance();
        userProvider.removeFriendship(firstUserId, secondUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mStatus = STATUS_NOT_FRIEND;
                        setupFriendRequestButton();
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }

        mContext = context;
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean shouldShowEditButton = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mUserId);
        menu.findItem(R.id.action_profile_edit_item).setVisible(shouldShowEditButton);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.main_menu_group, false);
        inflater.inflate(R.menu.action_bar_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile_edit_item:
                onEditProfileClick();
                return true;
        }
        return false;
    }

    public void onEditProfileClick() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, EditUserInfoFragment.newInstance(
                        mUser.displayName,
                        mUser.fullName,
                        mUser.phone,
                        mUser.avatarUrl,
                        true
                ))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

}
