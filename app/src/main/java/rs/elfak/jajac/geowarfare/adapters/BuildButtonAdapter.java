package rs.elfak.jajac.geowarfare.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;


public class BuildButtonAdapter extends BaseAdapter {

    private Context mContext;
    private Integer[] mThumbId = {
            R.drawable.ic_gold_cart,
            R.drawable.ic_barracks
    };
    private Integer[] mNameId = {
            R.string.build_gold_mine_text,
            R.string.build_barracks_text
    };
    private int[] mCostId = {
            GoldMineModel.COST,
            2000
    };

    public BuildButtonAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mThumbId.length;
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
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            view.setTag(R.id.grid_item_image_btn, view.findViewById(R.id.grid_item_image_btn));
            view.setTag(R.id.grid_item_name_text, view.findViewById(R.id.grid_item_name_text));
            view.setTag(R.id.grid_item_cost_text, view.findViewById(R.id.grid_item_cost_text));
        }

        ImageButton button = (ImageButton) view.getTag(R.id.grid_item_image_btn);
        TextView name = (TextView) view.getTag(R.id.grid_item_name_text);
        TextView cost = (TextView) view.getTag(R.id.grid_item_cost_text);

        button.setImageResource(mThumbId[position]);
        name.setText(mNameId[position]);
        cost.setText(Integer.toString(mCostId[position]));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Build this shiat!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
