package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.tolstykh.textviewrichdrawable.TextViewRichDrawable;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.BarracksModel;
import rs.elfak.jajac.geowarfare.models.UnitType;
import rs.elfak.jajac.geowarfare.utils.MaxValueTextWatcher;

public class BarracksFragment extends StructureFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "BarracksFragment";

    private int mTotalPurchasePrice = 0;

    private Map<UnitType, TextViewRichDrawable> mUnitsAvailableTvs = new HashMap<>();
    private Map<UnitType, EditText> mUnitsPurchaseEts = new HashMap<>();
    private Button mPurchaseButton;
    private Map<UnitType, TextView> mCurrentLevelAvailableTvs = new HashMap<>();
    private Map<UnitType, TextView> mNextLevelAvailableTvs = new HashMap<>();

    private Map<UnitType, MaxValueTextWatcher> mPurchaseEtTextWatchers = new HashMap<>();

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    public BarracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Call onCreateView of the generic structure fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mPurchaseButton = (Button) view.findViewById(R.id.fragment_barracks_purchase_btn);

        updatePurchaseButtonText();

        return view;
    }

    @Override
    void drawStructureSpecificView(View fragmentView) {
        drawPurchaseView(fragmentView);
    }

    @Override
    void drawStructureUpgradeView(View fragmentView) {
        drawUpgradeUnits(fragmentView);
    }

    @Override
    void updateSpecificStructureInfo() {
        setupUIValues();
    }

    private void setupUIValues() {
        BarracksModel barracks = (BarracksModel) mStructure;

        for (UnitType unitType : UnitType.values()) {
            // We need this maxCurrAvailable because when we add new units to the UnitType
            // enum, barracks will show 0 in the 'purchase' part because its 'availableUnits'
            // does not contain our new type until the user makes his first purchase of this type
            Map<UnitType, Integer> currLevelCounts = barracks.getCurrentAvailable();
            // Set available unit counts, reset purchase edit texts and set new maximums for text watchers
            int availableCount = currLevelCounts.get(unitType);
            if (barracks.availableUnits.containsKey(unitType.toString())) {
                // If we find the type in the barracks object, we use that count instead
                availableCount = barracks.availableUnits.get(unitType.toString());
            }
            mUnitsAvailableTvs.get(unitType).setText(String.valueOf(availableCount));
            mUnitsPurchaseEts.get(unitType).setText(null);
            mPurchaseEtTextWatchers.get(unitType).setMax(availableCount);

            // Upgrade the current level unit batch size
            int unitBatchCount = currLevelCounts.get(unitType);
            mCurrentLevelAvailableTvs.get(unitType).setText(String.valueOf(unitBatchCount));
        }

        if (barracks.canUpgrade()) {
            // Update the next level unit batch size if the structure is not max level
            Map<UnitType, Integer> nextLevelCounts = barracks.getNextAvailable();
            for (UnitType unitType : UnitType.values()) {
                int unitBatchCount = nextLevelCounts.get(unitType);
                mNextLevelAvailableTvs.get(unitType).setText(String.valueOf(unitBatchCount));
            }
        } else {
            for (UnitType unitType : UnitType.values()) {
                mNextLevelAvailableTvs.get(unitType).setText("/");
            }
        }
    }

    private void drawPurchaseView(View fragmentView) {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup parentView = (ViewGroup) fragmentView.findViewById(R.id.fragment_barracks_purchase_items_container);

        for (final UnitType unitType : UnitType.values()) {
            View view = layoutInflater.inflate(R.layout.barracks_purchase_unit_item, null, false);

            TextViewRichDrawable availableTv = (TextViewRichDrawable) view.findViewById(R.id.purchase_unit_available);
            EditText purchaseEt = (EditText) view.findViewById(R.id.purchase_item_unit_count);
            TextView priceTv = (TextView) view.findViewById(R.id.purchase_item_unit_price);

            Drawable availableUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());
            // This will return the value in pixels but based on pixel density
            int iconSize = (int) getResources().getDimension(R.dimen.structure_fragment_icon_size);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_barracks_purchase_btn:
                // implement purchasing units
                break;
        }
        super.onClick(v);
    }

    private void drawUpgradeUnits(View fragmentView) {
        ViewGroup upgradeCurrentContainer = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_structure_upgrade_current_container);
        ViewGroup upgradeNextContainer = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_structure_upgrade_next_container);

        // We need these layout params and margin to set for each drawn text view with drawable
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // This will return the value in pixels but based on pixel density
        int iconSize = (int) getResources().getDimension(R.dimen.structure_fragment_icon_size);

        // CURRENT LEVEL
        for (UnitType unitType : UnitType.values()) {
            TextViewRichDrawable curView = new TextViewRichDrawable(mContext, null,
                    R.attr.richTextViewDrawableStyle);

            Drawable upgradeUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());
            upgradeUnitIcon.setBounds(0, 0, iconSize, iconSize);
            curView.setCompoundDrawables(upgradeUnitIcon, null, null, null);

            mCurrentLevelAvailableTvs.put(unitType, curView);
            upgradeCurrentContainer.addView(curView);
        }

        // NEXT LEVEL
        for (UnitType unitType : UnitType.values()) {
            TextViewRichDrawable nextView = new TextViewRichDrawable(mContext, null,
                    R.attr.richTextViewDrawableStyle);

            Drawable upgradeUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());

            upgradeUnitIcon.setBounds(0, 0, iconSize, iconSize);
            nextView.setCompoundDrawables(upgradeUnitIcon, null, null, null);

            mNextLevelAvailableTvs.put(unitType, nextView);
            upgradeNextContainer.addView(nextView);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
