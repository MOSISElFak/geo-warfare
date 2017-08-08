package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tolstykh.textviewrichdrawable.TextViewRichDrawable;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.UnitType;

public class AttackFragment extends StructureFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "AttackFragment";

    private Map<String, Integer> mUserUnits;

    private Button mAttackButton;
    private Map<UnitType, TextViewRichDrawable> mMyArmyTvs = new HashMap<>();
    private Map<UnitType, TextViewRichDrawable> mDefenseArmyTvs = new HashMap<>();

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    public AttackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Call onCreateView of the generic structure fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // We hide the upgrade and defense views in the attack fragment
        view.findViewById(R.id.fragment_structure_specific_upgrade_divider).setVisibility(View.GONE);
        view.findViewById(R.id.fragment_structure_upgrade_title).setVisibility(View.GONE);
        view.findViewById(R.id.fragment_structure_upgrade_container).setVisibility(View.GONE);
        view.findViewById(R.id.fragment_structure_upgrade_defense_divider).setVisibility(View.GONE);
        view.findViewById(R.id.fragment_structure_defense_container).setVisibility(View.GONE);

        mAttackButton = (Button) view.findViewById(R.id.fragment_attack_attack_btn);

        return view;
    }

    public void setUnitCounts(Map<String, Integer> userUnits) {
        this.mUserUnits = userUnits;
    }

    @Override
    void drawStructureSpecificView(View fragmentView) {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup myArmyParentView = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_attack_army_container);
        ViewGroup defenseArmyParentView = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_attack_defense_container);

        // We need these layout params and margin to set for each drawn text view with drawable
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) getResources().getDimension(R.dimen.attack_army_item_vertical_margin);
        params.setMargins(0, margin, 0, margin);

        int iconSize = (int) getResources().getDimension(R.dimen.attack_army_icon_size);

        for (UnitType unitType : UnitType.values()) {
            TextViewRichDrawable myUnitCount = new TextViewRichDrawable(mContext, null,
                    R.attr.richTextViewDrawableStyle);

            myUnitCount.setLayoutParams(params);

            Drawable myUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());
            myUnitIcon.setBounds(0, 0, iconSize, iconSize);
            myUnitCount.setCompoundDrawables(myUnitIcon, null, null, null);

            mMyArmyTvs.put(unitType, myUnitCount);
            myArmyParentView.addView(myUnitCount);


            TextViewRichDrawable defenseUnitCount = new TextViewRichDrawable(mContext, null,
                    R.attr.richTextViewDrawableStyle);

            defenseUnitCount.setLayoutParams(params);
            defenseUnitCount.setGravity(Gravity.END);

            Drawable defenseUnitIcon = ContextCompat.getDrawable(mContext, unitType.getIconResourceId());
            defenseUnitIcon.setBounds(0, 0, iconSize, iconSize);
            defenseUnitCount.setCompoundDrawables(null, null, defenseUnitIcon, null);

            mDefenseArmyTvs.put(unitType, defenseUnitCount);
            defenseArmyParentView.addView(defenseUnitCount);
        }
    }

    @Override
    void drawStructureUpgradeView(View fragmentView) {
        // Don't need this here
    }

    @Override
    void updateSpecificStructureInfo() {
        setupUIValues();
    }

    private void setupUIValues() {
        for (UnitType unitType : UnitType.values()) {
            int myArmyUnitCount = 0;
            if (mUserUnits.containsKey(unitType.toString())) {
                myArmyUnitCount = mUserUnits.get(unitType.toString());
            }
            mMyArmyTvs.get(unitType).setText(String.valueOf(myArmyUnitCount));

            int defenseArmyUnitCount = 0;
            if (mStructure.getDefenseUnits().containsKey(unitType.toString())) {
                defenseArmyUnitCount = mStructure.getDefenseUnits().get(unitType.toString());
            }
            mDefenseArmyTvs.get(unitType).setText(String.valueOf(defenseArmyUnitCount));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_attack_attack_btn:
                onAttackClick();
                break;
        }
        super.onClick(v);
    }

    private void onAttackClick() {

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
