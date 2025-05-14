/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.view.audience

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.netease.yunxin.kit.common.image.ImageLoader
import com.netease.yunxin.kit.livestreamkit.ui.R

class AudienceErrorStateView : ConstraintLayout {
    /**
     * 背景模糊大图
     */
    private var ivBgView: ImageView? = null

    /**
     * 主播头像
     */
    private var ivPortrait: ImageView? = null

    /**
     * 主播昵称
     */
    private var tvNickname: TextView? = null

    /**
     * 错误提示
     */
    private var tvTip: TextView? = null

    private var ivPower: View? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.live_view_anchor_finishing_living, this, true)
        ivBgView = findViewById(R.id.iv_finishing_bg)
        ivPortrait = findViewById(R.id.iv_finishing_anchor_portrait)
        tvNickname = findViewById(R.id.tv_finishing_anchor_naming)
        tvTip = findViewById(R.id.tv_finishing_tip)
        ivPower = findViewById(R.id.iv_power)

        ivPower!!.setOnClickListener { v: View? -> (context as (Activity)).finish() }
    }

    /**
     * 基础信息渲染
     *
     * @param portraitUrl 头像url
     * @param nickname    昵称
     */
    fun renderInfo(portraitUrl: String?, nickname: String?) {
        ImageLoader.with(context.applicationContext).circleLoad(portraitUrl, ivPortrait)
        ImageLoader.with(context.applicationContext).load(portraitUrl).blurCenterCrop(15, 5)
            .into(ivBgView)
        tvNickname?.text = nickname
    }

    /**
     * 更新错误类型
     *
     * @param type     类型详见 [.TYPE_ERROR] [.TYPE_FINISHED]
     * @param listener 按钮点击监听
     */
    fun updateType(type: Int, listener: ClickButtonListener?) {
        val back = findViewById<View?>(R.id.tv_error_back)
        val retry = findViewById<View?>(R.id.tv_error_retry)
        val groupError: Group = findViewById(R.id.group_error)
        if (type == TYPE_ERROR) {
            groupError.visibility = VISIBLE
            back.setOnClickListener { v: View? -> listener?.onBackClick(v) }
            retry.setOnClickListener { v: View? -> listener?.onRetryClick(v) }
            tvTip?.setText(R.string.biz_live_follow_anchor_be_lost)
        } else if (type == TYPE_FINISHED) {
            groupError.visibility = GONE
            tvTip?.setText(R.string.biz_live_live_is_end)
            val errorBack = findViewById<View?>(R.id.iv_power)
            errorBack.setOnClickListener { v: View? -> listener?.onBackClick(v) }
        }
    }

    interface ClickButtonListener {
        open fun onBackClick(view: View?)
        open fun onRetryClick(view: View?)
    }

    companion object {
        /**
         * 正常结束直播状态
         */
        const val TYPE_FINISHED = 1

        /**
         * 拉流错误或获取信息错误状态
         */
        const val TYPE_ERROR = 2
    }
}
