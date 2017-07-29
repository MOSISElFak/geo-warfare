package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Constructor;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.StructureModel;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;

public abstract class StructureFragment extends BaseFragment implements View.OnClickListener {

    private static final String ARG_STRUCTURE_SPECIFIC_LAYOUT_RES_ID = "structure_specific_layout_res_id";
    private static final String ARG_STRUCTURE_CLASS = "structure_class";
    private static final String ARG_STRUCTURE_ID = "structure_id";

    protected Context mContext;

    private int mSpecificLayoutResId;
    private Class<? extends StructureModel> mStructureClass;
    private String mStructureId;

    protected StructureModel mStructure;
    protected UserModel mOwner;

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

    public StructureFragment() {
        // Required empty public constructor
    }

    public static <T extends StructureFragment> T newInstance(
            Class<T> fragmentClass,
            int specificLayoutResId,
            Class<? extends StructureModel> structureClass,
            String structureId) {

        Constructor<T> c = null;
        try {
            c = fragmentClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(fragmentClass.getSimpleName()
                    + " must have a constructor that StructureFragment can fetch!");
        }
        T fragment = null;
        try {
            fragment = c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(fragmentClass.getSimpleName()
                    + " newInstance() breaks when called from StructureFragment!");
        }
        Bundle args = new Bundle();
        args.putInt(ARG_STRUCTURE_SPECIFIC_LAYOUT_RES_ID, specificLayoutResId);
        args.putString(ARG_STRUCTURE_ID, structureId);
        args.putString(ARG_STRUCTURE_CLASS, structureClass.getCanonicalName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSpecificLayoutResId = getArguments().getInt(ARG_STRUCTURE_SPECIFIC_LAYOUT_RES_ID);
            mStructureId = getArguments().getString(ARG_STRUCTURE_ID);
            try {
                mStructureClass = (Class<? extends StructureModel>) Class.forName
                        (getArguments().getString(ARG_STRUCTURE_CLASS));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(getClass().getSimpleName() +
                        " onCreate failed, can't find structure class!");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the skeleton layout for a structure fragment
        View view = inflater.inflate(R.layout.fragment_structure, container, false);

        // Inflate the structure-specific layout
        ViewGroup structureSpecificContainer = (ViewGroup) view.findViewById(R.id
                .fragment_structure_specific_container);
        inflater.inflate(mSpecificLayoutResId, structureSpecificContainer);

        // Polymorphism will handle drawing the view
        drawStructureSpecificView(view);

        // The upgrade part is already inflated from the skeleton so we proceed
        drawStructureUpgradeView(view);

        mUpgradeButton = (Button) view.findViewById(R.id.fragment_structure_upgrade_btn);

        getStructureDataAndSetupUI();

        return view;
    }

    abstract void drawStructureSpecificView(View fragmentView);
    abstract void drawStructureUpgradeView(View fragmentView);
    abstract void updateSpecificStructureInfo();

    private void getStructureDataAndSetupUI() {
        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.getStructureById(mStructureId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mStructure = dataSnapshot.getValue(mStructureClass);
                        mStructure.id = dataSnapshot.getKey();
                        firebaseProvider.getUserById(mStructure.ownerId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        mOwner = dataSnapshot.getValue(UserModel.class);
                                        mOwner.id = dataSnapshot.getKey();
                                        setupUIValues();
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

    private void setupUIValues() {
        // We implement this here
        updateBasicStructureInfo();

        // This is implemented in specific fragments
        updateSpecificStructureInfo();

        // This is also shared so it's implemented here
        updateUpgradeButton();

        if (mStructure.type.hasDefense()) {
            // We implement this here
            updateDefenseInfo();
        }
    }

    private void updateBasicStructureInfo() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        StructureInfoFragment infoFrag = (StructureInfoFragment) childFragmentManager.findFragmentByTag
                (StructureInfoFragment.FRAGMENT_TAG);
        if (infoFrag == null) {
            infoFrag = StructureInfoFragment.newInstance(mStructure.type,
                    mStructure.level, mOwner.id, mOwner.displayName, mOwner.avatarUrl);
        } else {
            infoFrag.onStructureDataChanged(mStructure.level);
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_structure_info_container, infoFrag, StructureInfoFragment.FRAGMENT_TAG)
                .commit();
    }

    private void updateUpgradeButton() {
        if (mStructure.canUpgrade()) {
            mUpgradeButton.setText(String.valueOf(mStructure.getUpgradeCost()));
            mUpgradeButton.setVisibility(View.VISIBLE);
            mUpgradeButton.setOnClickListener(this);
        } else {
            mUpgradeButton.setVisibility(View.INVISIBLE);
            mUpgradeButton.setOnClickListener(null);
        }
    }

    private void updateDefenseInfo() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        DefenseFragment defenseFrag = (DefenseFragment) childFragmentManager.
                findFragmentByTag(DefenseFragment.FRAGMENT_TAG);
        if (defenseFrag == null) {
            defenseFrag = DefenseFragment.newInstance(mStructureId, mOwner.id, mStructure.defenseUnits, mOwner.units);
        } else {
            defenseFrag.onDefenseDataChanged(mStructure.defenseUnits, mOwner.units);
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_structure_defense_container, defenseFrag, DefenseFragment.FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_structure_upgrade_btn:
                onUpgradeStructure();
                break;
        }
    }

    private void onUpgradeStructure() {
        int upgradeCost = mStructure.getUpgradeCost();
        if (mOwner.gold < upgradeCost) {
            Toast.makeText(mContext, "Not enough gold", Toast.LENGTH_SHORT).show();
        } else {
            int subtractedGold = mOwner.gold - upgradeCost;
            FirebaseProvider.getInstance().upgradeStructureLevel(mOwner.id, subtractedGold,
                    mStructure.id, mStructure.level + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(mContext, "Upgraded structure level", Toast.LENGTH_SHORT).show();
                }
            });
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

        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

}