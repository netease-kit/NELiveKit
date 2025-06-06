// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.model;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomMember;
import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/** 个人资料信息（语聊房使用） */
public class VoiceRoomUser implements Serializable {
  /** 账号 */
  public final String account;

  /** 昵称 */
  public final String nick;

  /** 头像 */
  public final String avatar;

  public VoiceRoomUser(String account, String nick, String avatar) {
    this.account = account;
    this.nick = nick;
    this.avatar = avatar;
  }

  public VoiceRoomUser(NEVoiceRoomMember member) {
    this.account = member.getAccount();
    this.nick = member.getName();
    this.avatar = member.getAvatar();
  }

  private static final String ACCOUNT_KEY = "account";
  private static final String NICK_KEY = "nick";
  private static final String AVATAR_KEY = "avatar";

  public VoiceRoomUser(@NonNull JSONObject jsonObject) {
    account = jsonObject.optString(ACCOUNT_KEY, "");
    nick = jsonObject.optString(NICK_KEY, "");
    avatar = jsonObject.optString(AVATAR_KEY, "");
  }

  public JSONObject toJson() {
    JSONObject jsonObject = new JSONObject();
    try {
      if (!TextUtils.isEmpty(account)) {
        jsonObject.put(ACCOUNT_KEY, account);
      }
      if (!TextUtils.isEmpty(nick)) {
        jsonObject.put(NICK_KEY, nick);
      }
      if (!TextUtils.isEmpty(avatar)) {
        jsonObject.put(AVATAR_KEY, avatar);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  public String getAccount() {
    return account;
  }

  public String getNick() {
    return nick;
  }

  public String getAvatar() {
    return avatar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VoiceRoomUser other = (VoiceRoomUser) o;
    return account.equals(other.account);
  }

  @Override
  public int hashCode() {
    return account.hashCode();
  }
}
