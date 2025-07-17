// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.activity;

import android.text.*;
import android.text.method.*;
import android.view.*;
import androidx.annotation.*;
import com.netease.yunxin.kit.common.ui.utils.*;
import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.entertainment.common.*;
import com.netease.yunxin.kit.entertainment.common.activity.*;
import com.netease.yunxin.kit.entertainment.common.databinding.*;
import com.netease.yunxin.kit.entertainment.common.utils.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import kotlin.*;

public class AuthenticateActivity extends BasePartyActivity {
  private ActivityAuthenticateBinding binding;
  private static final String DIGISTS =
      "0123456789" + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private final DigitsKeyListener digitsKeyListener =
      new DigitsKeyListener() {
        @Override
        public int getInputType() {
          return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
        }

        @Override
        protected char[] getAcceptedChars() {
          return DIGISTS.toCharArray();
        }
      };

  private final TextWatcher textWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
          checkSubmitEnable();
        }
      };

  @Override
  protected void init() {
    paddingStatusBarHeight(binding.getRoot());
    initEvent();
  }

  @Override
  protected View getRootView() {
    binding = ActivityAuthenticateBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  private void initEvent() {
    binding.btnSubmit.setOnClickListener(v -> submitAuthenticate());
    binding.etName.addTextChangedListener(textWatcher);
    binding.etId.setKeyListener(digitsKeyListener);
    binding.etId.setTransformationMethod(new TransInformation());
    binding.etId.addTextChangedListener(textWatcher);
    binding.checkBox.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          checkSubmitEnable();
        });
  }

  private void checkSubmitEnable() {
    binding.btnSubmit.setEnabled(
        binding.checkBox.isChecked()
            && binding.etId.getText().toString().length() > 0
            && binding.etName.getText().toString().length() > 0);
  }

  private void submitAuthenticate() {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(R.string.network_error);
      return;
    }

    NELiveStreamKit.getInstance()
        .authenticate(
            binding.etName.getText().toString(),
            binding.etId.getText().toString(),
            new NELiveStreamCallback<Unit>() {

              @Override
              public void onFailure(int code, @Nullable String msg) {
                DialogUtil.showAlertDialog(
                    AuthenticateActivity.this,
                    getString(R.string.voiceroom_authentication_error),
                    getString(R.string.voiceroom_authentication_know));
              }

              @Override
              public void onSuccess(@Nullable Unit unit) {
                ToastX.showShortToast(R.string.voiceroom_authentication_success);
                finish();
              }
            });
  }

  public static class TransInformation extends ReplacementTransformationMethod {
    /** 原本输入的小写字母 */
    @Override
    protected char[] getOriginal() {
      return new char[] {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
      };
    }

    /** 替代为大写字母 */
    @Override
    protected char[] getReplacement() {
      return new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
      };
    }
  }
}
