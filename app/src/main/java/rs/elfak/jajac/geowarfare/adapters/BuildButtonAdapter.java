package rs.elfak.jajac.geowarfare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureType;

public class BuildButtonAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<BuildButtonItem> mItems = new ArrayList<>();
    private final OnBuildItemClickListener mListener;

    public interface OnBuildItemClickListener {
        void onBuildStructureClick(StructureType structureType);
    }

    public BuildButtonAdapter(Context context, OnBuildItemClickListener listener) {
        mContext = context;
        mListener = listener;
        mItems.add(new BuildButtonItem(R.drawable.ic_gold_cart, StructureType.GOLD_MINE, GoldMineModel.COST));
        mItems.add(new BuildButtonItem(R.drawable.ic_barracks, StructureType.BARRACKS, 2000));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BuildButtonViewHolder holder;
        View view = convertView;

        // If the view is being created, inflate the layout and attach a new holder as a tag
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            holder = new BuildButtonViewHolder(view, parent);
            view.setTag(holder);
        } else {
            // If the view exists and is being recycled, just get the holder from the tag
            holder = (BuildButtonViewHolder) view.getTag();
        }

        // Set the appropriate item
        holder.mItem = mItems.get(position);

        holder.mImageButton.setImageResource(holder.mItem.imageResourceId);
        holder.mName.setText(holder.mItem.structureType.getName());
        holder.mCost.setText(String.valueOf(holder.mItem.cost));

        return view;
    }

    // Simple class holding data related to one item on the Build menu/fragment
    private class BuildButtonItem {
        int imageResourceId;
        StructureType structureType;
        int cost;

        BuildButtonItem(int imageResourceId, StructureType structureType, int cost) {
            this.imageResourceId = imageResourceId;
            this.structureType = structureType;
            this.cost = cost;
        }
    }

    // View holder class for the item
    private class BuildButtonViewHolder implements View.OnClickListener {
        private BuildButtonItem mItem;
        private ImageButton mImageButton;
        private TextView mName;
        private TextView mCost;

        private BuildButtonViewHolder(View view, ViewGroup parent) {
            mImageButton = (ImageButton) view.findViewById(R.id.grid_item_image_btn);
            mName = (TextView) view.findViewById(R.id.grid_item_name_text);
            mCost = (TextView) view.findViewById(R.id.grid_item_cost_text);

            mImageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onBuildStructureClick(mItem.structureType);
        }
    }
}
