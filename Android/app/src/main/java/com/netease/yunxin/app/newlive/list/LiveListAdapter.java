package com.netease.yunxin.app.newlive.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.newlive.Constants;
import com.netease.yunxin.app.newlive.R;
import com.netease.yunxin.app.newlive.utils.SpUtils;
import com.netease.yunxin.app.newlive.utils.StringUtils;
import com.netease.yunxin.kit.livekit.NELiveConstants;
import com.netease.yunxin.kit.livekit.model.LiveInfo;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LiveListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int VIEW_TYPE_ITEM = 1;

    private final static int VIEW_TYPE_EMPTY = 0;

    private Context context;

    private ArrayList<LiveInfo> liveInfos = new ArrayList<>();

    private OnItemClickListener onItemClickListener = null;

    public LiveListAdapter(Context context) {
        this.context = context;
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class LiveItemHolder extends RecyclerView.ViewHolder {

        public ImageView ivRoomPic;

        public ImageView ivPkTag;

        public TextView tvAnchorName;

        public TextView tvRoomName;

        public TextView tvAudienceNum;

        public LiveItemHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomPic = itemView.findViewById(R.id.iv_room_pic);
            ivPkTag = itemView.findViewById(R.id.iv_pk_tag);
            tvAnchorName = itemView.findViewById(R.id.tv_anchor_name);
            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvAudienceNum = itemView.findViewById(R.id.tv_audience_num);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_layout, parent,
                                                                              false);
            return new EmptyViewHolder(emptyView);
        }
        View roomView = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_item_layout, parent, false);
        return new LiveItemHolder(roomView);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LiveItemHolder) {
            LiveInfo liveInfo = liveInfos.get(position);
            ((LiveItemHolder) holder).tvRoomName.setText(liveInfo.live.getLiveTopic());
            ((LiveItemHolder) holder).tvAnchorName.setText(liveInfo.anchor.getUserName());
            ((LiveItemHolder) holder).tvAudienceNum.setText(
                    StringUtils.INSTANCE.getAudienceCount(liveInfo.live.getAudienceCount()));
            ImageLoader.with(context.getApplicationContext()).roundedCorner(liveInfo.live.getCover(),
                                                                            SpUtils.INSTANCE.dp2pix(4f),
                                                                            ((LiveItemHolder) holder).ivRoomPic);

            if (liveInfo.live.getLive() == NELiveConstants.LiveStatus.LIVE_STATUS_PKING ||
                liveInfo.live.getLive() == NELiveConstants.LiveStatus.LIVE_STATUS_ON_PUNISHMENT){
                ((LiveItemHolder) holder).ivPkTag.setVisibility(View.VISIBLE);
                ((LiveItemHolder) holder).ivPkTag.setImageResource(R.drawable.pk_icon);
            } else if(liveInfo.live.getLive() == NELiveConstants.LiveStatus.LIVE_STATUS_ON_SEAT){
                ((LiveItemHolder) holder).ivPkTag.setVisibility(View.VISIBLE);
                ((LiveItemHolder) holder).ivPkTag.setImageResource(R.drawable.icon_status_multi_micro);
            }else{
                ((LiveItemHolder) holder).ivPkTag.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if(onItemClickListener != null) {
                        onItemClickListener.onItemClick(liveInfos, position);
                    }
                }
            });
        }
    }

    /**
     * 判断是否是空布局
     */
    public boolean isEmptyPosition(int position){
        return position == 0 && liveInfos.isEmpty();
    }

    /**
     * 更新数据
     *
     * @param liveInfoList
     * @param isRefresh
     */
    public void setDataList(List<LiveInfo> liveInfoList, Boolean isRefresh) {
        if (isRefresh) {
            liveInfos.clear();
        }
        if (liveInfoList != null && liveInfoList.size() != 0) {
            liveInfos.addAll(liveInfoList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if ( liveInfos.size() > 0) {
            return liveInfos.size();
        } else {
            return 1;
        }
    }

    public int getItemViewType(int position) {
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        if (liveInfos.size() == 0) {
            return VIEW_TYPE_EMPTY;
        } else {
            //如果有数据，则使用ITEM的布局
            return VIEW_TYPE_ITEM;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ArrayList<LiveInfo> liveList, int position);
    }
}
