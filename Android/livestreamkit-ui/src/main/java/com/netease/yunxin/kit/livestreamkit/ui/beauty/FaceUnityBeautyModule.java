// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.beauty;

import android.hardware.Camera;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.netease.yunxin.kit.roomkit.api.NERoomVideoFrame;

public class FaceUnityBeautyModule implements IBeautyModule {
  private static final String TAG = "FaceUnityBeautyModule";
  private FURenderer mFURendererManager;
  private FaceUnityDataFactory mFaceUnityDataFactory;
  private boolean isInitialized = false;
  private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
  private boolean isFirstInit = true;

  @Override
  public void init() {
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
    isInitialized = true;
  }

  @Override
  public void release() {
    if (mFURendererManager != null) {
      mFURendererManager.release();
      isInitialized = false;
      isFirstInit = true;
    }
  }

  @Override
  public void onFrameAvailable(NERoomVideoFrame frame) {
    if (!isInitialized) return;

    if (isFirstInit) {
      isFirstInit = false;
      mFURendererManager.prepareRenderer(
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
          });
    } else {
      int texId =
          mFURendererManager.onDrawFrameSingleInput(
              frame.getTextureId(), frame.getWidth(), frame.getHeight());
      if (frame.getFormat() == NERoomVideoFrame.Format.TEXTURE_OES) {
        frame.setFormat(NERoomVideoFrame.Format.TEXTURE_RGB);
      }
      frame.setTextureId(texId);
    }
  }

  @Override
  public void onCameraChanged(boolean isFrontCamera) {
    mCameraFacing =
        isFrontCamera
            ? Camera.CameraInfo.CAMERA_FACING_FRONT
            : Camera.CameraInfo.CAMERA_FACING_BACK;
    mFURendererManager.setCameraFacing(
        isFrontCamera ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK);
    mFURendererManager.setInputOrientation(
        CameraUtils.INSTANCE.getCameraOrientation(mCameraFacing));
  }

  @Override
  public void onSensorChanged(float x, float y) {
    if (Math.abs(x) > 3 || Math.abs(y) > 3) {
      if (Math.abs(x) > Math.abs(y)) {
        mFURendererManager.setDeviceOrientation(x > 0 ? 0 : 180);
      } else {
        mFURendererManager.setDeviceOrientation(y > 0 ? 90 : 270);
      }
    }
  }

  @Override
  public boolean isInitialized() {
    return isInitialized;
  }

  public FaceUnityDataFactory getFaceUnityDataFactory() {
    return mFaceUnityDataFactory;
  }
}
