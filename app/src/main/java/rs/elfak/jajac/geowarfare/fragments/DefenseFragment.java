package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.UnitType;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.utils.MaxValueTextWatcher;

public class DefenseFragment extends BaseFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "DefenseFragment";

    private static final String ARG_STRUCTURE_ID = "structure_id";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_DEFENSE_UNITS = "defense_units";
    private static final String ARG_USER_UNITS = "user_units";

    private Context mContext;

    private String mStructureId;
    private String mUserId;
    private Map<String, Integer> mDefenseUnits;
    private Map<String, Integer> mUserUnits;

    private Map<UnitType, TextView> mDefenseUnitsTvs = new HashMap<>();
    private Map<UnitType, EditText> mTransferUnitsEts = new HashMap<>();
    private Button mTransferUpBtn;
    private Button mTransferDownBtn;

    private Map<UnitType, MaxValueTextWatcher> mTransferEtTextWatchers = new HashMap<>();

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    public DefenseFragment() {
        // Required empty public constructor
    }

    public static DefenseFragment newInstance(String structureId, String userId,  Map<String, Integer> defenseUnits,
                                              Map<String, Integer> userUnits) {
        DefenseFragment fragment = new DefenseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STRUCTURE_ID, structureId);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_DEFENSE_UNITS, (HashMap<String, Integer>) defenseUnits);
        args.putSerializable(ARG_USER_UNITS, (HashMap<String, Integer>) userUnits);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStructureId = getArguments().getString(ARG_STRUCTURE_ID);
            mUserId = getArguments().getString(ARG_USER_ID);
            mDefenseUnits = (HashMap<String, Integer>) getArguments().getSerializable(ARG_DEFENSE_UNITS);
            mUserUnits = (HashMap<String, Integer>) getArguments().getSerializable(ARG_USER_UNITS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_defense, container, false);

        drawDefenseUnits(view);

        mTransferUpBtn = (Button) view.findViewById(R.id.fragment_defense_transfer_up_btn);
        mTransferDownBtn = (Button) view.findViewById(R.id.fragment_defense_transfer_down_btn);

        updateUIValues();

        return view;
    }

    private void drawDefenseUnits(View fragmentView) {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup parentView = (ViewGroup) fragmentView.findViewById(R.id.fragment_defense_unit_items_container);

        for (UnitType unitType : UnitType.values()) {
            View view = layoutInflater.inflate(R.layout.defense_unit_item, null, false);

            ImageView icon = (ImageView) view.findViewById(R.id.defense_item_unit_icon);
            TextView countTv = (TextView) view.findViewById(R.id.defense_item_unit_count);
            EditText transferEt = (EditText) view.findViewById(R.id.defense_item_transfer_count);

            icon.setImageResource(unitType.getIconResourceId());

            // Just create the text watchers here, max will be set in updateUIValues()
            MaxValueTextWatcher transferEtTextWatcher = new MaxValueTextWatcher(transferEt, 0);

            mDefenseUnitsTvs.put(unitType, countTv);
            mTransferUnitsEts.put(unitType, transferEt);
            mTransferEtTextWatchers.put(unitType, transferEtTextWatcher);

            parentView.addView(view);
        }
    }

    public void updateUIValues() {
        for (UnitType unitType : UnitType.values()) {

            int defenseUnitCount = 0;
            if (mDefenseUnits.containsKey(unitType.toString())) {
                defenseUnitCount = mDefenseUnits.get(unitType.toString());
            }

            int userUnitCount = 0;
            if (mUserUnits.containsKey(unitType.toString())) {
                userUnitCount = mUserUnits.get(unitType.toString());
            }

            int transferMax = Math.max(defenseUnitCount, userUnitCount);

            mDefenseUnitsTvs.get(unitType).setText(String.valueOf(defenseUnitCount));
            mTransferEtTextWatchers.get(unitType).setMax(transferMax);
        }

        addTransferButtonListeners();
    }

    private void addTransferButtonListeners() {
        mTransferUpBtn.setOnClickListener(this);
        mTransferDownBtn.setOnClickListener(this);
    }

    private void removeTransferButtonListeners() {
        mTransferUpBtn.setOnClickListener(null);
        mTransferDownBtn.setOnClickListener(null);
    }

    @Override
    public void onClick(View v) {
        removeTransferButtonListeners();
        switch (v.getId()) {
            case R.id.fragment_defense_transfer_up_btn:
                onTransfer(true);
                break;
            case R.id.fragment_defense_transfer_down_btn:
                onTransfer(false);
                break;
        }
        addTransferButtonListeners();
    }

    private void onTransfer(boolean isLeavingUnits) {
        if (isAllTransferEmpty()) {
            Toast.makeText(mContext, getString(R.string.structure_defense_transfer_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> newDefenseUnitCounts = new HashMap<>();
        Map<String, Integer> newUserUnitCounts = new HashMap<>();

        List<UnitType> unitTypes = Arrays.asList(UnitType.values());

        int userAmount = 0;
        int structureAmount = 0;
        int transferAmount = 0;
        // Exit if the transfer cannot be done because there are not enough units on one side or both
        for (UnitType unitType : unitTypes) {
            // If the user/structure (depending on direction of transfer) doesn't have any, go to next unit
            if ((isLeavingUnits && !mUserUnits.containsKey(unitType.toString())) ||
                    !isLeavingUnits && !mDefenseUnits.containsKey(unitType.toString())) {
                continue;
            }

            userAmount = 0;
            if (mUserUnits.containsKey(unitType.toString())) {
                userAmount = mUserUnits.get(unitType.toString());
            }

            structureAmount = 0;
            if (mDefenseUnits.containsKey(unitType.toString())) {
                structureAmount = mDefenseUnits.get(unitType.toString());
            }

            transferAmount = 0;
            String transferText = mTransferUnitsEts.get(unitType).getText().toString();
            if (!transferText.isEmpty()) {
                transferAmount = Integer.parseInt(transferText);
            }

            // User is trying to leave more units than he has to defend
            if (isLeavingUnits && userAmount < transferAmount) {
                Toast.makeText(mContext, "You don't have that many " + unitType.getName() + ".",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // User is trying to take more units than the structure has
            if (!isLeavingUnits && structureAmount < transferAmount) {
                Toast.makeText(mContext, "Structure doesn't have that many " + unitType.getName() + ".",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // If the user is taking units, we just change the sign
            if (!isLeavingUnits) {
                transferAmount = -transferAmount;
            }

            newDefenseUnitCounts.put(unitType.toString(), structureAmount + transferAmount);
            newUserUnitCounts.put(unitType.toString(), userAmount - transferAmount);
        }

        // This will execute only if everything is ok
        doTransfer(newDefenseUnitCounts, newUserUnitCounts);
    }

    private void doTransfer(Map<String, Integer> newDefenseUnitCounts, Map<String, Integer> newUserUnitCounts) {
        FirebaseProvider.getInstance().transferUnits(mStructureId, newDefenseUnitCounts, mUserId, newUserUnitCounts)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // We only show a toast here because updates will come from the parent fragment
                        Toast.makeText(mContext, getString(R.string.structure_defense_transfer_success),
                                Toast.LENGTH_SHORT).show();
                        // We also clear the transferUnits map and corresponding EditTexts
                        clearTransferData();
                    }
                });
    }

    private void clearTransferData() {
        for (EditText transferEt : mTransferUnitsEts.values()) {
            transferEt.setText(null);
        }
    }

    // Called when parent fragment receives updated structure data after some action (eg. transfer units)
    public void onDefenseDataChanged(Map<String, Integer> newDefenseUnits, Map<String, Integer> newUserUnits) {
        mDefenseUnits = newDefenseUnits;
        mUserUnits = newUserUnits;

        updateUIValues();
    }

    private boolean isAllTransferEmpty() {
        for (EditText transferUnitEt : mTransferUnitsEts.values()) {
            // If there's a single transfer field that isn't empty, we return
            if (!transferUnitEt.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
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
