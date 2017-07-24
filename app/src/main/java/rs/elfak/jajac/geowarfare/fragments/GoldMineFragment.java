package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureType;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;

public class GoldMineFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "GoldMineFragment";

    private static final String ARG_STRUCTURE_ID = "structure_id";

    private String mStructureId;
    private GoldMineModel mGoldMine;
    private UserModel mOwner;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    public GoldMineFragment() {
        // Required empty public constructor
    }

    public static GoldMineFragment newInstance(String structureId) {
        GoldMineFragment fragment = new GoldMineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STRUCTURE_ID, structureId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStructureId = getArguments().getString(ARG_STRUCTURE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gold_mine, container, false);

        getStructureDataAndSetupUI(mStructureId);

        return view;
    }

    private void getStructureDataAndSetupUI(String structureId) {
        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.getStructureById(structureId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mGoldMine = dataSnapshot.getValue(GoldMineModel.class);
                        mGoldMine.id = dataSnapshot.getKey();
                        firebaseProvider.getUserById(mGoldMine.ownerId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        mOwner = dataSnapshot.getValue(UserModel.class);
                                        mOwner.id = dataSnapshot.getKey();
                                        setupUI();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setupUI() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.structure_info_container, StructureInfoFragment.newInstance(mGoldMine.type,
                        mGoldMine.level, mOwner.id, mOwner.displayName, mOwner.avatarUrl), StructureInfoFragment
                        .FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.menu.action_bar_main_menu, false);
        super.onCreateOptionsMenu(menu, inflater);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
