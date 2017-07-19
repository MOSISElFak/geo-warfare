package rs.elfak.jajac.geowarfare.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.FriendsFragment.OnListFragmentInteractionListener;
import rs.elfak.jajac.geowarfare.models.FriendRequestModel;

import java.util.List;

public class FriendRequestRecyclerViewAdapter extends
        RecyclerView.Adapter<FriendRequestRecyclerViewAdapter.ViewHolder> {

    private final List<FriendRequestModel> mValues;
    private final OnListFragmentInteractionListener mListener;

    public FriendRequestRecyclerViewAdapter(List<FriendRequestModel> items,
                                            OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mFullName.setText(holder.mItem.fullName);
        holder.mDisplayName.setText(holder.mItem.displayName);

        // We need to get context somehow to be able to use Glide here
        Context context = holder.mAvatarImg.getContext();
        Glide.with(context)
                .load(holder.mItem.avatarUrl)
                .into(holder.mAvatarImg);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onFriendItemClick(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mFullName;
        public final TextView mDisplayName;
        public final ImageView mAvatarImg;
        public FriendRequestModel mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mFullName = (TextView) view.findViewById(R.id.friend_request_item_full_name);
            mDisplayName = (TextView) view.findViewById(R.id.friend_request_item_display_name);
            mAvatarImg = (ImageView) view.findViewById(R.id.friend_request_item_avatar_img);
        }

    }
}
