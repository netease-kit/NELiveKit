package com.netease.yunxin.app.newlive.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.newlive.Constants;
import com.netease.yunxin.app.newlive.R;
import com.netease.yunxin.app.newlive.databinding.PkLiveAnchorLayoutBinding;
import com.netease.yunxin.app.newlive.dialog.AnchorListDialog;
import com.netease.yunxin.app.newlive.dialog.ChoiceDialog;
import com.netease.yunxin.app.newlive.viewmodel.PkLiveViewModel;
import com.netease.yunxin.app.newlive.widget.AnchorActionView;
import com.netease.yunxin.app.newlive.widget.PKControlView;
import com.netease.yunxin.app.newlive.widget.PKVideoView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.livekit.NELiveCallback;
import com.netease.yunxin.kit.livekit.NELiveConstants;
import com.netease.yunxin.kit.livekit.NEStartLiveOptions;
import com.netease.yunxin.kit.livekit.model.AnchorPkInfo;
import com.netease.yunxin.kit.livekit.model.LiveInfo;
import com.netease.yunxin.kit.livekit.model.LiveStreamTaskRecorder;
import com.netease.yunxin.kit.livekit.model.RewardMsg;
import com.netease.yunxin.kit.livekit.model.pk.PkActionMsg;
import com.netease.yunxin.kit.livekit.model.pk.PkEndInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkPunishInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkStartInfo;
import com.netease.yunxin.kit.livekit.model.pk.NEPKUser;
import com.netease.yunxin.kit.livekit.model.reward.AnchorRewardInfo;
import com.netease.yunxin.kit.roomkit.api.NECallback2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import kotlin.Unit;

import static com.netease.yunxin.app.newlive.viewmodel.PkLiveViewModel.PK_STATE_AGREED;
import static com.netease.yunxin.app.newlive.viewmodel.PkLiveViewModel.PK_STATE_IDLE;
import static com.netease.yunxin.app.newlive.viewmodel.PkLiveViewModel.PK_STATE_PKING;
import static com.netease.yunxin.app.newlive.viewmodel.PkLiveViewModel.PK_STATE_PUNISH;
import static com.netease.yunxin.app.newlive.viewmodel.PkLiveViewModel.PK_STATE_REQUEST;

public class AnchorPkLiveActivity extends AnchorBaseLiveActivity {

    private final static String TAG = "AnchorPkLiveActivity";

    private final static String ANCHOR_LIST_DIALOG_TAG = "anchorListDialog";

    //*******************直播参数*******************
    //视频分辨率
    private int videoWidth = 960;

    private int videoHeight = 540;

    //码率
    private NELiveConstants.NELiveVideoFrameRate frameRate = NELiveConstants.NELiveVideoFrameRate.FRAME_RATE_FPS_15;

    //音频标准
    private NELiveConstants.AudioScenario audioScenario = NELiveConstants.AudioScenario.MUSIC;

    private PkLiveViewModel pkViewModel;

    private PkLiveAnchorLayoutBinding pkViewBind;

    private PKVideoView pkVideoView = null;

    private PKControlView.WrapperCountDownTimer countDownTimer = null;

    private ChoiceDialog pkRequestDialog = null;

    private ChoiceDialog pkInviteedDialog = null;

    private ChoiceDialog stopPkDialog = null;

    private AnchorListDialog anchorListDialog = null;

    private boolean selfStopPk = false;

    private NEPKUser otherAnchor = null;

    private final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pkViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(
                PkLiveViewModel.class);
        pkViewBind = PkLiveAnchorLayoutBinding.inflate(getLayoutInflater(), baseViewBinding.flyContainer, true);
        if (pkViewBind.pkControlView.getVideoContainer() != null) {
            pkViewBind.pkControlView.getVideoContainer().removeAllViews();
        }

    }
    @Override
    protected void initContainer() {
    }


    @Override
    protected void setListener() {
        super.setListener();
        pkViewBind.ivRequestPk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (pkViewModel.getPkState()) {
                    case PK_STATE_IDLE:
                        showAnchorListDialog();
                        break;
                    case PK_STATE_PKING:
                    case PK_STATE_PUNISH:
                        showStopPkDialog();
                        break;
                    default:
                        ToastUtils.showShort(R.string.biz_live_is_pking_please_try_again_later);
                }
            }
        });
    }

    @Override
    protected void initView() {
        super.initView();
        baseViewBinding.rlyConnect.setVisibility(View.GONE);
    }

    @Override
    protected void onRoomLiveStart() {
        super.onRoomLiveStart();
        pkViewModel.init();
        observePkData();
    }

    /**
     * 展示主播列表供选择
     */
    private void showAnchorListDialog() {
        if (anchorListDialog != null && anchorListDialog.isVisible()) {
            return;
        }
        if (anchorListDialog == null) {
            anchorListDialog = new AnchorListDialog();
        }
        anchorListDialog.setSelectAnchorListener(new AnchorListDialog.SelectAnchorListener() {

            @Override
            public void onAnchorSelect(@NonNull LiveInfo liveInfo) {
                //show pk confirm dialog
                if (pkRequestDialog == null) {
                    pkRequestDialog = new ChoiceDialog(AnchorPkLiveActivity.this).setTitle(
                            getString(R.string.biz_live_invite_pk)).setNegative(getString(R.string.biz_live_cancel),
                                                                                null);
                    pkRequestDialog.setCancelable(false);
                }
                pkRequestDialog.setContent(
                        getString(R.string.biz_live_sure_invite) + "“" + liveInfo.anchor.getUserUuid() + "”" +
                        getString(R.string.biz_live_for_pk)).setPositive(getString(R.string.biz_live_determine),
                                                                         new View.OnClickListener() {

                                                                             @Override
                                                                             public void onClick(View view) {
                                                                                 requestPk(
                                                                                         liveInfo.anchor.getUserUuid(),
                                                                                         liveInfo.anchor.getUserName());
                                                                             }
                                                                         });
                if (!pkRequestDialog.isShowing()) {
                    pkRequestDialog.show();
                }
            }
        });
        if (!anchorListDialog.isAdded() && fm.findFragmentByTag(ANCHOR_LIST_DIALOG_TAG) == null) {
            anchorListDialog.show(fm, ANCHOR_LIST_DIALOG_TAG);
        } else {
            anchorListDialog.dismiss();
        }
    }

    private boolean isInvite = false;

    @Override
    protected void startLive() {
        liveAnchor.startLive(NELiveConstants.LiveType.LIVE_TYPE_PK, baseViewBinding.previewAnchor.getTopic(),
                             baseViewBinding.previewAnchor.getLiveCoverPic(), new NELiveCallback<LiveInfo>() {

                    @Override
                    public void onSuccess(LiveInfo info) {
                        if (info != null) {
                            liveInfo = info;
                            liveBaseViewModel.refreshLiveInfo(liveInfo);
                            onRoomLiveStart();
                        }
                    }
                    @Override
                    public void onFailure(int code, String msg) {
                        ALog.e(TAG, "startLive error code = " + code + ", msg = " + msg);
                        ToastUtils.showShort(R.string.biz_live_network_error_try_again);
                        finish();
                    }
                });
    }

    private void observePkData() {
        pkViewModel.getPkActionData().observe(this, new Observer<PkActionMsg>() {

            @Override
            public void onChanged(PkActionMsg pkActionMsg) {
                if (pkActionMsg == null) {
                    return;
                }
                switch (pkActionMsg.getAction()) {
                    case NELiveConstants.PkAction.PK_INVITE:
                        onReceivedPkRequest(pkActionMsg);
                        break;
                    case NELiveConstants.PkAction.PK_ACCEPT:
                        onPkAccept(pkActionMsg);
                        break;
                    case NELiveConstants.PkAction.PK_CANCEL:
                        onPkRequestCancel();
                        break;
                    case NELiveConstants.PkAction.PK_REJECT:
                        onPkRequestRejected();
                        break;
                    case NELiveConstants.PkAction.PK_TIME_OUT:
                        onTimeout();
                        break;
                }
            }
        });
        pkViewModel.getPkStartData().observe(this, new Observer<PkStartInfo>() {

            @Override
            public void onChanged(PkStartInfo pkStartInfo) {
                onPkStart(pkStartInfo);
            }
        });
        pkViewModel.getPunishData().observe(this, new Observer<PkPunishInfo>() {

            @Override
            public void onChanged(PkPunishInfo pkPunishInfo) {
                onPunishStart(pkPunishInfo);
            }
        });
        pkViewModel.getPkEndData().observe(this, new Observer<PkEndInfo>() {

            @Override
            public void onChanged(PkEndInfo pkEndInfo) {
                onPkEnd(pkEndInfo);
            }
        });
        pkViewModel.getPkOtherAnchorJoinedData().observe(this, new Observer<String>() {

            @Override
            public void onChanged(String uuid) {
                if (uuid != null) {
                    if (pkViewModel.getPkState() != PK_STATE_PKING) {
                        liveAnchor.getMediaController().mutePKAudio(uuid, true, new NECallback2<Unit>() {

                            @Override
                            public void onSuccess(@Nullable Unit unit) {
                                ALog.d(TAG, "mutePKAudio success");
                            }
                            @Override
                            public void onError(int code, @Nullable String message) {
                                ALog.e(TAG, "mutePKAudio failed code = " + code + ", message = " + message);
                            }
                        });
                    }
                }
            }
        });
        pkViewModel.getCountDownTimeOutData().observe(this, new Observer<Boolean>() {

            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    onTimeout();
                }
            }
        });
    }

    @Override
    protected void clearLocalImage() {
        super.clearLocalImage();
        if (pkVideoView != null && pkVideoView.getLocalVideo() != null) {
            //todo clearImage
//            pkVideoView.getLocalVideo().clearImage();
        }
    }

    private void onReceivedPkRequest(PkActionMsg action) {
        if (action == null) {
            return;
        }
        int agreeTaskTime = 0;
        int inviteTaskTime = 0;
        if (action.getPkConfig() != null) {
            agreeTaskTime = action.getPkConfig().getAgreeTaskTime();
            inviteTaskTime = action.getPkConfig().getInviteTaskTime();
        }
        action.getActionAnchor();
        isInvite = false;
        //保存本次PK信息
        pkViewModel.setCurrentPkConfig(
                new PkLiveViewModel.PkConfigInfo(action.getPkId(), agreeTaskTime, inviteTaskTime));
        if (pkInviteedDialog == null) {
            pkInviteedDialog = new ChoiceDialog(this).setTitle(getString(R.string.biz_live_invite_pk));
            pkInviteedDialog.setCancelable(false);
        }
        pkInviteedDialog.setContent("“" + action.getActionAnchor().getUserUuid() + "”" +
                                    getString(R.string.biz_live_invite_you_pk_whether_to_accept)).setPositive(
                getString(R.string.biz_live_accept), new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        acceptPkRequest(action.getTargetAnchor().getCheckSum(), action.getTargetAnchor().getRoomUid());
                    }
                }).setNegative(getString(R.string.biz_live_reject), new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                rejectPkRequest();
            }
        });
        if (!pkInviteedDialog.isShowing()) {
            pkInviteedDialog.show();
        }
        if (anchorListDialog != null && anchorListDialog.isVisible()) {
            anchorListDialog.dismiss();
        }
        if (pkRequestDialog != null && pkRequestDialog.isShowing()) {
            pkRequestDialog.dismiss();
        }
    }

    private void onPkRequestCancel() {
        if (pkInviteedDialog != null && pkInviteedDialog.isShowing()) {
            pkInviteedDialog.dismiss();
        }
        ToastUtils.showShort(getString(R.string.biz_live_the_other_cancel_invite));
    }

    private void onPkRequestRejected() {
        ToastUtils.showShort(R.string.biz_live_the_other_party_reject_your_accept);
        pkViewBind.viewAction.hide();

    }

    private void onPkAccept(PkActionMsg action) {
        isInvite = true;
        pkViewBind.viewAction.hide();
        pkViewBind.llyPkProgress.setVisibility(View.VISIBLE);
        if (liveInfo != null && liveInfo.live != null && liveInfo.anchor != null) {
        }

        //todo同意倒计时
//        if (pkViewModel.getCurrentPkConfig() != null) {
//            pkViewModel.startAgreeCountTimer(pkViewModel.getCurrentPkConfig().getAgreeTaskTime(),
//                                             pkViewModel.getCurrentPkConfig().getPkId());
//        }
    }

    /**
     * 结束PK dialog
     */
    private void showStopPkDialog() {
        if (stopPkDialog == null) {
            stopPkDialog = new ChoiceDialog(this);
            stopPkDialog.setTitle(getString(R.string.biz_live_end_pk));
            stopPkDialog.setContent(getString(R.string.biz_live_stop_pk_dialog_content));
            stopPkDialog.setPositive(getString(R.string.biz_live_immediate_end), new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    stopPk();
                }
            });
            stopPkDialog.setNegative(getString(R.string.biz_live_cancel), null);
        }
        stopPkDialog.show();
    }

    private void onPkStart(PkStartInfo startInfo) {
        if (startInfo == null) {
            return;
        }
        pkViewBind.llyPkProgress.setVisibility(View.GONE);
        ImageLoader.with(this).circleLoad(R.drawable.icon_stop_pk, pkViewBind.ivRequestPk);
        otherAnchor = isInvite ? startInfo.getInvitee() : startInfo.getInviter();
        if (pkVideoView == null) {
            pkVideoView = new PKVideoView(this);
        }
        if (pkViewBind.pkControlView.getVideoContainer() != null) {
            pkViewBind.pkControlView.getVideoContainer().removeAllViews();
            pkViewBind.pkControlView.getVideoContainer().addView(pkVideoView);
        }
        if (pkVideoView != null) {
            liveAnchor.getMediaController().bindLocalCanvas(pkVideoView.getLocalVideo());
            liveAnchor.getMediaController().bindRemoteCanvas(pkVideoView.getRemoteVideo(), otherAnchor.getUserUuid());
        }
        baseViewBinding.videoView.setVisibility(View.GONE);
        pkViewBind.pkControlView.setVisibility(View.VISIBLE);
        // pk 控制状态重置
        pkViewBind.pkControlView.reset();
        // 更新对方主播信息
        pkViewBind.pkControlView.updatePkAnchorInfo(otherAnchor.getNickname(), otherAnchor.getAvatar());
        // 开始定时器
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        countDownTimer = pkViewBind.pkControlView.createCountDownTimer(Constants.TYPE_PK,
                                                                       startInfo.getPkCountDown() * 1000L);
        if (countDownTimer != null) {
            countDownTimer.start();
        }
        selfStopPk = false;
        if (pkViewBind.pkControlView.getIvMuteOther() != null) {
            pkViewBind.pkControlView.getIvMuteOther().setVisibility(View.VISIBLE);
        }
        if (pkViewBind.pkControlView.getIvMuteOther() != null) {
            pkViewBind.pkControlView.getIvMuteOther().setSelected(false);
        }
        if (liveInfo != null && liveInfo.anchor != null) {
            liveAnchor.getMediaController().mutePKAudio(otherAnchor.getUserUuid(), false, new NECallback2<Unit>() {

                @Override
                public void onSuccess(@Nullable Unit unit) {
                    ALog.d(TAG, "mutePKAudio success");
                }
                @Override
                public void onError(int code, @Nullable String message) {
                    ALog.e(TAG, "mutePKAudio failed code = " + code + ", message = " + message);
                }
            });
            //set mute button
            if (pkViewBind.pkControlView.getIvMuteOther() != null) {
                pkViewBind.pkControlView.getIvMuteOther().setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        view.setSelected(!view.isSelected());
                        liveAnchor.getMediaController().mutePKAudio(otherAnchor.getUserUuid(), view.isSelected(), new NECallback2<Unit>() {

                            @Override
                            public void onSuccess(@Nullable Unit unit) {
                                ALog.d(TAG, "mutePKAudio success");
                            }
                            @Override
                            public void onError(int code, @Nullable String message) {
                                ALog.e(TAG, "mutePKAudio failed code = " + code + ", message = " + message);
                            }
                        });
                    }
                });
            }
        }
    }

    private void onPunishStart(PkPunishInfo punishInfo) {
        if (punishInfo == null) {
            return;
        }
        // 发送 pk 结束消息
        int anchorWin = 0;
        if (punishInfo.getInviteeRewards() == punishInfo.getInviterRewards()) {
            anchorWin = 0;
        } else if (!isInvite) {
            if (punishInfo.getInviteeRewards() > punishInfo.getInviterRewards()) {
                anchorWin = 1;
            } else {
                anchorWin = -1;
            }
        } else {
            if (punishInfo.getInviteeRewards() < punishInfo.getInviterRewards()) {
                anchorWin = 1;
            } else {
                anchorWin = -1;
            }
        } // 当前主播是否 pk 成功
        // 展示pk结果
        pkViewBind.pkControlView.handleResultFlag(true, anchorWin);
        // 惩罚开始倒计时
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        if (anchorWin != 0) {
            countDownTimer = pkViewBind.pkControlView.createCountDownTimer(
                    Utils.getApp().getString(R.string.biz_live_punishment), punishInfo.getPkPenaltyCountDown() * 1000L);
            if (countDownTimer != null) {
                countDownTimer.start();
            }
        }
    }

    private void onPkEnd(PkEndInfo endInfo) {
        pkViewModel.setPkState(PK_STATE_IDLE);
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        ImageLoader.with(this).circleLoad(R.drawable.icon_pk, pkViewBind.ivRequestPk);
        if (pkViewBind.pkControlView.getVideoContainer() != null) {
            pkViewBind.pkControlView.getVideoContainer().removeAllViews();
        }
        pkViewBind.pkControlView.setVisibility(View.GONE);
        baseViewBinding.videoView.setVisibility(View.VISIBLE);
//        liveAnchor.bindLocalCanvas(baseViewBinding.videoView);
        if (endInfo != null && otherAnchor != null && endInfo.getReason() == 1 && !endInfo.getCountDownEnd() &&
            !selfStopPk) {
            ToastUtils.showShort("“" + otherAnchor.getUserUuid() + getString(R.string.biz_live_end_of_pk));
        }
        if (stopPkDialog != null && stopPkDialog.isShowing()) {
            stopPkDialog.dismiss();
        }
        otherAnchor = null;
    }

    private void onTimeout() {
        if (!isInvite) {
            if (pkInviteedDialog != null && pkInviteedDialog.isShowing()) {
                pkInviteedDialog.dismiss();
            }
        } else {
            pkViewBind.viewAction.hide();
        }
        ToastUtils.showShort(R.string.biz_live_pk_request_time_out);
        pkViewBind.llyPkProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onUserReward(RewardMsg reward) {
        if (pkViewModel.getPkState() == PK_STATE_PKING) {
            AnchorRewardInfo selfRewardInfo;
            AnchorRewardInfo otherAnchor;
            if (liveInfo != null && liveInfo.anchor != null && TextUtils.equals(liveInfo.anchor.getUserUuid(),
                                                                                reward.getAnchorReward()
                                                                                      .getUserUuid())) {
                selfRewardInfo = reward.getAnchorReward();
                otherAnchor = reward.getOtherAnchorReward();
            } else {
                selfRewardInfo = reward.getOtherAnchorReward();
                otherAnchor = reward.getAnchorReward();
            }
            if (selfRewardInfo != null && otherAnchor != null) {
                pkViewBind.pkControlView.updateScore(selfRewardInfo.getPkRewardTotal(), otherAnchor.getPkRewardTotal());
                if (selfRewardInfo.getPkRewardTop() != null && otherAnchor.getPkRewardTop() != null) {
                    pkViewBind.pkControlView.updateRanking(selfRewardInfo.getPkRewardTop(),
                                                           otherAnchor.getPkRewardTop());
                }
            }
        }
        super.onUserReward(reward);
    }

    private void requestPk(String accId, String nickname) {
        liveAnchor.invitePK(accId, new NELiveCallback<AnchorPkInfo>() {

            @Override
            public void onSuccess(AnchorPkInfo info) {
                isInvite = true;
                AnchorActionView actionView = pkViewBind.viewAction.setText(
                        getString(R.string.biz_live_invite) + accId + getString(R.string.biz_live_pk_linking));
                if (actionView != null) {
                    actionView.setColorButton(getString(R.string.biz_live_cancel), new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            cancelRequest();
                        }
                    });
                    actionView.show();
                }
                pkViewModel.setPkState(PK_STATE_REQUEST);
                if (info != null && info.getPkConfig() != null) {
                    //保存进PK倒计时相关信息
                    pkViewModel.setCurrentPkConfig(
                            new PkLiveViewModel.PkConfigInfo(info.getPkId(), info.getPkConfig().getAgreeTaskTime(),
                                                             info.getPkConfig().getInviteTaskTime()));
                    //开启邀请倒计时
                    //todo 倒计时
//                    pkViewModel.startInviteCountTimer(info.getPkConfig().getInviteTaskTime(), info.getPkId());
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ToastUtils.showShort(getString(R.string.biz_live_invite_failed) + ":" + msg);
            }
        });
    }

    private void cancelRequest() {
        liveAnchor.cancelPKInvite(new NELiveCallback<Unit>() {

            @Override
            public void onSuccess(Unit unit) {
                pkViewBind.viewAction.hide();
                pkViewModel.setPkState(PK_STATE_IDLE);
            }
            @Override
            public void onFailure(int code, String msg) {
                ToastUtils.showShort(msg);
            }
        });
    }

    private void rejectPkRequest() {
        liveAnchor.rejectPK(new NELiveCallback<Unit>() {

            @Override
            public void onSuccess(Unit unit) {
                pkViewBind.viewAction.hide();
                pkViewModel.setPkState(PK_STATE_IDLE);
            }
            @Override
            public void onFailure(int code, String msg) {
                ToastUtils.showShort(msg);
            }
        });
    }

    private void acceptPkRequest(String token, long uid) {
        liveAnchor.acceptPK(new NELiveCallback<AnchorPkInfo>() {

            @Override
            public void onSuccess(AnchorPkInfo anchorPkInfo) {
                if (pkViewModel.getPkState() == PK_STATE_REQUEST) {
                    pkViewBind.viewAction.hide();
                    pkViewBind.llyPkProgress.setVisibility(View.VISIBLE);
                    pkViewModel.setPkState(PK_STATE_AGREED);
                    //todo 同意倒计时
//                    if (pkViewModel.getCurrentPkConfig() != null) {
//                        pkViewModel.startAgreeCountTimer(pkViewModel.getCurrentPkConfig().getAgreeTaskTime(),
//                                                         pkViewModel.getCurrentPkConfig().getPkId());
//                    }
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ToastUtils.showShort(msg);
            }
        });
    }

    private void stopPk() {
        selfStopPk = true;
        liveAnchor.stopPK(new NELiveCallback<Unit>() {

            @Override
            public void onSuccess(Unit unit) {
            }
            @Override
            public void onFailure(int code, String msg) {
                selfStopPk = false;
                ToastUtils.showShort(msg);
                onPkEnd(null);
            }
        });
    }

    @Override
    protected void onNetworkConnected(NetworkUtils.NetworkType networkType) {
        super.onNetworkConnected(networkType);
        //断网重连同步状态，恢复单主播或者从pk 到惩罚
        if (pkViewModel.getPkState() == PK_STATE_PKING || pkViewModel.getPkState() == PK_STATE_PUNISH) {
            liveAnchor.requestPKInfo(liveInfo.live.getLiveRecordId(), new NELiveCallback<PkInfo>() {

                @Override
                public void onSuccess(PkInfo info) {
                    if (info != null) {
                        if (pkViewModel.getPkState() == PK_STATE_PKING &&
                            info.getState() == NELiveConstants.PkStatus.PK_STATUS_PUNISHMENT) {
                            PkPunishInfo punishInfo = new PkPunishInfo(0, info.getPkStartTime(), info.getCountDown(),
                                                                       info.getInviterReward().getRewardCoinTotal(),
                                                                       info.getInviteeReward().getRewardCoinTotal());
                            pkViewModel.setPkState(PK_STATE_PUNISH);
                            onPunishStart(punishInfo);
                        } else if (info.getState() != NELiveConstants.PkStatus.PK_STATUS_PUNISHMENT &&
                                   info.getState() != NELiveConstants.PkStatus.PK_STATUS_PKING) {
                            //not in pk or punish
                            onPkEnd(null);
                        }
                    } else {
                        onPkEnd(null);
                    }
                }
                @Override
                public void onFailure(int code, String msg) {
                    if (code != NELiveConstants.ErrorCode.CODE_NO_PK) {
                        ToastUtils.showLong(msg);
                    }
                    onPkEnd(null);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        pkViewModel.destroy();
        super.onDestroy();
    }
}
