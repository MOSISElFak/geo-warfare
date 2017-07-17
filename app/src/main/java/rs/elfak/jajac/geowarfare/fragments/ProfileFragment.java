package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.UserProvider;

public class ProfileFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_USER_ID = "user_id";

    private String mUserId;
    private UserModel mUser;

    // UI elements
    ImageView mAvatarImage;
    TextView mDisplayName;
    TextView mFullName;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onAddFriend(String userId);
    }

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

        mAvatarImage = (ImageView) inflatedView.findViewById(R.id.profile_fragment_avatar);
        mDisplayName = (TextView) inflatedView.findViewById(R.id.profile_fragment_display_name);
        mFullName = (TextView) inflatedView.findViewById(R.id.profile_fragment_full_name);

        getUserDataAndSetupUI(mUserId);

        return inflatedView;
    }

    private void getUserDataAndSetupUI(String userId) {
        if (mUser != null) {
            setupUI();
        } else {
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
    }

    private void setupUI() {
        Glide.with(ProfileFragment.this)
                .load(mUser.avatarUrl)
                .into(mAvatarImage);
        mDisplayName.setText(mUser.displayName);
        mFullName.setText(mUser.fullName);
    }

    @Override
    public void onClick(View v) {

    }

    public void onAddFriendClick() {
        if (mListener != null) {
            mListener.onAddFriend(mUser.id);
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

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
