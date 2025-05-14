// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.LiveTogetherSeatsBinding;

public class LiveStreamSeatsLayout extends ConstraintLayout {
  private LiveTogetherSeatsBinding binding;

  public LiveStreamSeatsLayout(@NonNull Context context) {
    super(context);
    init(context, null);
  }

  public LiveStreamSeatsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public LiveStreamSeatsLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    binding = LiveTogetherSeatsBinding.inflate(LayoutInflater.from(context), this, true);
  }

  public void setIsListeningTogether(boolean isListeningTogether) {
    if (isListeningTogether) {
      binding.ivWaveLine.setVisibility(VISIBLE);
    } else {
      binding.ivWaveLine.setVisibility(INVISIBLE);
    }
  }

  public void setAnchorSeatInfo(SeatView.SeatInfo seatInfo) {
    binding.seatAnchor.setSeatInfo(seatInfo);
  }

  public void setAudienceSeatInfo(SeatView.SeatInfo seatInfo) {
    if (!seatInfo.isOnSeat) {
      showAudienceAvatarAnimal(false);
    }
    binding.seatAudience.setSeatInfo(seatInfo);
  }

  public void showAnim(boolean show) {
    if (show) {
      if (binding.lottieView.isAnimating()) {
        return;
      }
      binding.lottieView.setRepeatCount(LottieDrawable.INFINITE);
      binding.lottieView.playAnimation();
    } else {
      binding.lottieView.cancelAnimation();
      binding.lottieView.setProgress(0);
    }
  }

  public void showAnchorAvatarAnimal(boolean showAvatarAnimal) {
    LottieAnimationView lavAnchorAvatar = binding.lavAnchorAvatarLottieView;
    if (showAvatarAnimal) {
      lavAnchorAvatar.setVisibility(View.VISIBLE);
      if (lavAnchorAvatar.isAnimating()) {
        return;
      }
      lavAnchorAvatar.setRepeatCount(LottieDrawable.INFINITE);
      lavAnchorAvatar.playAnimation();
    } else {
      lavAnchorAvatar.setVisibility(View.INVISIBLE);
      lavAnchorAvatar.cancelAnimation();
      lavAnchorAvatar.setProgress(0);
    }
  }

  public void showAudienceAvatarAnimal(boolean showAvatarAnimal) {
    LottieAnimationView lavAnchorAvatar = binding.lavAudienceAvatarLottieView;
    if (showAvatarAnimal) {
      lavAnchorAvatar.setVisibility(View.VISIBLE);
      if (lavAnchorAvatar.isAnimating()) {
        return;
      }
      lavAnchorAvatar.setRepeatCount(LottieDrawable.INFINITE);
      lavAnchorAvatar.playAnimation();
    } else {
      lavAnchorAvatar.setVisibility(View.INVISIBLE);
      lavAnchorAvatar.cancelAnimation();
      lavAnchorAvatar.setProgress(0);
    }
  }
}
