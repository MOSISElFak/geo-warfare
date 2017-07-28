package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tolstykh.textviewrichdrawable.TextViewRichDrawable;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;

public class GoldMineFragment extends StructureFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "GoldMineFragment";

    private TextView mCollectAmount;
    private Button mCollectButton;
    private TextViewRichDrawable mCurrentLevelIncome;
    private TextViewRichDrawable mNextLevelIncome;
    private Button mUpgradeButton;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    public GoldMineFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Call onCreateView of the generic structure fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mCollectAmount = (TextView) view.findViewById(R.id.fragment_gold_mine_collect);
        mCollectButton = (Button) view.findViewById(R.id.fragment_gold_mine_collect_btn);

        return view;
    }

    @Override
    void drawStructureSpecificView(View fragmentView) {
        // We do nothing here since the "collect" part of the gold mine is
        // basic and is implemented directly in the fragment_gold_mine.xml
    }

    @Override
    void drawStructureUpgradeView(View fragmentView) {
        drawUpgradeAmounts(fragmentView);
    }

    @Override
    void updateSpecificStructureInfo() {
        setupUIValues();
    }

    private void setupUIValues() {
        GoldMineModel goldMine = (GoldMineModel) mStructure;

        mCollectAmount.setText(String.valueOf(goldMine.gold));

        mCurrentLevelIncome.setText(String.valueOf(goldMine.getCurrentIncome()));
        if (goldMine.canUpgrade()) {
            mNextLevelIncome.setText(String.valueOf(goldMine.getNextIncome()));
        } else {
            mNextLevelIncome.setText(getString(R.string.structure_max_level_message));
        }
    }

    private void drawUpgradeAmounts(View fragmentView) {
        ViewGroup upgradeCurrentContainer = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_structure_upgrade_current_container);
        ViewGroup upgradeNextContainer = (ViewGroup) fragmentView.findViewById(R.id
                .fragment_structure_upgrade_next_container);

        // We need these layout params and margin to set for each drawn text view with drawable
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int endMargin = (int) getResources().getDimension(R.dimen.structure_icon_value_margin);
        params.setMarginEnd(endMargin);

        // This will return the value in pixels but based on pixel density
        int iconSize = (int) getResources().getDimension(R.dimen.structure_fragment_icon_size);

        Drawable goldStackIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_coin_stack);
        goldStackIcon.setBounds(0, 0, iconSize, iconSize);

        // CURRENT LEVEL
        mCurrentLevelIncome = new TextViewRichDrawable(mContext, null, R.attr.richTextViewDrawableStyle);
        mCurrentLevelIncome.setCompoundDrawables(goldStackIcon, null, null, null);
        mCurrentLevelIncome.setLayoutParams(params);
        upgradeCurrentContainer.addView(mCurrentLevelIncome);

        // NEXT LEVEL
        mNextLevelIncome = new TextViewRichDrawable(mContext, null, R.attr.richTextViewDrawableStyle);
        mNextLevelIncome.setCompoundDrawables(goldStackIcon, null, null, null);
        mNextLevelIncome.setLayoutParams(params);
        upgradeNextContainer.addView(mNextLevelIncome);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_gold_mine_collect_btn:
                onCollectGold();
                break;
        }
    }

    private void onCollectGold() {
//        if (mGoldMine.gold <= 0) {
//            Toast.makeText(mContext, getString(R.string.gold_mine_empty_message), Toast.LENGTH_SHORT).show();
//        } else {
//            // Remove listener to prevent multiple clicks
//            mCollectButton.setOnClickListener(null);
//
//            final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
//            final String myUserId = firebaseProvider.getCurrentFirebaseUser().getUid();
//            firebaseProvider.getUserGold(myUserId)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            int currentGold = dataSnapshot.getValue(int.class);
//                            int totalGold = currentGold + mGoldMine.gold;
//                            firebaseProvider.transferGold(myUserId, totalGold, mGoldMine.id)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            mCollectButton.setOnClickListener(GoldMineFragment.this);
//                                            updateBasicStructureInfo();
//                                        }
//                                    });
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//        }
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
