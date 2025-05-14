// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.helper.AudioPlayHelper;
import com.netease.yunxin.kit.livestreamkit.ui.view.VolumeSetup;

/** Created by luc on 1/28/21. */
public class ChatRoomMixerDialog extends BottomBaseDialog {

  private final AudioPlayHelper audioPlayHelper;
  private int earBack = 100;
  private boolean isAnchor;

  public ChatRoomMixerDialog(
      @NonNull Activity activity, AudioPlayHelper audioPlayHelper, boolean isAnchor) {
    super(activity);
    this.audioPlayHelper = audioPlayHelper;
    this.isAnchor = isAnchor;
  }

  @Override
  protected void renderTopView(FrameLayout parent) {
    TextView titleView = new TextView(getContext());
    titleView.setText(getContext().getString(R.string.live_mixer));
    titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
    titleView.setGravity(Gravity.CENTER);
    titleView.setTextColor(Color.parseColor("#ff333333"));
    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    parent.addView(titleView, layoutParams);
  }

  @Override
  protected void renderBottomView(FrameLayout parent) {
    View bottomView =
        LayoutInflater.from(getContext()).inflate(R.layout.live_view_dialog_more_mixer, parent);
    SeekBar skRecordingVolume = bottomView.findViewById(R.id.recording_volume_control);
    skRecordingVolume.setProgress(audioPlayHelper.getAudioCaptureVolume());
    skRecordingVolume.setOnSeekBarChangeListener(
        new VolumeSetup() {
          @Override
          protected void onVolume(int volume) {
            audioPlayHelper.setAudioCaptureVolume(volume);
          }
        });
  }
}
