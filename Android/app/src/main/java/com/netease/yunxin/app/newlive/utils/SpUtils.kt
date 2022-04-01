/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.utils

import android.content.Context
import com.netease.yunxin.app.newlive.PKApplication

/**
 * Created by luc on 2020/11/11.
 */
object SpUtils {
    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     */
    fun getScreenWidth(): Int {
        return PKApplication.getApplication().resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     */
    fun getScreenHeight(): Int {
        return PKApplication.getApplication().resources.displayMetrics.heightPixels
    }

    /**
     * dp 转换成 pixel
     */
    fun dp2pix(dp: Float): Int {
        val density = PKApplication.getApplication().resources.displayMetrics.density
        return (density * dp + 0.5f).toInt()
    }
}