package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.adapters.ResearchSkillRecyclerViewAdapter;
import rs.elfak.jajac.geowarfare.models.SkillType;

public class ResearchFragment extends BaseFragment implements
        ResearchSkillRecyclerViewAdapter.OnUpgradeSkillClickListener {

    public static final String FRAGMENT_TAG = "ResearchFragment";

    private static final String ARG_SKILL_LEVELS = "skill_levels";

    private Context mContext;

    private Map<String, Integer> mSkillLevels;

    private RecyclerView mSkillsRecyclerView;
    private ResearchSkillRecyclerViewAdapter mSkillsAdapter;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onUpgradeSkill(SkillType skillType, int upgradeCost);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle("Research");
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    public ResearchFragment() {
        // Required empty public constructor
    }
    public static ResearchFragment newInstance(Map<String, Integer> skillLevels) {
        ResearchFragment fragment = new ResearchFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SKILL_LEVELS, (HashMap<String, Integer>)skillLevels);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSkillLevels = (HashMap<String, Integer>) getArguments().getSerializable(ARG_SKILL_LEVELS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_research, container, false);

        mSkillsRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_research_items_list);

        DividerItemDecoration divider = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.list_divider));
        mSkillsRecyclerView.addItemDecoration(divider);

        mSkillsAdapter = new ResearchSkillRecyclerViewAdapter(mSkillLevels, this);
        mSkillsRecyclerView.setAdapter(mSkillsAdapter);

        return view;
    }

    @Override
    public void onUpgradeSkillClick(SkillType skillType) {
        int currentLevel = mSkillLevels.get(skillType.toString());
        int upgradeCost = skillType.getUpgradeCost(currentLevel);
        mListener.onUpgradeSkill(skillType, upgradeCost);
    }

    public void updateUI(Map<String, Integer> newSkillLevels) {
        mSkillLevels = newSkillLevels;
        mSkillsAdapter = new ResearchSkillRecyclerViewAdapter(mSkillLevels, this);
        mSkillsRecyclerView.setAdapter(mSkillsAdapter);
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
