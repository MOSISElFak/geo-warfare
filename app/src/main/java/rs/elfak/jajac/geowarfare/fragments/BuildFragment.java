package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.adapters.BuildButtonAdapter;
import rs.elfak.jajac.geowarfare.models.StructureType;

public class BuildFragment extends BaseFragment implements BuildButtonAdapter.OnBuildItemClickListener {

    public static final String FRAGMENT_TAG = "BuildFragment";

    private Context mContext;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onBuildStructureClick(StructureType structureType);
    }

    public BuildFragment() {
        // Required empty public constructor
    }

    public static BuildFragment newInstance() {
        BuildFragment fragment = new BuildFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(getString(R.string.build_title));
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.fragment_build_grid);
        gridView.setAdapter(new BuildButtonAdapter(mContext, this));

        return view;
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

        setHasOptionsMenu(true);
        mContext = context;
    }

    @Override
    public void onBuildStructureClick(StructureType structureType) {
        mListener.onBuildStructureClick(structureType);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

}
