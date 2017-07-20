package rs.elfak.jajac.geowarfare.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.FriendsFragment.OnListFragmentInteractionListener;
import rs.elfak.jajac.geowarfare.models.FriendModel;

import java.util.List;

public class FriendRecyclerViewAdapter extends
        RecyclerView.Adapter<FriendRecyclerViewAdapter.FriendViewHolder> {

    private final List<FriendModel> mValues;
    private final OnListFragmentInteractionListener mListener;

    public FriendRecyclerViewAdapter(List<FriendModel> items,
                                     OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_list_item, parent, false);
        return new FriendViewHolder(view, parent);
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mFullName.setText(holder.mItem.fullName);
        holder.mDisplayName.setText(holder.mItem.displayName);

        // We need to get context somehow to be able to use Glide here
        Context context = holder.mAvatarImg.getContext();
        Glide.with(context)
                .load(holder.mItem.avatarUrl)
                .into(holder.mAvatarImg);

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
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

    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mItemView;
        public final TextView mFullName;
        public final TextView mDisplayName;
        public final ImageView mAvatarImg;
        public FriendModel mItem;

        public FriendViewHolder(View itemView, ViewGroup parent) {
            super(itemView);
            mItemView = itemView;

            mFullName = (TextView) itemView.findViewById(R.id.friend_request_item_full_name);
            mDisplayName = (TextView) itemView.findViewById(R.id.friend_request_item_display_name);
            mAvatarImg = (ImageView) itemView.findViewById(R.id.friend_request_item_avatar_img);
            Button acceptButton = (Button) itemView.findViewById(R.id.friend_request_item_accept_btn);
            Button declineButton = (Button) itemView.findViewById(R.id.friend_request_item_decline_btn);

            // If it's a "friendRequest" item, we want to use the Accept/Decline buttons
            // that are invisible for the "friend" items
            if (parent.getId() == R.id.fragment_friend_request_list) {
                acceptButton.setOnClickListener(this);
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setOnClickListener(this);
                declineButton.setVisibility(View.VISIBLE);
            }
            mItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            FriendModel friendItem = mValues.get(getAdapterPosition());

            if (viewId == mItemView.getId()) {
                mListener.onFriendItemClick(friendItem);
            } else if (viewId == R.id.friend_request_item_accept_btn) {
                mListener.onFriendRequestAccept(friendItem);
            } else if (viewId == R.id.friend_request_item_decline_btn) {
                mListener.onFriendRequestDecline(friendItem);
            }
        }
    }
}
