// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.viewmodel;

import android.content.Context;
import android.hardware.*;
import android.text.TextUtils;
import androidx.lifecycle.MutableLiveData;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.kit.entertainment.common.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveStreamRoomInfo;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.beauty.FaceUnityBeautyModule;
import com.netease.yunxin.kit.livestreamkit.ui.beauty.IBeautyModule;
import com.netease.yunxin.kit.livestreamkit.ui.config.LiveConfig;
import com.netease.yunxin.kit.livestreamkit.ui.repo.LiveRepo;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.model.NERoomVideoConfig;
import com.netease.yunxin.kit.roomkit.api.service.NEPreviewRoomOptions;
import com.netease.yunxin.kit.roomkit.api.service.NEPreviewRoomParams;
import com.netease.yunxin.kit.roomkit.api.service.NERoomService;
import com.netease.yunxin.kit.roomkit.api.view.NERoomVideoView;
import kotlin.Unit;
import org.jetbrains.annotations.*;

public class HostLiveViewModel extends BaseLiveViewModel implements SensorEventListener {
  private static final String TAG = "HostLiveViewModel";
  private static final int ANCHOR_SEAT_INDEX = 1;
  public static final int LIVE_STATE_FINISH = 0;
  public static final int LIVE_STATE_LIVING = 1;
  public static final int LIVE_STATE_PREVIEW = 2;
  public static final int LIVE_STATE_ERROR = 3;
  private final MutableLiveData<Integer> liveStateData = new MutableLiveData<>(LIVE_STATE_PREVIEW);
  private final MutableLiveData<String> memberCount = new MutableLiveData<>("0");
  private final MutableLiveData<NELiveStreamRoomInfo> existingRoomInfo = new MutableLiveData<>();
  private final MutableLiveData<NELiveStreamRoomInfo> roomInfoData = new MutableLiveData<>();
  private NEPreviewRoomContext previewRoomContext;
  protected NELiveStreamRoomInfo voiceRoomInfo;
  private final LiveRepo liveRepo = new LiveRepo();
  private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
  private boolean isFirstInit = true;
  private FURenderer mFURendererManager;
  private FaceUnityDataFactory mFaceUnityDataFactory;
  private SensorManager mSensorManager;

  private final IBeautyModule beautyModule;

  public HostLiveViewModel() {
    beautyModule = new FaceUnityBeautyModule();
    beautyModule.init();
    setupFaceUnity();
    checkExistingLiveRoom();
    checkBackground();
  }

  private void checkBackground() {
    AppStatusManager.getInstance()
        .addCallback(
            new AppStatusManager.AppForegroundStateCallback() {
              @Override
              public void onAppForeground() {
                NELiveStreamKit.getInstance().resumeLive(null);
              }

              @Override
              public void onAppBackground() {
                NELiveStreamKit.getInstance().pauseLive(null);
              }
            });
  }

  private final FURendererListener mFURendererListener =
      new FURendererListener() {
        @Override
        public void onPrepare() {
          mFaceUnityDataFactory.bindCurrentRenderer();
        }

        @Override
        public void onTrackStatusChanged(FUAIProcessorEnum type, int status) {}

        @Override
        public void onFpsChanged(double fps, double callTime) {}

        @Override
        public void onRelease() {}
      };

  public void setRoomInfo(NELiveStreamRoomInfo roomInfo) {
    voiceRoomInfo = roomInfo;
    roomInfoData.setValue(roomInfo);
  }

  private final NERoomService roomService = NERoomKit.getInstance().getService(NERoomService.class);

  public void startPreview(NERoomVideoView videoView) {
    liveStateData.postValue(LIVE_STATE_PREVIEW);
    roomService.previewRoom(
        new NEPreviewRoomParams(),
        new NEPreviewRoomOptions(),
        new NECallback<NEPreviewRoomContext>() {
          @Override
          public void onResult(int code, String message, NEPreviewRoomContext data) {
            previewRoomContext = data;
            if (previewRoomContext != null) {
              setupLocalVideoConfig();
              previewRoomContext.getPreviewController().startPreview(videoView);
              previewRoomContext
                  .getPreviewController()
                  .setVideoFrameCallback(
                      true,
                      frame -> {
                        beautyModule.onFrameAvailable(frame);
                        if (LiveStreamUtils.skipFrame-- > 0) {
                          return null;
                        } else {
                          return frame;
                        }
                      });
            }
          }
        });
  }

  private void stopPreview() {
    if (previewRoomContext != null) {
      previewRoomContext.getPreviewController().stopPreview(true);
    }
    beautyModule.release();
  }

  private void setupLocalVideoConfig() {
    NERoomVideoConfig videoConfig = new NERoomVideoConfig();
    videoConfig.setWidth(LiveConfig.Video.DEFAULT_WIDTH);
    videoConfig.setHeight(LiveConfig.Video.DEFAULT_HEIGHT);
    videoConfig.setFps(LiveConfig.Video.DEFAULT_FPS);
    previewRoomContext.getPreviewController().setLocalVideoConfig(videoConfig);
  }

  public void checkExistingLiveRoom() {
    NELiveStreamKit.getInstance()
        .getLivingRoomInfo(
            new NELiveStreamCallback<NELiveStreamRoomInfo>() {
              @Override
              public void onSuccess(NELiveStreamRoomInfo roomInfo) {
                LiveRoomLog.d(TAG, "getLivingRoomInfo onSuccess, roomInfo = " + roomInfo);
                if (roomInfo != null) {
                  existingRoomInfo.postValue(roomInfo);
                }
              }

              @Override
              public void onFailure(int code, @androidx.annotation.Nullable String msg) {
                LiveRoomLog.d(TAG, "getLivingRoomInfo onFailure, no existing room found");
                existingRoomInfo.postValue(null);
              }
            });
  }

  public void createLive(String title, String anchorNick, String cover, int configId) {
    LiveRoomLog.d(
        TAG,
        "createLive title = "
            + title
            + " anchorNick = "
            + anchorNick
            + " cover = "
            + cover
            + " configId = "
            + configId);
    liveRepo.createLive(
        title,
        anchorNick,
        cover,
        configId,
        new NELiveStreamCallback<NELiveStreamRoomInfo>() {
          @Override
          public void onSuccess(NELiveStreamRoomInfo roomInfo) {
            LiveRoomLog.d(TAG, "createLive onSuccess");
            setRoomInfo(roomInfo);
            liveStateData.postValue(LIVE_STATE_LIVING);
            NELiveStreamKit.getInstance().submitSeatRequest(ANCHOR_SEAT_INDEX, true, null);
          }

          @Override
          public void onFailure(int code, String msg) {
            LiveRoomLog.d(TAG, "createLive onFailure code = " + code + " msg = " + msg);
            liveStateData.postValue(LIVE_STATE_ERROR);
            if (!TextUtils.isEmpty(msg)) {
              ToastX.showShortToast(msg);
            } else {
              ToastX.showShortToast(R.string.live_start_live_error);
            }
          }
        });
  }

  public void joinLive(String username, String avatar, NELiveStreamRoomInfo roomInfo) {
    joinLive(
        username,
        avatar,
        NELiveRoomRole.HOST.getValue(),
        roomInfo,
        new NELiveStreamCallback<NELiveStreamRoomInfo>() {

          @Override
          public void onSuccess(@Nullable NELiveStreamRoomInfo neLiveStreamRoomInfo) {
            LiveRoomLog.d(TAG, "joinLive onSuccess");
            liveStateData.setValue(HostLiveViewModel.LIVE_STATE_LIVING);
            roomInfoData.setValue(neLiveStreamRoomInfo);
            NELiveStreamKit.getInstance().submitSeatRequest(ANCHOR_SEAT_INDEX, true, null);
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {
            if (!TextUtils.isEmpty(msg)) {
              ToastX.showShortToast(msg);
            } else {
              ToastX.showShortToast(R.string.live_join_live_error);
            }
          }
        });
  }

  public void endLive() {
    NELiveStreamKit.getInstance()
        .endRoom(
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(Unit unit) {
                LiveRoomLog.d(TAG, "endRoom onSuccess");
                ToastX.showShortToast(R.string.voiceroom_host_close_room_success);
                liveStateData.postValue(LIVE_STATE_FINISH);
              }

              @Override
              public void onFailure(int code, @androidx.annotation.Nullable String msg) {
                LiveRoomLog.e(TAG, "endRoom failed code:" + code + ",msg:" + msg);
                liveStateData.postValue(LIVE_STATE_FINISH);
              }
            });
  }

  public void getLivingRoomInfo() {
    NELiveStreamKit.getInstance()
        .getLivingRoomInfo(
            new NELiveStreamCallback<NELiveStreamRoomInfo>() {
              @Override
              public void onSuccess(NELiveStreamRoomInfo roomInfo) {
                LiveRoomLog.d(TAG, "getLivingRoomInfo onSuccess");
                ToastX.showShortToast(R.string.voiceroom_host_close_room_success);
                liveStateData.postValue(LIVE_STATE_FINISH);
              }

              @Override
              public void onFailure(int code, @androidx.annotation.Nullable String msg) {
                LiveRoomLog.e(TAG, "getLivingRoomInfo failed code:" + code + ",msg:" + msg);
                liveStateData.postValue(LIVE_STATE_FINISH);
              }
            });
  }

  public void switchCamera() {
    if (previewRoomContext != null) {
      LiveStreamUtils.skipFrame = 5;
      previewRoomContext.getPreviewController().switchCamera();
      beautyModule.onCameraChanged(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
  }

  private void setupFaceUnity() {
    mFURendererManager = FURenderer.getInstance();
    mFURendererManager.setMarkFPSEnable(true);
    mFURendererManager.setInputTextureType(FUInputTextureEnum.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE);
    mFURendererManager.setCameraFacing(CameraFacingEnum.CAMERA_FRONT);
    mFURendererManager.setInputOrientation(
        CameraUtils.INSTANCE.getCameraOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT));
    mFURendererManager.setInputTextureMatrix(
        mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
            ? FUTransformMatrixEnum.CCROT0_FLIPVERTICAL
            : FUTransformMatrixEnum.CCROT0);
    mFURendererManager.setInputBufferMatrix(
        mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
            ? FUTransformMatrixEnum.CCROT0_FLIPVERTICAL
            : FUTransformMatrixEnum.CCROT0);
    mFURendererManager.setOutputMatrix(
        mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
            ? FUTransformMatrixEnum.CCROT0
            : FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
    mFURendererManager.setCreateEGLContext(false);
    mFaceUnityDataFactory = new FaceUnityDataFactory(0);
    mSensorManager =
        (SensorManager) XKitUtils.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
    Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  public MutableLiveData<String> getMemberCount() {
    return memberCount;
  }

  public MutableLiveData<NELiveStreamRoomInfo> getRoomInfoData() {
    return roomInfoData;
  }

  public MutableLiveData<Integer> getLiveStateData() {
    return liveStateData;
  }

  public FaceUnityDataFactory getFaceUnityDataFactory() {
    return ((FaceUnityBeautyModule) beautyModule).getFaceUnityDataFactory();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    beautyModule.onSensorChanged(event.values[0], event.values[1]);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  private void destroyFU() {
    mFURendererManager.release();
    isFirstInit = true;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    stopPreview();
    destroyFU();
    beautyModule.release();
    mSensorManager.unregisterListener(this);
  }

  public MutableLiveData<NELiveStreamRoomInfo> getExistingRoomInfo() {
    return existingRoomInfo;
  }

  @Override
  protected void onLiveRoomEnded(NERoomEndReason reason) {
    liveStateData.postValue(LIVE_STATE_FINISH);
  }
}
