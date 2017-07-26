package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import rs.elfak.jajac.geowarfare.R;

public class DefenseFragment extends BaseFragment implements View.OnFocusChangeListener {

    public static final String FRAGMENT_TAG = "DefenseFragment";

    private static final String ARG_STRUCTURE_SWORD_COUNT = "structure_sword_count";
    private static final String ARG_STRUCTURE_BOW_COUNT = "structure_bow_count";
    private static final String ARG_USER_SWORD_COUNT = "user_sword_count";
    private static final String ARG_USER_BOW_COUNT = "user_bow_count";

    private int mStructureSwordCount;
    private int mStructureBowCount;
    private int mUserSwordCount;
    private int mUserBowCount;

    private int mSwordTransferCount = 0;
    private int mBowTransferCount = 0;

    private TextView mStructureSwordCountTv;
    private TextView mStructureBowCountTv;
    private EditText mTransferSwordCountEt;
    private EditText mTransferBowCountEt;
    private ImageButton mTransferUpBtn;
    private ImageButton mTransferDownBtn;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

    }

    public DefenseFragment() {
        // Required empty public constructor
    }

    public static DefenseFragment newInstance(int structureSwordCount, int structureBowCount,
                                              int userSwordCount, int userBowCount) {
        DefenseFragment fragment = new DefenseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STRUCTURE_SWORD_COUNT, structureSwordCount);
        args.putInt(ARG_STRUCTURE_BOW_COUNT, structureBowCount);
        args.putInt(ARG_USER_SWORD_COUNT, userSwordCount);
        args.putInt(ARG_USER_BOW_COUNT, userBowCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStructureSwordCount = getArguments().getInt(ARG_STRUCTURE_SWORD_COUNT);
            mStructureBowCount = getArguments().getInt(ARG_STRUCTURE_BOW_COUNT);
            mUserSwordCount = getArguments().getInt(ARG_USER_SWORD_COUNT);
            mUserBowCount = getArguments().getInt(ARG_USER_BOW_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_defense, container, false);

        mStructureSwordCountTv = (TextView) view.findViewById(R.id.fragment_defense_sword_count);
        mStructureBowCountTv = (TextView) view.findViewById(R.id.fragment_defense_bow_count);
        mTransferSwordCountEt = (EditText) view.findViewById(R.id.fragment_defense_sword_transfer_count);
        mTransferBowCountEt = (EditText) view.findViewById(R.id.fragment_defense_bow_transfer_count);
        mTransferUpBtn = (ImageButton) view.findViewById(R.id.fragment_defense_transfer_up_btn);
        mTransferDownBtn = (ImageButton) view.findViewById(R.id.fragment_defense_transfer_down_btn);

        setupUI();

        return view;
    }

    private void setupUI() {
        mStructureSwordCountTv.setText(String.valueOf(mStructureSwordCount));
        mStructureBowCountTv.setText(String.valueOf(mStructureBowCount));

        mTransferSwordCountEt.setText(String.valueOf(mSwordTransferCount));
        mTransferBowCountEt.setText(String.valueOf(mBowTransferCount));
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

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
