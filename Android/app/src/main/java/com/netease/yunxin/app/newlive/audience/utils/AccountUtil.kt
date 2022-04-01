/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.audience.utils

import android.text.TextUtils
import com.netease.yunxin.kit.login.AuthorManager

object AccountUtil {
    fun isCurrentUser(accountId: String?): Boolean {
        val currentUser =
            AuthorManager.getUserInfo()
        return currentUser != null && !TextUtils.isEmpty(currentUser.accountId) && currentUser.accountId == accountId
    }
}