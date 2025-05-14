// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.*;
import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.entertainment.common.fragment.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;

public class AudienceLinkSeatInviteDialog extends BaseDialogFragment {
  private final String anchorName;
  private final OnActionListener listener;
  private int countdown = 12;
  private final Handler handler = new Handler(Looper.getMainLooper());

  public interface OnActionListener {
    void onConfirm();

    void onCancel();
  }

  public AudienceLinkSeatInviteDialog(String anchorName, OnActionListener listener) {
    this.anchorName = anchorName;
    this.listener = listener;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.request_dialog_fragment);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.live_dialog_audience_link_mic_invite, container, false);
    TextView tvTitle = view.findViewById(R.id.tv_invite_title);
    Button btnCancel = view.findViewById(R.id.btn_cancel);
    Button btnConfirm = view.findViewById(R.id.btn_confirm);

    tvTitle.setText(
        String.format(
            XKitUtils.getApplicationContext()
                .getString(R.string.live_anchor_invite_audience_join_seats),
            anchorName));

    btnCancel.setOnClickListener(
        v -> {
          if (listener != null) listener.onCancel();
          dismiss();
        });
    btnConfirm.setOnClickListener(
        v -> {
          if (listener != null) listener.onConfirm();
        });

    startCountdown(btnCancel);

    return view;
  }

  private void startCountdown(Button btnCancel) {
    handler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            if (countdown > 0) {
              btnCancel.setText(
                  String.format(
                      XKitUtils.getApplicationContext().getString(R.string.live_reject_countdown),
                      countdown));
              countdown--;
              handler.postDelayed(this, 1000);
            } else {
              if (listener != null) listener.onCancel();
              dismiss();
            }
          }
        },
        0);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    handler.removeCallbacksAndMessages(null);
  }
}
