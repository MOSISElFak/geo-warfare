package rs.elfak.jajac.geowarfare.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.SkillType;
import rs.elfak.jajac.geowarfare.utils.Num2Str;

public class ResearchSkillRecyclerViewAdapter extends
        RecyclerView.Adapter<ResearchSkillRecyclerViewAdapter.SkillViewHolder> {

    private Map<String, Integer> mSkillLevels;

    private ArrayList<SkillType> mItems = new ArrayList<>();
    private final OnUpgradeSkillClickListener mListener;

    public interface OnUpgradeSkillClickListener {
        void onUpgradeSkillClick(SkillType skillType);
    }

    public ResearchSkillRecyclerViewAdapter(Map<String, Integer> skillLevels, OnUpgradeSkillClickListener listener) {
        mSkillLevels = skillLevels;
        mListener = listener;
        mItems.addAll(Arrays.asList(SkillType.values()));
    }

    @Override
    public SkillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.research_list_item, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SkillViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mSkillName.setText(holder.mItem.getName());
        holder.mSkillDescription.setText(holder.mItem.getDescription());
        holder.mSkillIcon.setImageResource(holder.mItem.getIconResourceId());

        int currentLevel = mSkillLevels.get(holder.mItem.toString());
        if (holder.mItem.canUpgrade(currentLevel)) {
            String priceString = Num2Str.convert(holder.mItem.getUpgradeCost(currentLevel));
            holder.mUpgradeButton.setText(priceString);
            holder.mUpgradeButton.setVisibility(View.VISIBLE);
        }

        holder.mSkillLevelContainer.removeAllViews();
        Context context = holder.mItemView.getContext();
        for (int i = 0; i < holder.mItem.getMaxLevel(); i++) {
            ImageView starImage = new ImageView(context);
            starImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            starImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimaryDark)));
            if (i < currentLevel) {
                starImage.setImageResource(R.drawable.ic_star_24dp);
            } else {
                starImage.setImageResource(R.drawable.ic_star_border_24dp);
            }
            holder.mSkillLevelContainer.addView(starImage);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class SkillViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mItemView;
        public final ViewGroup mSkillLevelContainer;
        public final TextView mSkillName;
        public final TextView mSkillDescription;
        public final ImageView mSkillIcon;
        public final Button mUpgradeButton;
        public SkillType mItem;

        public SkillViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;

            mSkillLevelContainer = (ViewGroup) itemView.findViewById(R.id.research_item_level_container);
            mSkillName = (TextView) itemView.findViewById(R.id.research_item_skill_name);
            mSkillDescription = (TextView) itemView.findViewById(R.id.research_item_skill_description);
            mSkillIcon = (ImageView) itemView.findViewById(R.id.research_item_skill_icon);
            mUpgradeButton = (Button) itemView.findViewById(R.id.research_item_skill_upgrade_btn);

            mUpgradeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SkillType skillType = mItems.get(getAdapterPosition());
            mListener.onUpgradeSkillClick(skillType);
        }
    }
}
