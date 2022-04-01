/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.dialog

import android.app.Activity
import android.view.View
import com.netease.yunxin.app.newlive.R

/**
 * Created by luc on 2020/12/3.
 */
class NotificationDialog(activity: Activity) : ChoiceDialog(activity) {
    override fun renderRootView(rootView: View?) {
        super.renderRootView(rootView)
        rootView?.findViewById<View?>(R.id.line_divide)?.visibility =
            View.GONE
    }

    init {
        setCancelable(false)
    }
}