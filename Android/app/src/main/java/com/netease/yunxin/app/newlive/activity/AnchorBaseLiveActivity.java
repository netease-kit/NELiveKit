package com.netease.yunxin.app.newlive.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.vcloud.video.render.NeteaseView;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.newlive.BuildConfig;
import com.netease.yunxin.app.newlive.R;
import com.netease.yunxin.app.newlive.beauty.BeautyControl;
import com.netease.yunxin.app.newlive.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.app.newlive.dialog.AnchorMoreDialog;
import com.netease.yunxin.app.newlive.dialog.ChoiceDialog;
import com.netease.yunxin.app.newlive.dialog.DumpDialog;
import com.netease.yunxin.app.newlive.gift.GiftCache;
import com.netease.yunxin.app.newlive.utils.InputUtils;
import com.netease.yunxin.app.newlive.utils.StringUtils;
import com.netease.yunxin.app.newlive.utils.ViewUtils;
import com.netease.yunxin.app.newlive.viewmodel.AudioControl;
import com.netease.yunxin.app.newlive.viewmodel.LiveBaseViewModel;
import com.netease.yunxin.app.newlive.databinding.LiveAnchorBaseLayoutBinding;
import com.netease.yunxin.app.newlive.databinding.ViewIncludeRoomTopBinding;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.livekit.NELiveKit;
import com.netease.yunxin.kit.livekit.NELiveCallback;
import com.netease.yunxin.kit.livekit.model.ErrorInfo;
import com.netease.yunxin.kit.livekit.model.LiveInfo;
import com.netease.yunxin.kit.livekit.model.RewardMsg;
import com.netease.yunxin.app.newlive.config.StatusBarConfig;
import com.netease.yunxin.kit.livekit.network.ServiceCreator;
import com.netease.yunxin.kit.livekit.utils.LiveLog;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.roomkit.api.NECallback2;
import com.netease.yunxin.kit.roomkit.api.NERoomMember;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import kotlin.Unit;

public abstract class AnchorBaseLiveActivity extends BaseActivity {

    private final static String TAG = "AnchorBaseLiveActivity";

    /**
     * 美颜控制
     */
    private BeautyControl beautyControl = null;

    /**
     * 单主播直播信息
     */
    protected LiveInfo liveInfo = null;

    protected AudioControl audioControl = null;

    boolean isMirror = true;

    boolean voiceBeautifierEnable = false;

    boolean audioEffectEnable = false;

    /**
     * 直播开始
     */
    private boolean isLiveStart = false;

    //摄像头FACE_BACK = 0, FACE_FRONT = 1
    protected int cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

    protected LiveAnchorBaseLayoutBinding baseViewBinding;

    protected ViewIncludeRoomTopBinding topViewBinding;

    protected LiveBaseViewModel liveBaseViewModel;

    protected NELiveKit liveAnchor = NELiveKit.getInstance();

    /**
     * 结束直播
     */
    private void stopLiveErrorNetwork() {
        if (isLiveStart) {
            ToastUtils.showLong(R.string.biz_live_network_is_not_stable_live_is_end);
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 应用运行时，保持不锁屏、全屏化
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        baseViewBinding = LiveAnchorBaseLayoutBinding.inflate(getLayoutInflater());
        topViewBinding = ViewIncludeRoomTopBinding.bind(baseViewBinding.clyAnchorInfo);
        liveBaseViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(
                LiveBaseViewModel.class);
        setContentView(baseViewBinding.getRoot());
        // 全屏展示控制
        paddingStatusBarHeight(findViewById(R.id.preview_anchor));
        paddingStatusBarHeight(findViewById(R.id.cly_anchor_info));
        paddingStatusBarHeight(findViewById(R.id.fly_container));
        requestPermissionsIfNeeded();
        //初始化伴音
        audioControl = new AudioControl(this);
        audioControl.initMusicAndEffect();
    }

    /**
     * 权限检查
     */
    private void requestPermissionsIfNeeded() {
        List<String> missedPermissions = new ArrayList<>();
        missedPermissions.add(Manifest.permission.RECORD_AUDIO);
        missedPermissions.add(Manifest.permission.CAMERA);
        missedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        missedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        if (missedPermissions.size() > 0) {
            PermissionUtils.permission(missedPermissions.toArray(new String[missedPermissions.size()])).callback(
                    new PermissionUtils.FullCallback() {

                        @Override
                        public void onGranted(@NonNull List<String> granted) {
                            if (CollectionUtils.isEqualCollection(granted, missedPermissions)) {
                                initView();
                            }
                        }
                        @Override
                        public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                            ToastUtils.showShort(R.string.biz_live_authorization_failed);
                            finish();
                        }
                    }).request();
        } else {
            initView();
        }
    }

    protected void initView() {
        baseViewBinding.clyAnchorInfo.post(new Runnable() {

            @Override
            public void run() {
                {
                    InputUtils.INSTANCE.registerSoftInputListener(AnchorBaseLiveActivity.this,
                                                                  new InputUtils.InputParamHelper() {

                                                                      @NonNull
                                                                      @Override
                                                                      public EditText getInputView() {
                                                                          return baseViewBinding.etRoomMsgInput;
                                                                      }
                                                                      @Override
                                                                      public int getHeight() {
                                                                          return baseViewBinding.clyAnchorInfo
                                                                                  .getHeight();
                                                                      }
                                                                  });
                }
                initContainer();
                initData();
                setListener();
                initDataObserve();
                liveBaseViewModel.init();
            }
        });
    }

    protected abstract void initContainer();

    private NetworkUtils.OnNetworkStatusChangedListener netWorkStatusChangeListener = new NetworkUtils.OnNetworkStatusChangedListener() {

        @Override
        public void onDisconnected() {
            LiveLog.i(TAG, "network disconnected");
            onNetworkDisconnected();
        }

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
            onNetworkConnected(networkType);
            LiveLog.i(TAG, "network onConnected");

        }
    };

    @SuppressLint("MissingPermission")
    protected void initData() {
        beautyControl = new BeautyControl(this);
        beautyControl.initFaceUI();
        liveBaseViewModel.setBeautyControl(beautyControl);
        startPreview(baseViewBinding.videoView);
        //添加网络监听回调
        NetworkUtils.registerNetworkStatusChangedListener(netWorkStatusChangeListener);
    }

    protected void setListener() {
        baseViewBinding.ivMusic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showAudioControlDialog();
            }
        });
        baseViewBinding.ivBeauty.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showBeautyDialog();
            }
        });
        baseViewBinding.tvRoomMsgInput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                InputUtils.INSTANCE.showSoftInput(baseViewBinding.etRoomMsgInput);
            }
        });
        baseViewBinding.previewAnchor.getBtnLiveCreate().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                startLive();
            }
        });
        baseViewBinding.previewAnchor.getLlyBeauty().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showBeautyDialog();
            }
        });
        baseViewBinding.previewAnchor.getLlyFilter().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });
        baseViewBinding.previewAnchor.getIvClose().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        baseViewBinding.previewAnchor.getIvSwitchCamera().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });
        baseViewBinding.ivMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showLiveMoreDialog();
            }
        });
        if (BuildConfig.DEBUG) {
            baseViewBinding.ivMore.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    DumpDialog.Companion.showDialog(getSupportFragmentManager());
                    return true;
                }
            });
        }
        baseViewBinding.etRoomMsgInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (textView == baseViewBinding.etRoomMsgInput) {
                    String input = baseViewBinding.etRoomMsgInput.getText().toString();
                    InputUtils.INSTANCE.hideSoftInput(baseViewBinding.etRoomMsgInput);
                    sendTextMsg(input);
                    return true;
                }
                return false;
            }
        });
    }

    private void setMirror() {
        isMirror = !isMirror;
        baseViewBinding.videoView.setMirror(isMirror);
    }

    protected void onNetworkDisconnected() {
    }

    protected void onNetworkConnected(NetworkUtils.NetworkType networkType) {
    }

    protected void initDataObserve() {
        liveBaseViewModel.getRewardData().observe(this, new Observer<RewardMsg>() {

            @Override
            public void onChanged(RewardMsg rewardMsg) {
                onUserReward(rewardMsg);
            }
        });
        liveBaseViewModel.getErrorData().observe(this, new Observer<ErrorInfo>() {

            @Override
            public void onChanged(ErrorInfo errorInfo) {
                if (errorInfo.getSerious()) {
                    finish();
                }
                ToastUtils.showLong(errorInfo.getMsg());
            }
        });
        liveBaseViewModel.getChatRoomMsgData().observe(this, new Observer<CharSequence>() {

            @Override
            public void onChanged(CharSequence charSequence) {
                baseViewBinding.crvMsgList.appendItem(charSequence);
            }
        });
        liveBaseViewModel.getKickedOutData().observe(this, new Observer<Boolean>() {

            @Override
            public void onChanged(Boolean aBoolean) {
//                stopLiveErrorNetwork();
            }
        });
        liveBaseViewModel.getUserAccountData().observe(this, new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                topViewBinding.tvAudienceCount.setText(StringUtils.INSTANCE.getAudienceCount(integer));
            }
        });
        liveBaseViewModel.getAudioEffectFinishData().observe(this, new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                onAudioEffectFinished(integer);
            }
        });
        liveBaseViewModel.getAudioMixingFinishData().observe(this, new Observer<Boolean>() {

            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    onAudioMixingFinished();
                }
            }
        });
        liveBaseViewModel.getAudienceData().observe(this, new Observer<List<NERoomMember>>() {

            @Override
            public void onChanged(List<NERoomMember> liveUsers) {
                topViewBinding.rvAnchorPortraitList.updateAll(liveUsers);
            }
        });
    }

    /**
     * on user reward to anchor
     */
    protected void onUserReward(RewardMsg reward) {
        if (liveInfo == null) {
            return;
        }
        if (liveInfo.anchor == null) {
            return;
        }
        //todo reward
        if (TextUtils.equals(reward.getAnchorReward().getUserUuid(), liveInfo.anchor.getUserUuid())) {
            topViewBinding.tvAnchorCoinCount.setText(StringUtils.INSTANCE.getCoinCount(reward.getAnchorReward().getRewardTotal()));

            baseViewBinding.crvMsgList.appendItem(
                    ChatRoomMsgCreator.INSTANCE.createGiftReward(
                            reward.getRewarderNickname(),
                            1, GiftCache.INSTANCE.getGift(reward.getGiftId()).getStaticIconResId()
                    )
            );
        }
    }

    /**
     * switch the camera
     */
    protected void switchCamera() {
        liveAnchor.getMediaController().switchCamera();
        cameraFacing = getCameraFacing();
        if (beautyControl != null) {
            beautyControl.switchCamera(cameraFacing);
        }
    }

    private int getCameraFacing() {
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        // 键盘区域外点击收起键盘
        if (!ViewUtils.INSTANCE.isInView(baseViewBinding.etRoomMsgInput, x, y) && isLiveStart) {
            InputUtils.INSTANCE.hideSoftInput(baseViewBinding.etRoomMsgInput);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 预览
     */
    private void startPreview(NeteaseView view) {
        liveAnchor.getMediaController().startPreview(view, true);
    }

    /**
     * create a live room
     */
    protected abstract void startLive();

    /**
     * 停止直播
     */
    protected void stopLive() {
        isLiveStart = false;
        liveAnchor.getMediaController().stopPreview();
        liveAnchor.stopLive(new NELiveCallback<Unit>() {

            @Override
            public void onSuccess(Unit unit) {
                finish();
            }
            @Override
            public void onFailure(int code, String msg) {
                LiveLog.e(TAG, "destroyRoom error msg = " + msg + " code = " + code);
                finish();
            }
        });
    }


    protected void onRoomLiveStart() {
        baseViewBinding.previewAnchor.setVisibility(View.GONE);
        baseViewBinding.clyAnchorInfo.setVisibility(View.VISIBLE);
        if (liveInfo != null && liveInfo.anchor != null) {
            topViewBinding.tvAnchorNickname.setText(liveInfo.anchor.getUserUuid());
            ImageLoader.with(getApplicationContext()).circleLoad(liveInfo.anchor.getAvatar(),
                                                                 topViewBinding.ivAnchorPortrait);
        }
        topViewBinding.tvAnchorCoinCount.setText(R.string.biz_live_zero_coin);
        isLiveStart = true;
        baseViewBinding.flyContainer.setVisibility(View.VISIBLE);
        topViewBinding.tvAudienceCount.setText(StringUtils.INSTANCE.getAudienceCount(0));
    }

    private void onAudioEffectFinished(int effectId) {
        if (audioControl != null) {
            audioControl.onEffectFinish(effectId);
        }
    }

    private void onAudioMixingFinished() {
        if (audioControl != null) {
            audioControl.onMixingFinished();
        }
    }

    void onError(boolean serious, int code, String msg) {
        if (serious) {
            ToastUtils.showShort(msg);
            finish();
        }
        LiveLog.d(TAG, "$msg code = $code");
    }

    /**
     * 显示混音dailog
     */
    private void showAudioControlDialog() {
        if (audioControl != null) {
            audioControl.showAudioControlDialog();
        }
    }

    /**
     * 展示美颜dialog
     */
    private void showBeautyDialog() {
        if (beautyControl != null) {
            beautyControl.showBeautyDialog();
        }
    }

    protected void showFilterDialog() {
        if (beautyControl != null) {
            beautyControl.showFilterDialog();
        }
    }


    private void sendTextMsg(String msg) {
        if (msg == null) {
            return;
        }
        if (liveInfo == null) {
            return;
        }
        if (liveInfo.anchor == null) {
            return;
        }
        if (!TextUtils.isEmpty(msg.trim())) {
            liveAnchor.sendMessage(msg, new NELiveCallback<Unit>() {

                @Override
                public void onSuccess(Unit unit) {
                }
                @Override
                public void onFailure(int code, String msg) {
                }
            });
            baseViewBinding.crvMsgList.appendItem(
                    ChatRoomMsgCreator.INSTANCE.createText(true, liveInfo.anchor.getUserUuid(), msg));
        }
    }

    protected void clearLocalImage() {
        //        baseViewBinding.videoView.clearImage();
    }


    /**
     * 直播中的更多弹框
     */
    private void showLiveMoreDialog() {
        AnchorMoreDialog anchorMoreDialog = new AnchorMoreDialog(this);
        anchorMoreDialog.registerOnItemClickListener(new AnchorMoreDialog.OnItemClickListener() {

            @Override
            public boolean onItemClick(@Nullable View itemView, @Nullable AnchorMoreDialog.MoreItem item) {
                if (item == null) {
                    return true;
                }
                switch (item.getId()) {
                    case AnchorMoreDialog.ITEM_CAMERA:
                        if (item.getEnable()) {
                            clearLocalImage();
                        }
                        liveAnchor.getMediaController().muteLocalVideo(item.getEnable(), new NECallback2<Unit>() {

                            @Override
                            public void onSuccess(@Nullable Unit unit) {
                                ALog.d(TAG, "muteLocalVideo success");
                            }
                            @Override
                            public void onError(int code, @Nullable String message) {
                                ALog.d(TAG, "muteLocalVideo failed code = " + code + " message = " + message);
                            }
                        });
                        return true;
                    case AnchorMoreDialog.ITEM_MUTE:
                        liveAnchor.getMediaController().muteLocalAudio(item.getEnable(), new NECallback2<Unit>() {

                            @Override
                            public void onSuccess(@Nullable Unit unit) {
                                ALog.d(TAG, "muteLocalAudio success");
                            }
                            @Override
                            public void onError(int code, @Nullable String message) {
                                ALog.d(TAG, "muteLocalAudio failed code = " + code + " message = " + message);
                            }
                        });
                        return true;
                    case AnchorMoreDialog.ITEM_RETURN:
//                        Pair<Boolean, Boolean> result = liveAnchor.getMediaController().enableEarBack(!item.getEnable(), 100);
//                        if (!result.getFirst() && !result.getSecond()) {
//                            ToastUtils.showShort(R.string.biz_live_insert_earphones_before_open_earback);
//                        }
//                        return result.getFirst();
                        return true;
                    case AnchorMoreDialog.ITEM_CAMERA_SWITCH:
                        switchCamera();
                        break;
                    case AnchorMoreDialog.ITEM_SETTING:
                        ToastUtils.showShort(R.string.biz_live_setting_function_to_be_improved);
                        break;
                    case AnchorMoreDialog.ITEM_DATA:
                        ToastUtils.showShort(R.string.biz_live_data_statistics_function_to_be_improved);
                        break;
                    case AnchorMoreDialog.ITEM_FINISH:
                        onBackPressed();
                        break;
                    case AnchorMoreDialog.ITEM_FILTER:
                        showFilterDialog();
                        break;
                    default:

                }
                return true;
            }
        });
        anchorMoreDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (isLiveStart) {
            ChoiceDialog closeDialog = new ChoiceDialog(this).setTitle(getString(R.string.biz_live_end_live))
                                                             .setContent(getString(R.string.biz_live_sure_end_live))
                                                             .setNegative(getString(R.string.biz_live_cancel), null)
                                                             .setPositive(getString(R.string.biz_live_determine),
                                                                          new View.OnClickListener() {

                                                                              @Override
                                                                              public void onClick(View view) {
                                                                                  stopLive();
                                                                              }
                                                                          });
            closeDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (beautyControl != null) {
            beautyControl.onDestroy();
            beautyControl = null;
        }
        liveAnchor.getMediaController().stopPreview();
        AnchorMoreDialog.Companion.clearItem();
        liveBaseViewModel.destroy();
        NetworkUtils.unregisterNetworkStatusChangedListener(netWorkStatusChangeListener);
        LiveLog.flush(true);
        super.onDestroy();
    }

    @Override
    protected StatusBarConfig provideStatusBarConfig() {
        return new StatusBarConfig.Builder().statusBarDarkFont(false).build();
    }

}
