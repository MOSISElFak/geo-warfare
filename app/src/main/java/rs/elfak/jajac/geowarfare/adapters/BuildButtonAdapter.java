package rs.elfak.jajac.geowarfare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.StructureType;

public class BuildButtonAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<StructureType> mItems = new ArrayList<>();
    private final OnBuildItemClickListener mListener;

    public interface OnBuildItemClickListener {
        void onBuildStructureClick(StructureType structureType);
    }

    public BuildButtonAdapter(Context context, OnBuildItemClickListener listener) {
        mContext = context;
        mListener = listener;
        // get all types and add them to the array
        mItems.addAll(Arrays.asList(StructureType.values()));
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

        holder.mImageButton.setImageResource(holder.mItem.getIconResId());
        holder.mName.setText(holder.mItem.getName());
        holder.mCost.setText(String.valueOf(holder.mItem.getBaseCost()));

        return view;
    }

    // View holder class for the item
    private class BuildButtonViewHolder implements View.OnClickListener {
        private StructureType mItem;
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
            mListener.onBuildStructureClick(mItem);
        }
    }
}
