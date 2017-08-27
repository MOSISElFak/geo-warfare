package rs.elfak.jajac.geowarfare.fragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.elfak.jajac.geowarfare.R;

public class AboutDialogFragment extends DialogFragment {

    public AboutDialogFragment() {
        // Required empty public constructor
    }

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_about, container, false);

        return view;
    }

}
