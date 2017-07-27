package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;

public class GoldMineFragment extends BaseFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "GoldMineFragment";

    private static final String ARG_STRUCTURE_ID = "structure_id";

    private Context mContext;

    private String mStructureId;
    private GoldMineModel mGoldMine;
    private UserModel mOwner;

    private TextView mCollectAmount;
    private Button mCollectButton;
    private TextView mCurrentLevelIncome;
    private TextView mNextLevelIncome;
    private ImageView mNextLevelCoinsIcon;
    private Button mUpgradeButton;

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

        mCollectAmount = (TextView) view.findViewById(R.id.fragment_gold_mine_collect_amount);
        mCollectButton = (Button) view.findViewById(R.id.fragment_gold_mine_collect_btn);
        mCollectButton.setOnClickListener(this);

        mCurrentLevelIncome = (TextView) view.findViewById(R.id.fragment_gold_mine_upgrade_current);
        mNextLevelIncome = (TextView) view.findViewById(R.id.fragment_gold_mine_upgrade_next);
        mUpgradeButton = (Button) view.findViewById(R.id.fragment_gold_mine_upgrade_btn);
        mUpgradeButton.setOnClickListener(this);

        getStructureDataAndSetupUI(mStructureId);

        return view;
    }

    private void getStructureDataAndSetupUI(String structureId) {
        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.getStructureById(structureId)
                .addValueEventListener(new ValueEventListener() {
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
        updateBasicStructureInfo();

        mCollectAmount.setText(String.valueOf(mGoldMine.gold));

        mCurrentLevelIncome.setText(String.valueOf(mGoldMine.getCurrentIncome()));
        if (mGoldMine.canUpgrade()) {
            mNextLevelIncome.setText(String.valueOf(mGoldMine.getNextIncome()));
            mUpgradeButton.setVisibility(View.VISIBLE);
            mUpgradeButton.setText(String.valueOf(mGoldMine.getUpgradeCost()));
        } else {
            mNextLevelCoinsIcon.setVisibility(View.GONE);
            mNextLevelIncome.setText(getString(R.string.structure_max_level_message));
        }

        updateDefenseInfo();
    }

    private void updateBasicStructureInfo() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        StructureInfoFragment infoFrag = (StructureInfoFragment) childFragmentManager.findFragmentByTag
                (StructureInfoFragment.FRAGMENT_TAG);
        if (infoFrag == null) {
            infoFrag = StructureInfoFragment.newInstance(mGoldMine.type,
                    mGoldMine.level, mOwner.id, mOwner.displayName, mOwner.avatarUrl);
        } else {
            infoFrag.onStructureDataChanged(mGoldMine.level);
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_gold_mine_info_container, infoFrag, StructureInfoFragment.FRAGMENT_TAG)
                .commit();
    }

    private void updateDefenseInfo() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        DefenseFragment defenseFrag = (DefenseFragment) childFragmentManager.
                findFragmentByTag(DefenseFragment.FRAGMENT_TAG);
        if (defenseFrag == null) {
            defenseFrag = DefenseFragment.newInstance(mStructureId, mOwner.id, mGoldMine.defenseUnits, mOwner.units);
        } else {
            defenseFrag.onDefenseDataChanged(mGoldMine.defenseUnits, mOwner.units);
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_gold_mine_defense_container, defenseFrag, DefenseFragment.FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_gold_mine_collect_btn:
                onCollectGold();
                break;
            case R.id.fragment_gold_mine_upgrade_btn:
                onUpgradeGoldMine();
                break;
        }
    }

    private void onCollectGold() {
        if (mGoldMine.gold <= 0) {
            Toast.makeText(mContext, getString(R.string.gold_mine_empty_message), Toast.LENGTH_SHORT).show();
        } else {
            // Remove listener to prevent multiple clicks
            mCollectButton.setOnClickListener(null);

            final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
            final String myUserId = firebaseProvider.getCurrentFirebaseUser().getUid();
            firebaseProvider.getUserGold(myUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int currentGold = dataSnapshot.getValue(int.class);
                            int totalGold = currentGold + mGoldMine.gold;
                            firebaseProvider.transferGold(myUserId, totalGold, mGoldMine.id)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mCollectButton.setOnClickListener(GoldMineFragment.this);
                                            updateBasicStructureInfo();
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void onUpgradeGoldMine() {
        mUpgradeButton.setOnClickListener(null);

        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        final String myUserId = firebaseProvider.getCurrentFirebaseUser().getUid();
        firebaseProvider.getUserGold(myUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int currentGold = dataSnapshot.getValue(int.class);
                        int upgradeCost = mGoldMine.getUpgradeCost();
                        if (currentGold >= upgradeCost) {
                            int newGoldAmount = currentGold = upgradeCost;
                            int newLevel = mGoldMine.level + 1;
                            firebaseProvider.upgradeStructureLevel(myUserId, newGoldAmount, mGoldMine.id, newLevel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mUpgradeButton.setOnClickListener(GoldMineFragment.this);
                                        }
                                    });
                        } else {
                            Toast.makeText(mContext, getString(R.string.structure_no_gold_message),
                                    Toast.LENGTH_SHORT).show();
                            mUpgradeButton.setOnClickListener(GoldMineFragment.this);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.main_menu_group, false);
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

        mContext = context;
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

}
