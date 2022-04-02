/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.fragment

import android.view.View
import androidx.fragment.app.Fragment
import com.netease.yunxin.app.newlive.config.StatusBarConfig

/**
 * Created by luc on 2020/11/13.
 */
open class BaseFragment : Fragment() {
    protected fun paddingStatusBarHeight(view: View?) {
        StatusBarConfig.paddingStatusBarHeight(activity, view)
    }
}