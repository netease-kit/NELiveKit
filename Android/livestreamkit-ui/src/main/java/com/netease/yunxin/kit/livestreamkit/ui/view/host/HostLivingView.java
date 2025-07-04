// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.host;

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
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.InputUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ViewUtils;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.livestreamkit.ui.cohost.dialog.*;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.LiveAnchorLivingLayoutBinding;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.LiveStreamUtils;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.model.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.*;
import kotlin.Unit;

public class HostLivingView extends BaseLivingView {
  private final String TAG = "HostLivingView";
  protected static final String CO_HOST_INVITED_DIALOG_TAG = "CoHostInvitedDialog";
  private LiveAnchorLivingLayoutBinding binding;
  private HostPreviewView.OnBeautyClickListener onBeautyClickListener;
  private CoHostInviteDialog coHostInviteDialog = null;
  private CoHostInvitedDialog coHostInvitedDialog = null;

  public HostLivingView(@NonNull Context context) {
    this(context, null);
  }

  public HostLivingView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HostLivingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    startUpdateAudienceList();
  }

  @Override
  protected void onLivingResume() {
    binding.clPauseLiving.setVisibility(View.GONE);
    if (moreItems != null && !moreItems.isEmpty() && moreItems.get(MORE_ITEM_MICRO_PHONE) != null) {
      moreItems.get(MORE_ITEM_MICRO_PHONE).setEnable(true);
      moreItems
          .get(MORE_ITEM_MICRO_PHONE)
          .setName(getContext().getString(R.string.live_pause_living));
    }
    if (moreItemsDialog != null) {
      moreItemsDialog.updateData();
    }
  }

  @Override
  protected void onLivingPause() {
    binding.clPauseLiving.setVisibility(View.VISIBLE);
    if (moreItems != null && !moreItems.isEmpty() && moreItems.get(MORE_ITEM_MICRO_PHONE) != null) {
      moreItems.get(MORE_ITEM_MICRO_PHONE).setEnable(false);
      moreItems
          .get(MORE_ITEM_MICRO_PHONE)
          .setName(getContext().getString(R.string.live_resume_living));
    }
    if (moreItemsDialog != null) {
      moreItemsDialog.updateData();
    }
  }

  @Override
  protected void initView() {
    super.initView();
    binding = LiveAnchorLivingLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
    ViewUtils.paddingStatusBarHeight((Activity) getContext(), binding.clContent);

    // 初始化底部按钮点击事件
    initBottomTools();

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

    binding.ivOrderSong.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            showSingingTable();
          }
        });

    binding.resumeLiving.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            NELiveStreamKit.getInstance().resumeLive(null);
          }
        });
    binding.ivPk.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            coHostInviteDialog = new CoHostInviteDialog((Activity) getContext());
            coHostInviteDialog.show();
          }
        });
    binding.ivGift.setVisibility(View.GONE);
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

    // 初始化观众头像
    ImageView[] audienceAvatars =
        new ImageView[] {
          binding.ivAudienceAvatar1, binding.ivAudienceAvatar2, binding.ivAudienceAvatar3
        };
    for (ImageView avatar : audienceAvatars) {
      avatar.setVisibility(View.GONE);
    }
  }

  private void initBottomTools() {
    // PK按钮点击事件
    binding.ivPk.setOnClickListener(
        v -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.network_error);
            return;
          }
          showPkDialog();
        });

    // 美颜按钮点击事件
    binding.ivBeauty.setOnClickListener(
        v -> {
          if (onBeautyClickListener != null) {
            onBeautyClickListener.onBeautyClick();
          }
        });
  }

  private void showPkDialog() {
    // 显示PK邀请对话框
    //    NELiveStreamKit.getInstance().getPkService().getPkableRoomList(1, 20, new NELiveStreamCallback<NEPkableRoomList>() {
    //      @Override
    //      public void onSuccess(@Nullable NEPkableRoomList pkableRoomList) {
    //        if (pkableRoomList != null && !pkableRoomList.getList().isEmpty()) {
    //          // 显示可PK主播列表对话框
    //          new PkInviteDialog((Activity) getContext(), pkableRoomList.getList())
    //              .setOnPkInviteListener((anchor) -> {
    //                // 发送PK邀请
    //                NELiveStreamKit.getInstance().getPkService().sendPkInvite(
    //                    anchor.getRoomUuid(),
    //                    anchor.getAnchorUuid(),
    //                    new NELiveStreamCallback<Unit>() {
    //                      @Override
    //                      public void onSuccess(@Nullable Unit unit) {
    //                        ToastX.showShortToast(R.string.pk_invite_sent);
    //                      }
    //
    //                      @Override
    //                      public void onFailure(int code, @Nullable String msg) {
    //                        ToastX.showShortToast(R.string.pk_invite_failed);
    //                      }
    //                    });
    //              })
    //              .show();
    //        } else {
    //          ToastX.showShortToast(R.string.no_pkable_anchor);
    //        }
    //      }
    //
    //      @Override
    //      public void onFailure(int code, @Nullable String msg) {
    //        ToastX.showShortToast(R.string.get_pkable_list_failed);
    //      }
    //    });
  }

  private void showBeautyDialog() {
    // 显示美颜设置对话框
    //    new BeautyDialog((Activity) getContext())
    //        .setOnBeautySettingListener(new BeautyDialog.OnBeautySettingListener() {
    //          @Override
    //          public void onBeautyValueChanged(int type, float value) {
    //            // 设置美颜参数
    //            NELiveStreamKit.getInstance().getBeautyService().setBeautyParam(type, value);
    //          }
    //        })
    //        .show();
  }

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

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  @Override
  protected void createMoreItems() {
    moreItems =
        Arrays.asList(
            new ChatRoomMoreDialog.MoreItem(
                MORE_ITEM_SWITCH_CAMERA,
                R.drawable.icon_switch_camera,
                getContext().getString(R.string.live_switch_camera)),
            new ChatRoomMoreDialog.MoreItem(
                MORE_ITEM_MICRO_PHONE,
                R.drawable.live_selector_more_micro_phone_status,
                getContext().getString(R.string.live_pause_living)));
    //            new ChatRoomMoreDialog.MoreItem(
    //                MORE_ITEM_SMALL_WINDOW,
    //                R.drawable.icon_small_window,
    //                getContext().getString(R.string.live_small_window)));
  }

  @Override
  protected List<ChatRoomMoreDialog.MoreItem> getMoreItems() {
    boolean isLivePausing = NELiveStreamKit.getInstance().isLivePausing();
    if (moreItems != null && moreItems.get(MORE_ITEM_MICRO_PHONE) != null) {
      moreItems.get(MORE_ITEM_MICRO_PHONE).setEnable(!isLivePausing);
      moreItems
          .get(MORE_ITEM_MICRO_PHONE)
          .setName(
              getContext()
                  .getString(
                      isLivePausing ? R.string.live_resume_living : R.string.live_pause_living));
    }
    return moreItems;
  }

  @Override
  protected void onLivingAudioOutputDeviceChanged(NEAudioOutputDevice device) {
    super.onLivingAudioOutputDeviceChanged(device);
    if (device != NEAudioOutputDevice.BLUETOOTH_HEADSET
        && device != NEAudioOutputDevice.WIRED_HEADSET) {
      //      if (moreItems != null && moreItems.get(MORE_ITEM_SMALL_WINDOW) != null) {
      //        moreItems.get(MORE_ITEM_SMALL_WINDOW).setEnable(false);
      //      }
      if (moreItemsDialog != null) {
        moreItemsDialog.updateData();
      }
      enableEarBack(false);
    }
  }

  @Override
  public void showChatRoomMixerDialog() {
    new ChatRoomMixerDialog((Activity) getContext(), audioPlay, true).show();
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

  public void setMemberCount(String memberCount) {
    binding.tvMemberCount.setText(memberCount);
  }

  public void setOnMoreClickListener(OnClickListener clickListener) {
    binding.ivRoomMore.setOnClickListener(clickListener);
  }

  public void setOnBeautyClickListener(
      HostPreviewView.OnBeautyClickListener onBeautyClickListener) {
    this.onBeautyClickListener = onBeautyClickListener;
  }

  public void setOnLeaveRoomClickListener(OnClickListener clickListener) {
    binding.ivPower.setOnClickListener(clickListener);
  }

  public void setOnLinkMicClickListener(OnClickListener clickListener) {
    binding.ivLinkMic.setOnClickListener(clickListener);
  }

  public ChatRoomMsgRecyclerView getChatRoomMsgRecyclerView() {
    return binding.rcyChatMessageList;
  }

  protected final void toggleMuteLocalAudio() {
    //    if (!joinRoomSuccess) return;
    NERoomMember localMember = NELiveStreamKit.getInstance().getLocalMember();
    if (localMember == null) return;
    boolean isAudioOn = localMember.isAudioOn();
    ALog.d(
        TAG,
        "toggleMuteLocalAudio,localMember.isAudioOn:"
            + isAudioOn
            + ",localMember.isAudioBanned():"
            + localMember.isAudioBanned());
    if (isAudioOn) {
      muteMyAudio(
          new NELiveStreamCallback<Unit>() {
            @Override
            public void onSuccess(@Nullable Unit unit) {
              ToastX.showShortToast(R.string.live_mic_off);
            }

            @Override
            public void onFailure(int code, @Nullable String msg) {}
          });
    } else {
      unmuteMyAudio(
          new NELiveStreamCallback<Unit>() {
            @Override
            public void onSuccess(@Nullable Unit unit) {
              ToastX.showShortToast(R.string.live_mic_on);
            }

            @Override
            public void onFailure(int code, @Nullable String msg) {}
          });
    }
  }

  public void unmuteMyAudio(NELiveStreamCallback<Unit> callback) {
    NELiveStreamKit.getInstance().unmuteMyAudio(callback);
  }

  public void muteMyAudio(NELiveStreamCallback<Unit> callback) {
    NELiveStreamKit.getInstance().muteMyAudio(callback);
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
  protected void onLivingMemberAudioMuteChanged(
      NERoomMember member, boolean mute, NERoomMember operateBy) {
    if (LiveStreamUtils.isMySelf(member.getUuid())) {
      if (moreItemsDialog != null) {
        moreItemsDialog.updateData();
      }
    }
  }

  @Override
  protected void onLivingSeatRequestSubmitted() {
    refreshRedDot();
  }

  @Override
  protected void onLocalSeatRequestApproved() {
    refreshRedDot();
  }

  @Override
  protected void onRemoteSeatRequestApproved(NESeatRequestItem requestItem, NERoomUser operateBy) {
    refreshRedDot();
  }

  @Override
  protected void onLivingSeatRequestCancelled() {
    refreshRedDot();
  }

  @Override
  protected void onRemoteSeatRequestRejected() {
    refreshRedDot();
  }

  @Override
  protected void onLivingSeatInvitationRejected(@NonNull NESeatInvitationItem invitationItem) {
    ToastX.showShortToast(R.string.live_audience_reject_link_seats_invited);
  }

  @Override
  protected void onLivingConnectionRequestReceived(@NonNull ConnectionUser inviter) {
    LiveRoomLog.i(TAG, "onLivingConnectionRequestReceived inviter = " + inviter);
    if (coHostInviteDialog != null && coHostInviteDialog.isShowing()) {
      coHostInviteDialog.dismiss();
    }
    coHostInvitedDialog =
        new CoHostInvitedDialog(
            inviter.getName(),
            new TimeoutDialog.OnActionListener() {
              @Override
              public void onConfirm() {
                NELiveStreamKit.getInstance()
                    .getCoHostManager()
                    .acceptConnection(
                        inviter.getRoomUuid(),
                        new NELiveStreamCallback<Unit>() {
                          @Override
                          public void onSuccess(@Nullable Unit unit) {
                            coHostInvitedDialog.dismiss();
                          }

                          @Override
                          public void onFailure(int code, @Nullable String msg) {}
                        });
              }

              @Override
              public void onCancel() {
                NELiveStreamKit.getInstance()
                    .getCoHostManager()
                    .rejectConnection(
                        inviter.getRoomUuid(),
                        new NELiveStreamCallback<Unit>() {
                          @Override
                          public void onSuccess(@Nullable Unit unit) {}

                          @Override
                          public void onFailure(int code, @Nullable String msg) {}
                        });
              }
            });
    coHostInvitedDialog.setCancelable(false);
    coHostInvitedDialog.show(
        ((AppCompatActivity) getContext()).getSupportFragmentManager(), CO_HOST_INVITED_DIALOG_TAG);
  }

  @Override
  protected void onLivingConnectionRequestCancelled(@NonNull ConnectionUser inviter) {
    if (coHostInvitedDialog != null && coHostInvitedDialog.isVisible()) {
      coHostInvitedDialog.dismiss();
    }
  }

  @Override
  protected void onLivingConnectionRequestAccept(@NonNull ConnectionUser invitee) {
    if (coHostInviteDialog != null && coHostInviteDialog.isShowing()) {
      coHostInviteDialog.dismiss();
    }
  }

  @Override
  protected void onLivingConnectionUserListChanged(
      @NonNull List<ConnectionUser> connectedList,
      @NonNull List<ConnectionUser> joinedList,
      @NonNull List<ConnectionUser> leavedList) {
    LiveRoomLog.i(TAG, "onLivingConnectionUserListChanged connectedList = " + connectedList);
    if (connectedList.isEmpty()) {
      if (coHostInviteDialog != null && coHostInviteDialog.isShowing()) {
        coHostInviteDialog.dismiss();
      }
    }
  }

  private void showSingingTable() {
    if (!ClickUtils.isSlightlyFastClick()) {
      if (!NetworkUtils.isConnected()) {
        return;
      }
    }
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

  private void refreshRedDot() {
    getSeatRequestList(
        new NELiveStreamCallback<List<SeatView.SeatInfo>>() {
          @Override
          public void onSuccess(@Nullable List<SeatView.SeatInfo> seatInfos) {
            if (seatInfos == null || seatInfos.isEmpty()) {
              binding.ivLinkMicRedDot.setVisibility(View.GONE);
            } else {
              binding.ivLinkMicRedDot.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {}
        });
  }

  protected void getSeatRequestList(NELiveStreamCallback<List<SeatView.SeatInfo>> callback) {
    NELiveStreamKit.getInstance()
        .getSeatRequestList(
            new NELiveStreamCallback<List<NELiveRoomSeatRequestItem>>() {
              @Override
              public void onSuccess(@Nullable List<NELiveRoomSeatRequestItem> seatRequestItems) {
                LiveRoomLog.i(
                    TAG, "getSeatRequestList success seatRequestItems = " + seatRequestItems);
                List<SeatView.SeatInfo> applySeatList = new ArrayList<>();
                if (seatRequestItems != null) {
                  for (NELiveRoomSeatRequestItem item : seatRequestItems) {
                    if (item != null) {
                      applySeatList.add(
                          new SeatView.SeatInfo(
                              item.getUser(), item.getUserName(), item.getIcon()));
                    }
                  }
                }

                if (callback != null) {
                  callback.onSuccess(applySeatList);
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "getSeatRequestList failed msg = " + msg);
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }
}
