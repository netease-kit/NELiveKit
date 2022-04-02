/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.kit.roomkit.api.view.NERoomVideoView

class PKVideoView : LinearLayout {
    private var localVideo: NERoomVideoView? = null
    private var remoteVideo: NERoomVideoView? = null

    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.pk_video_view_layout, this, true)
        localVideo = findViewById(R.id.local_video)
        remoteVideo = findViewById(R.id.remote_video)
    }

    fun getLocalVideo(): NERoomVideoView? {
        return localVideo
    }

    fun getRemoteVideo(): NERoomVideoView? {
        return remoteVideo
    }
}