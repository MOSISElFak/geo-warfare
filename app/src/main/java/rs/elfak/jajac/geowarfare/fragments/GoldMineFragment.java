package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.StructureType;

public class GoldMineFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "GoldMineFragment";

    private static final String ARG_STRUCTURE_ID = "structure_id";

    private String mStructureId;

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

        return view;
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
