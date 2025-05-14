// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.ui.FaceUnityView;
import com.netease.yunxin.kit.beauty.faceunity.R;
import org.jetbrains.annotations.NotNull;

public class BeautyBottomDialog extends DialogFragment {
  protected Activity activity;
  protected View rootView;
  private FaceUnityDataFactory mFaceUnityDataFactory;
  private FaceUnityView fuView;

  public BeautyBottomDialog(@NonNull Activity activity, FaceUnityDataFactory faceUnityDataFactory) {
    this.activity = activity;
    this.mFaceUnityDataFactory = faceUnityDataFactory;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_TITLE, com.netease.yunxin.kit.common.ui.R.style.BottomDialogTheme);
  }

  @Override
  public @Nullable View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return requireActivity()
        .getLayoutInflater()
        .inflate(R.layout.dialog_bottom_beauty, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupFaceUnity(view);
  }

  @Override
  public @NotNull Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    final Window window = dialog.getWindow();
    if (window != null) {
      window.getDecorView().setPadding(0, 0, 0, 0);
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.BOTTOM;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
    }
    return dialog;
  }

  private void setupFaceUnity(View view) {
    fuView = view.findViewById(R.id.fu_view);
    fuView.bindDataFactory(mFaceUnityDataFactory);
    fuView.checkSkinBeautyTab();
  }
}
