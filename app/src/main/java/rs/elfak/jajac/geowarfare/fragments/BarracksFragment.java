package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tolstykh.textviewrichdrawable.TextViewRichDrawable;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.BarracksModel;
import rs.elfak.jajac.geowarfare.models.UnitType;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.utils.MaxValueTextWatcher;

public class BarracksFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "BarracksFragment";

    private static final String ARG_STRUCTURE_ID = "structure_id";

    private Context mContext;

    private String mStructureId;
    private BarracksModel mBarracks;
    private UserModel mOwner;

    private int mTotalPurchasePrice = 0;

    private Map<UnitType, TextViewRichDrawable> mUnitsAvailableTvs = new HashMap<>();
    private Map<UnitType, EditText> mUnitsPurchaseEts = new HashMap<>();
    private Button mPurchaseButton;
    private Map<UnitType, TextView> mCurrentLevelAvailableTvs = new HashMap<>();
    private Map<UnitType, TextView> mNextLevelAvailableTvs = new HashMap<>();
    private Button mUpgradeButton;

    private Map<UnitType, MaxValueTextWatcher> mPurchaseEtTextWatchers = new HashMap<>();

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    public BarracksFragment() {
        // Required empty public constructor
    }

    public static BarracksFragment newInstance(String structureId) {
        BarracksFragment fragment = new BarracksFragment();
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
        View view = inflater.inflate(R.layout.fragment_barracks, container, false);

        drawPurchaseUnits(view);
        drawUpgradeUnits(view);
        mPurchaseButton = (Button) view.findViewById(R.id.fragment_barracks_purchase_btn);
        mUpgradeButton = (Button) view.findViewById(R.id.fragment_barracks_upgrade_btn);

        updatePurchaseButtonText();

        getStructureDataAndSetupUI(mStructureId);

        return view;
    }

    private void getStructureDataAndSetupUI(String structureId) {
        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        firebaseProvider.getStructureById(structureId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mBarracks = dataSnapshot.getValue(BarracksModel.class);
                        mBarracks.id = dataSnapshot.getKey();
                        firebaseProvider.getUserById(mBarracks.ownerId)
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
        updateBasicStructureInfo();

        for (UnitType unitType : UnitType.values()) {
            // Set available unit counts, reset purchase edit texts and set new maximums for text watchers
            int availableCount = mBarracks.availableUnits.get(unitType.toString());
            mUnitsAvailableTvs.get(unitType).setText(String.valueOf(availableCount));
            mUnitsPurchaseEts.get(unitType).setText(null);
            mPurchaseEtTextWatchers.get(unitType).setMax(availableCount);

            // Upgrade the current level unit batch size
            Map<UnitType, Integer> currLevelCounts = mBarracks.getCurrentAvailable();
            int unitBatchCount = currLevelCounts.get(unitType);
            mCurrentLevelAvailableTvs.get(unitType).setText(String.valueOf(unitBatchCount));
        }

        if (mBarracks.canUpgrade()) {
            // Update the next level unit batch size if the structure is not max level
            Map<UnitType, Integer> nextLevelCounts = mBarracks.getNextAvailable();
            for (UnitType unitType : UnitType.values()) {
                int unitBatchCount = nextLevelCounts.get(unitType);
                mNextLevelAvailableTvs.get(unitType).setText(String.valueOf(unitBatchCount));
                mUpgradeButton.setText(String.valueOf(mBarracks.getUpgradeCost()));
                mUpgradeButton.setVisibility(View.VISIBLE);
            }
        } else {
            for (UnitType unitType : UnitType.values()) {
                mNextLevelAvailableTvs.get(unitType).setText("/");
                mUpgradeButton.setVisibility(View.INVISIBLE);
            }
        }

        updateDefenseInfo();
    }

    private void updateBasicStructureInfo() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        StructureInfoFragment infoFrag = (StructureInfoFragment) childFragmentManager.findFragmentByTag
                (StructureInfoFragment.FRAGMENT_TAG);
        if (infoFrag == null) {
            infoFrag = StructureInfoFragment.newInstance(mBarracks.type,
                    mBarracks.level, mOwner.id, mOwner.displayName, mOwner.avatarUrl);
        } else {
            infoFrag.onStructureDataChanged(mBarracks.level);
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_barracks_info_container, infoFrag, StructureInfoFragment.FRAGMENT_TAG)
                .commit();
    }

    private void updateDefenseInfo() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        DefenseFragment defenseFrag = (DefenseFragment) childFragmentManager.
                findFragmentByTag(DefenseFragment.FRAGMENT_TAG);
        if (defenseFrag == null) {
            defenseFrag = DefenseFragment.newInstance(mStructureId, mOwner.id, mBarracks.defenseUnits, mOwner.units);
        } else {
            defenseFrag.onDefenseDataChanged(mBarracks.defenseUnits, mOwner.units);
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_barracks_defense_container, defenseFrag, DefenseFragment.FRAGMENT_TAG)
                .commit();
    }

    private void drawPurchaseUnits(View fragmentView) {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup parentView = (ViewGroup) fragmentView.findViewById(R.id.fragment_barracks_purchase_items_container);

        for (final UnitType unitType : UnitType.values()) {
            View view = layoutInflater.inflate(R.layout.barracks_purchase_unit_item, null, false);

            TextViewRichDrawable availableTv = (TextViewRichDrawable) view.findViewById(R.id.purchase_unit_available);
            EditText purchaseEt = (EditText) view.findViewById(R.id.purchase_item_unit_count);
            TextView priceTv = (TextView) view.findViewById(R.id.purchase_item_unit_price);

            Drawable availableUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());
            // This will return the value in pixels but based on pixel density
            int iconSize = (int) getResources().getDimension(R.dimen.structure_unit_icon_size);
            availableUnitIcon.setBounds(0, 0, iconSize, iconSize);
            availableTv.setCompoundDrawables(availableUnitIcon, null, null, null);

            priceTv.setText(String.valueOf(unitType.getBaseCost()));

            // Just create and add the text watchers here, max will be set in updateUIValues()
            MaxValueTextWatcher transferEtTextWatcher = new MaxValueTextWatcher(purchaseEt, 0);
            purchaseEt.addTextChangedListener(transferEtTextWatcher);

            // Also add a text watcher that will update the total price when some number is entered
            purchaseEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateTotalPurchasePriceAndUpdate();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }
                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mUnitsAvailableTvs.put(unitType, availableTv);
            mUnitsPurchaseEts.put(unitType, purchaseEt);
            mPurchaseEtTextWatchers.put(unitType, transferEtTextWatcher);

            parentView.addView(view);
        }
    }

    private void calculateTotalPurchasePriceAndUpdate() {
        mTotalPurchasePrice = 0;
        for (UnitType unitType : UnitType.values()) {
            String text = mUnitsPurchaseEts.get(unitType).getText().toString();
            if (!text.isEmpty()) {
                mTotalPurchasePrice += Integer.valueOf(text) * unitType.getBaseCost();
            }
        }
        updatePurchaseButtonText();
    }

    private void updatePurchaseButtonText() {
        mPurchaseButton.setText(String.valueOf(mTotalPurchasePrice));
    }

    private void drawUpgradeUnits(View fragmentView) {
        ViewGroup currentLevelItemsParent = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_barracks_current_level_items_container);
        ViewGroup nextLevelItemsParent = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_barracks_next_level_items_container);

        // We need these layout params and margin to set for each drawn text view with drawable
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int endMargin = (int) getResources().getDimension(R.dimen.structure_icon_value_margin);
        params.setMarginEnd(endMargin);

        // This will return the value in pixels but based on pixel density
        int iconSize = (int) getResources().getDimension(R.dimen.structure_unit_icon_size);

        // CURRENT LEVEL
        for (UnitType unitType : UnitType.values()) {
            TextViewRichDrawable curView = new TextViewRichDrawable(mContext, null,
                    R.attr.richTextViewDrawableStyle);

            Drawable upgradeUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());
            upgradeUnitIcon.setBounds(0, 0, iconSize, iconSize);
            curView.setCompoundDrawables(upgradeUnitIcon, null, null, null);
            curView.setLayoutParams(params);

            mCurrentLevelAvailableTvs.put(unitType, curView);
            currentLevelItemsParent.addView(curView);
        }

        // NEXT LEVEL
        for (UnitType unitType : UnitType.values()) {
            TextViewRichDrawable nextView = new TextViewRichDrawable(mContext, null,
                    R.attr.richTextViewDrawableStyle);

            Drawable upgradeUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());

            upgradeUnitIcon.setBounds(0, 0, iconSize, iconSize);
            nextView.setCompoundDrawables(upgradeUnitIcon, null, null, null);
            nextView.setLayoutParams(params);

            mNextLevelAvailableTvs.put(unitType, nextView);
            nextLevelItemsParent.addView(nextView);
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
