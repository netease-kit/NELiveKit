// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.livestreamkit.fragment;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.livestreamkit.R;
import com.netease.yunxin.app.livestreamkit.databinding.FragmentUserCenterBinding;
import com.netease.yunxin.app.livestreamkit.utils.AppUtils;
import com.netease.yunxin.app.livestreamkit.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;
import com.netease.yunxin.kit.common.ui.dialog.LoadingDialog;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.dialog.PhoneConsultBottomDialog;
import com.netease.yunxin.kit.entertainment.common.fragment.BaseFragment;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.roomkit.api.model.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserCenterFragment extends BaseFragment {
  private static final String TAG = "UserCenterFragment";
  private FragmentUserCenterBinding binding;
  private Dialog loadingDialog;
  private int count = 0;
  private int quality = -1;
  private static final int CALLBACK_TOTAL_COUNT = 2;
  private NERoomRtcLastmileProbeResult probeResult;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentUserCenterBinding.inflate(inflater, container, false);
    View rootView = binding.getRoot();
    initViews();
    initDataCenter();
    listenNetworkProbInfo();
    return rootView;
  }

  private void listenNetworkProbInfo() {}

  private void initViews() {
    initUser();
    binding.beautySetting.setOnClickListener(
        v -> {
          ArrayList<String> list = new ArrayList<>();
          list.add(Manifest.permission.CAMERA);
          list.add(Manifest.permission.RECORD_AUDIO);
          Permission.requirePermissions(getActivity(), list.toArray(new String[0]))
              .request(
                  new Permission.PermissionCallback() {

                    @Override
                    public void onGranted(@NonNull List<String> granted) {
                      if (new HashSet<>(granted).containsAll(list)) {
                        NavUtils.toBeautySettingPage(requireActivity());
                      }
                    }

                    @Override
                    public void onDenial(
                        List<String> permissionsDenial, List<String> permissionDenialForever) {
                      for (String s : permissionsDenial) {
                        ALog.e(TAG, "permissionsDenial:" + s);
                      }
                      for (String s : permissionDenialForever) {
                        ALog.e(TAG, "permissionDenialForever:" + s);
                      }
                      if (!permissionsDenial.isEmpty()) {
                        ToastX.showShortToast(R.string.permission_request_failed_tips);
                      }
                      if (!permissionDenialForever.isEmpty()) {
                        DialogUtil.showConfirmDialog(
                            requireActivity(),
                            getString(R.string.app_tip),
                            getString(R.string.app_permission_content),
                            getString(R.string.app_cancel),
                            getString(R.string.app_ok),
                            new CommonConfirmDialog.Callback() {

                              @Override
                              public void result(@Nullable Boolean aBoolean) {
                                if (aBoolean != null && aBoolean) {
                                  NavUtils.goToSetting(requireActivity());
                                }
                              }
                            });
                      }
                    }

                    @Override
                    public void onException(Exception exception) {}
                  });
        });
    binding.networkDetect.setOnClickListener(
        v -> {
          NELiveStreamKit.getInstance().startLastmileProbeTest(new NERoomRtcLastmileProbeConfig());
          toggleLoading(true);
        });
    binding.commonSetting.setOnClickListener(v -> NavUtils.toCommonSettingPage(requireActivity()));
    binding.phoneConsult.setOnClickListener(
        v -> {
          PhoneConsultBottomDialog dialog = new PhoneConsultBottomDialog(requireActivity());
          dialog.show();
        });
  }

  private void initUser() {
    binding.ivUserPortrait.loadAvatar(AppUtils.getAvatar());
    binding.tvUserName.setText(AppUtils.getUserName());
  }

  private void initDataCenter() {}

  private void toggleLoading(boolean show) {
    if (loadingDialog == null) {
      loadingDialog = new LoadingDialog(requireActivity());
    }
    if (show && !loadingDialog.isShowing()) {
      loadingDialog.show();
    } else if (!show) {
      loadingDialog.dismiss();
      loadingDialog = null;
    }
  }
}
