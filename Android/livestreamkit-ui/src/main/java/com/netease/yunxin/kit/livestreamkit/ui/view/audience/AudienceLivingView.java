// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.audience;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.*;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.utils.InputUtils;
import com.netease.yunxin.kit.entertainment.common.utils.StringUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ViewUtils;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfo;
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfoList;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.LiveAudienceLivingLayoutBinding;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.LiveStreamUtils;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.*;
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage;
import java.util.List;
import kotlin.*;

public class AudienceLivingView extends BaseLivingView {
  private final String TAG = "AudienceLivingView";
  private LiveAudienceLivingLayoutBinding binding;
  private AudienceLiveViewModel liveViewModel;
  protected static final String AUDIENCE_LINK_SEAT_DIALOG_TAG = "audienceLinkSeatDialog";
  protected AudienceLinkSeatDialog audienceLinkSeatDialog = null;
  protected static final String AUDIENCE_LINK_SEAT_INVITE_DIALOG_TAG =
      "AudienceLinkMicInviteDialog";
  private AudienceLinkSeatInviteDialog linkSeatInviteDialog = null;

  public AudienceLivingView(@NonNull Context context) {
    this(context, null);
  }

  public AudienceLivingView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AudienceLivingView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    startUpdateAudienceList();
  }

  @Override
  protected void onLivingResume() {
    binding.clPauseLiving.setVisibility(View.GONE);
  }

  @Override
  protected void onLivingPause() {
    binding.clPauseLiving.setVisibility(View.VISIBLE);
  }

  @Override
  protected void initView() {
    super.initView();
    binding =
        LiveAudienceLivingLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
    ViewUtils.paddingStatusBarHeight((Activity) getContext(), binding.clContent);
    if (getContext() instanceof AppCompatActivity) {
      liveViewModel =
          new ViewModelProvider((AppCompatActivity) getContext()).get(AudienceLiveViewModel.class);
      liveViewModel
          .getLiveStateData()
          .observe(
              (AppCompatActivity) getContext(),
              liveState -> {
                if (liveState == AudienceLiveViewModel.LIVE_STATE_FINISH) {
                  if (audienceLinkSeatDialog != null) {
                    audienceLinkSeatDialog.dismiss();
                  }

                  if (moreItemsDialog != null) {
                    moreItemsDialog.dismiss();
                  }
                }
              });
    }
    binding.tvInputText.setOnClickListener(
        v -> InputUtils.showSoftInput(getContext(), binding.edtInputText));
    binding.edtInputText.setOnEditorActionListener(
        (v, actionId, event) -> {
          InputUtils.hideSoftInput(getContext(), binding.edtInputText);
          sendTextMessage();
          return true;
        });
    InputUtils.registerSoftInputListener(
        (AppCompatActivity) getContext(),
        new InputUtils.InputParamHelper() {

          @Override
          public int getHeight() {
            return binding.getRoot().getHeight();
          }

          @Override
          public EditText getInputView() {
            return binding.edtInputText;
          }
        });

    binding.ivGift.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.live_reward_failed);
              return;
            }
            showSendGiftDialog();
          }
        });

    binding.ivRoomMore.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            moreItems = getMoreItems();
            moreItemsDialog = new ChatRoomMoreDialog((Activity) getContext(), moreItems);
            moreItemsDialog.registerOnItemClickListener(onMoreItemClickListener);
            moreItemsDialog.show();
          }
        });
    //    seatsLayout = baseAudioView.findViewById(R.id.seats_layout);
    binding.ivLinkMic.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (audienceLinkSeatDialog != null && audienceLinkSeatDialog.isVisible()) {
              return;
            }
            if (audienceLinkSeatDialog == null) {
              audienceLinkSeatDialog = new AudienceLinkSeatDialog();
            }
            if (!audienceLinkSeatDialog.isAdded()
                && ((AppCompatActivity) getContext())
                        .getSupportFragmentManager()
                        .findFragmentByTag(AUDIENCE_LINK_SEAT_DIALOG_TAG)
                    == null) {
              audienceLinkSeatDialog.show(
                  ((AppCompatActivity) getContext()).getSupportFragmentManager(),
                  AUDIENCE_LINK_SEAT_DIALOG_TAG);
            } else {
              audienceLinkSeatDialog.dismiss();
            }
          }
        });
  }

  @Override
  protected void createMoreItems() {
    moreItems =
        List.of(
            new ChatRoomMoreDialog.MoreItem(
                MORE_ITEM_SWITCH_CAMERA,
                R.drawable.icon_switch_camera,
                getContext().getString(R.string.live_switch_camera)));
  }

  public void setLiveName(String roomName) {
    binding.tvLiveName.setText(roomName);
  }

  public void setLiveId(String liveId) {
    binding.tvLiveId.setText(liveId);
  }

  public void setAnchorAvatar(String url) {
    ImageLoader.with(getContext()).circleLoad(url, binding.ivAnchorAvatar);
  }

  public void setMemberCount(int memberCount) {
    binding.tvMemberCount.setText(StringUtils.getAudienceCount(memberCount));
  }

  public void setOnSendGiftClickListener(OnClickListener clickListener) {
    binding.ivGift.setOnClickListener(clickListener);
  }

  public void setOnMoreClickListener(OnClickListener clickListener) {
    binding.ivRoomMore.setOnClickListener(clickListener);
  }

  public void setOnLeaveRoomClickListener(OnClickListener clickListener) {
    binding.ivPower.setOnClickListener(clickListener);
  }

  @Override
  protected List<ChatRoomMoreDialog.MoreItem> getMoreItems() {
    return moreItems;
  }

  @Override
  public void showChatRoomMixerDialog() {
    new ChatRoomMixerDialog((Activity) getContext(), audioPlay, false).show();
  }

  @Override
  protected void updateAudienceAvatars(NEAudienceInfoList audienceList) {
    binding.tvMemberCount.setText(String.valueOf(audienceList.getTotal()));
    ImageView[] avatarViews =
        new ImageView[] {
          binding.ivAudienceAvatar1, binding.ivAudienceAvatar2, binding.ivAudienceAvatar3
        };
    List<NEAudienceInfo> members = audienceList.getList();
    for (int i = 0; i < avatarViews.length; i++) {
      if (i < members.size()) {
        NEAudienceInfo member = members.get(i);
        avatarViews[i].setVisibility(View.VISIBLE);
        ImageLoader.with(getContext()).circleLoad(member.getIcon(), avatarViews[i]);
      } else {
        avatarViews[i].setVisibility(View.GONE);
      }
    }
  }

  protected void sendTextMessage() {
    String content = binding.edtInputText.getText().toString().trim();
    if (TextUtils.isEmpty(content)) {
      ToastX.showShortToast(R.string.live_chat_message_tips);
      return;
    }
    NELiveStreamKit.getInstance()
        .sendTextMessage(
            content,
            new NELiveStreamCallback<NERoomChatMessage>() {
              @Override
              public void onSuccess(@Nullable NERoomChatMessage unit) {
                binding.rcyChatMessageList.appendItem(
                    ChatRoomMsgCreator.createText(
                        getContext(),
                        LiveStreamUtils.isCurrentHost(),
                        LiveStreamUtils.getLocalName(),
                        content));
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "sendTextMessage failed code = " + code + " msg = " + msg);
              }
            });
  }

  @Override
  protected void onLocalSeatRequestApproved() {
    if (linkSeatInviteDialog != null && linkSeatInviteDialog.isVisible()) {
      linkSeatInviteDialog.dismiss();
    }
  }

  @Override
  protected void onLivingSeatInvitationReceived(
      int seatIndex, @NonNull String account, @NonNull String operateBy) {

    if (!TextUtils.equals(account, LiveStreamUtils.getLocalAccount())) {
      LiveRoomLog.i(
          TAG,
          "onLivingSeatInvitationReceived account = "
              + account
              + ", current account = "
              + LiveStreamUtils.getLocalAccount());
      return;
    }

    // 防止重复弹出
    if (linkSeatInviteDialog != null && linkSeatInviteDialog.isVisible()) {
      return;
    }

    String showName = operateBy;
    if (LiveStreamUtils.getHost() != null
        && !TextUtils.isEmpty(LiveStreamUtils.getHost().getName())) {
      showName = LiveStreamUtils.getHost().getName();
    }
    linkSeatInviteDialog =
        new AudienceLinkSeatInviteDialog(
            showName,
            new AudienceLinkSeatInviteDialog.OnActionListener() {
              @Override
              public void onConfirm() {
                NELiveStreamKit.getInstance()
                    .acceptSeatInvitation(
                        new NELiveStreamCallback<Unit>() {
                          @Override
                          public void onSuccess(@Nullable Unit unit) {
                            linkSeatInviteDialog.dismiss();
                          }

                          @Override
                          public void onFailure(int code, @Nullable String msg) {
                            LiveRoomLog.e(TAG, "acceptSeatInvitation failed code = " + code);
                            ToastX.showShortToast(
                                msg == null ? getContext().getString(R.string.network_error) : msg);
                          }
                        });
              }

              @Override
              public void onCancel() {
                NELiveStreamKit.getInstance()
                    .rejectSeatInvitation(
                        new NELiveStreamCallback<Unit>() {
                          @Override
                          public void onSuccess(@Nullable Unit unit) {}

                          @Override
                          public void onFailure(int code, @Nullable String msg) {}
                        });
              }
            });
    linkSeatInviteDialog.setCancelable(false);
    linkSeatInviteDialog.setOnDismissListener(dialog -> linkSeatInviteDialog = null);
    linkSeatInviteDialog.show(
        ((AppCompatActivity) getContext()).getSupportFragmentManager(),
        AUDIENCE_LINK_SEAT_INVITE_DIALOG_TAG);
  }

  @Override
  protected void onLocalSeatRequestRejected() {
    ToastX.showShortToast(R.string.live_anchor_reject_link_seats_request);
  }

  @Override
  protected void onLocalSeatKicked() {
    ToastX.showShortToast(R.string.live_has_been_kicked_seat);
  }

  public ChatRoomMsgRecyclerView getChatRoomMsgRecyclerView() {
    return binding.rcyChatMessageList;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    int x = (int) ev.getRawX();
    int y = (int) ev.getRawY();
    // 键盘区域外点击收起键盘
    if (!ViewUtils.isInView(binding.edtInputText, x, y)) {
      InputUtils.hideSoftInput(getContext(), binding.edtInputText);
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (moreItemsDialog != null) {
      moreItemsDialog.dismiss();
    }
  }
}
