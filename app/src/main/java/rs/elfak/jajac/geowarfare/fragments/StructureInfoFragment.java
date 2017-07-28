package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
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

    private ViewGroup mStructureLevelContainer;
    private ImageView mStructureIconImg;
    private TextView mStructureTypeTv;
    private TextView mOwnerDisplayNameTv;
    private ImageView mOwnerAvatarImg;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onOwnerAvatarClick(String mOwnerUserId);
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

        mStructureIconImg.setImageResource(mStructureType.getIconResId());
        mStructureTypeTv.setText(mStructureType.getName());
        mOwnerDisplayNameTv.setText(mOwnerDisplayName);
        Glide.with(mContext)
                .load(mOwnerAvatarUrl)
                .into(mOwnerAvatarImg);
        mOwnerAvatarImg.setOnClickListener(this);

        mStructureLevelContainer = (ViewGroup) view.findViewById(R.id.structure_info_level_container);
        drawStructureLevelIndicators();

        return view;
    }

    // We're only concerned whether the structure level changed so we can update the star indicators
    public void onStructureDataChanged(int structureLevel) {
        if (mStructureLevel != structureLevel) {
            mStructureLevel = structureLevel;
            drawStructureLevelIndicators();
        }
    }

    private void drawStructureLevelIndicators() {
        mStructureLevelContainer.removeAllViews();
        for (int i = 0; i < mStructureType.getMaxLevel(); i++) {
            ImageView starImage = new ImageView(mContext);
            starImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            starImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
            if (i < mStructureLevel) {
                starImage.setImageResource(R.drawable.ic_star_24dp);
            } else {
                starImage.setImageResource(R.drawable.ic_star_border_24dp);
            }
            mStructureLevelContainer.addView(starImage);
        }
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
        mContext = null;
        mListener = null;
    }

}
