// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.entertainment.common.databinding.ItemVoiceRoomListBinding;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomInfo;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LiveStreamListAdapter
    extends RecyclerView.Adapter<LiveStreamListAdapter.RoomViewHolder> {
  protected final Context context;
  private final List<NELiveRoomInfo> roomInfoList;
  private static LiveStreamListAdapter.OnItemClickListener itemOnClickListener;

  public LiveStreamListAdapter(Context context) {
    this.context = context;
    roomInfoList = new ArrayList<>();
  }

  public void refreshList(List<NELiveRoomInfo> dataList) {
    roomInfoList.clear();
    roomInfoList.addAll(dataList);
    notifyDataSetChanged();
  }

  public void loadMore(List<NELiveRoomInfo> dataList) {
    roomInfoList.addAll(dataList);
    notifyDataSetChanged();
  }

  public boolean isEmptyPosition(int position) {
    return position == 0 && roomInfoList.isEmpty();
  }

  @Override
  public void onBindViewHolder(@NonNull LiveStreamListAdapter.RoomViewHolder holder, int position) {
    NELiveRoomInfo roomInfo = roomInfoList.get(position);
    holder.setData(roomInfo);
  }

  @Override
  public int getItemCount() {
    return roomInfoList.size();
  }

  public static class RoomViewHolder extends RecyclerView.ViewHolder {

    protected final ItemVoiceRoomListBinding binding;
    private final Context context;

    public RoomViewHolder(ItemVoiceRoomListBinding binding, Context context) {
      super(binding.getRoot());
      this.binding = binding;
      this.context = context;
    }

    public void setData(NELiveRoomInfo info) {
      ImageLoader.with(context)
          .load(info.getLiveModel().getCover())
          .error(com.netease.yunxin.kit.entertainment.common.R.drawable.chat_room_default_bg)
          .roundedCornerCenterCrop(SizeUtils.dp2px(4))
          .into(binding.ivChatRoomBg);
      binding.tvChatRoomName.setText(info.getLiveModel().getLiveTopic());
      binding.tvChatRoomAnchorName.setText(info.getAnchor().getNick());
      if (info.getLiveModel().getAudienceCount() != null) {
        binding.tvChatRoomMemberNum.setText(
            getCurrentCount(info.getLiveModel().getAudienceCount()));
      }
      binding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (itemOnClickListener != null) {
                  itemOnClickListener.onClick(info);
                }
              });
    }

    private String getCurrentCount(int count) {
      if (count < 10000) {
        return String.format(
            context.getString(
                com.netease.yunxin.kit.entertainment.common.R.string.voiceroom_people_online2),
            count);
      }
      DecimalFormat decimalFormat = new DecimalFormat("#.#");
      return String.format(
          context.getString(
              com.netease
                  .yunxin
                  .kit
                  .entertainment
                  .common
                  .R
                  .string
                  .voiceroom_people_online_ten_thousand),
          decimalFormat.format(count / 10000.f));
    }
  }

  public void setItemOnClickListener(
      LiveStreamListAdapter.OnItemClickListener itemOnClickListener) {
    LiveStreamListAdapter.itemOnClickListener = itemOnClickListener;
  }

  public interface OnItemClickListener {
    void onClick(NELiveRoomInfo info);
  }

  @NonNull
  @Override
  public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemVoiceRoomListBinding binding =
        ItemVoiceRoomListBinding.inflate(LayoutInflater.from(context), parent, false);
    return new VoiceRoomViewHolder(binding, context);
  }

  public static class VoiceRoomViewHolder extends RoomViewHolder {

    public VoiceRoomViewHolder(ItemVoiceRoomListBinding binding, Context context) {
      super(binding, context);
    }

    @Override
    public void setData(NELiveRoomInfo info) {
      super.setData(info);
      binding.ivType.setVisibility(View.GONE);
    }
  }
}
