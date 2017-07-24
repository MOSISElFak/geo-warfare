package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.StructureType;

public class StructureInfoFragment extends BaseFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "StructureInfoFragment";

    private static final String ARG_STRUCTURE_TYPE = "structure_type";
    private static final String ARG_STRUCTURE_LEVEL = "structure_level";
    private static final String ARG_OWNER_USER_ID = "owner_user_id";
    private static final String ARG_OWNER_DISPLAY_NAME = "owner_display_name";
    private static final String ARG_OWNER_AVATAR_URL = "owner_avatar_url";

    private Context mContext;

    private StructureType mStructureType;
    private int mStructureLevel;
    private String mOwnerUserId;
    private String mOwnerDisplayName;
    private String mOwnerAvatarUrl;

    private ImageView mStructureIconImg;
    private TextView mStructureTypeTv;
    private TextView mOwnerDisplayNameTv;
    private ImageView mOwnerAvatarImg;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onOwnerAvatarClick(String mOwnerUserId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    public StructureInfoFragment() {
        // Required empty public constructor
    }

    public static StructureInfoFragment newInstance(StructureType structureType, int structureLevel, String ownerUserId,
                                                    String ownerDisplayName, String owner_avatarUrl) {
        StructureInfoFragment fragment = new StructureInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STRUCTURE_TYPE, structureType);
        args.putInt(ARG_STRUCTURE_LEVEL, structureLevel);
        args.putString(ARG_OWNER_USER_ID, ownerUserId);
        args.putString(ARG_OWNER_DISPLAY_NAME, ownerDisplayName);
        args.putString(ARG_OWNER_AVATAR_URL, owner_avatarUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStructureType = (StructureType) getArguments().getSerializable(ARG_STRUCTURE_TYPE);
            mStructureLevel = getArguments().getInt(ARG_STRUCTURE_LEVEL);
            mOwnerUserId = getArguments().getString(ARG_OWNER_USER_ID);
            mOwnerDisplayName = getArguments().getString(ARG_OWNER_DISPLAY_NAME);
            mOwnerAvatarUrl = getArguments().getString(ARG_OWNER_AVATAR_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_structure_info, container, false);

        mStructureIconImg = (ImageView) view.findViewById(R.id.structure_info_icon_img);
        mStructureTypeTv = (TextView) view.findViewById(R.id.structure_info_type);
        mOwnerDisplayNameTv = (TextView) view.findViewById(R.id.structure_info_owner_display_name);
        mOwnerAvatarImg = (ImageView) view.findViewById(R.id.structure_info_owner_avatar_img);

        mStructureIconImg.setImageDrawable(ContextCompat.getDrawable(mContext, mStructureType.getIconResourceId()));
        mStructureTypeTv.setText(mStructureType.getName());
        mOwnerDisplayNameTv.setText(mOwnerDisplayName);
        Glide.with(mContext)
                .load(mOwnerAvatarUrl)
                .into(mOwnerAvatarImg);
        mOwnerAvatarImg.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.structure_info_owner_avatar_img:
                if (mListener != null) {
                    mListener.onOwnerAvatarClick(mOwnerUserId);
                }
                break;
        }
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

        mContext = context;
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

}
