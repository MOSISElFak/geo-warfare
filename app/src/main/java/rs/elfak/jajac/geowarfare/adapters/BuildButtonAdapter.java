package rs.elfak.jajac.geowarfare.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.BuildFragment;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.StructureType;

class BuildButtonItem {
    int imageResourceId;
    StructureType structureType;
    int cost;

    BuildButtonItem(int imageResourceId, StructureType structureType, int cost) {
        this.imageResourceId = imageResourceId;
        this.structureType = structureType;
        this.cost = cost;
    }
}

public class BuildButtonAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<BuildButtonItem> mItems = new ArrayList<>();
    private final OnBuildItemClickListener mListener;

    public interface OnBuildItemClickListener {
        void onBuildStuctureClick(StructureType structureType);
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

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            holder = new BuildButtonViewHolder(view, parent);
            view.setTag(holder);
        } else {
            holder = (BuildButtonViewHolder) view.getTag();
        }

        holder.mItem = mItems.get(position);

        holder.mImageButton.setImageResource(holder.mItem.imageResourceId);
        holder.mName.setText(holder.mItem.structureType.getName());
        holder.mCost.setText(String.valueOf(holder.mItem.cost));

        return view;
    }

    public class BuildButtonViewHolder implements View.OnClickListener {
        public BuildButtonItem mItem;
        public ImageButton mImageButton;
        public TextView mName;
        public TextView mCost;

        public BuildButtonViewHolder(View view, ViewGroup parent) {
            mImageButton = (ImageButton) view.findViewById(R.id.grid_item_image_btn);
            mName = (TextView) view.findViewById(R.id.grid_item_name_text);
            mCost = (TextView) view.findViewById(R.id.grid_item_cost_text);

            mImageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onBuildStuctureClick(mItem.structureType);
        }
    }
}
