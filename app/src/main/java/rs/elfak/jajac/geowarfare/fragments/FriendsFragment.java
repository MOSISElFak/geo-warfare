package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.FriendRequestModel;
import rs.elfak.jajac.geowarfare.providers.UserProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mRequestsRecyclerView;

    private List<FriendRequestModel> mRequests = new ArrayList<>();
    private FriendRequestRecyclerViewAdapter mRequestsAdapter;

    private OnListFragmentInteractionListener mListener;

    public interface OnListFragmentInteractionListener {
        void onFriendItemClick(FriendRequestModel item);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {
    }

    @SuppressWarnings("unused")
    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendrequest_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRequestsRecyclerView = (RecyclerView) view;
            mRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider));
            mRequestsRecyclerView.addItemDecoration(divider);
            mRequestsAdapter = new FriendRequestRecyclerViewAdapter(mRequests, mListener);
            mRequestsRecyclerView.setAdapter(mRequestsAdapter);

            getAndSetupFriendRequests();
        }
        return view;
    }

    private void getAndSetupFriendRequests() {
        UserProvider userProvider = UserProvider.getInstance();
        String myUserId = userProvider.getCurrentUser().getUid();

        userProvider.getFriendRequestsForUser(myUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<Map<String, Boolean>> t = new GenericTypeIndicator
                                <Map<String, Boolean>>() {};
                        Map<String, Boolean> requests = dataSnapshot.getValue(t);
                        if (requests != null) {
                            setupFriendRequests(requests.keySet());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setupFriendRequests(Set<String> userIds) {
        UserProvider userProvider = UserProvider.getInstance();
        for (String userId : userIds) {
            userProvider.getUserById(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FriendRequestModel request = dataSnapshot.getValue(FriendRequestModel.class);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                    mRequests.add(request);
                    mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
