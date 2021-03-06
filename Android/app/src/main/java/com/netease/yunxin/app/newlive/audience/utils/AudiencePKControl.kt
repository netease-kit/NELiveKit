/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.audience.utils

import android.app.Activity
import android.graphics.PointF
import android.view.TextureView
import android.view.View
import com.blankj.utilcode.util.Utils
import com.netease.yunxin.app.newlive.Constants
import com.netease.yunxin.app.newlive.LiveTimeDef
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.app.newlive.config.StatusBarConfig
import com.netease.yunxin.app.newlive.utils.SpUtils
import com.netease.yunxin.app.newlive.widget.PKControlView
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.livekit.model.pk.NEPKUser
import com.netease.yunxin.kit.livekit.model.reward.RewardAudience

/**
 * 观众端PK管理
 */
class AudiencePKControl {

    /**
     * 直播播放View
     */
    private var videoView: TextureView? = null

    /**
     * pk 状态整体控制
     */
    private var pkControlView: PKControlView? = null

    /**
     * pk 阶段倒计时
     */
    private var countDownTimer: PKControlView.WrapperCountDownTimer? = null
    private var activity: Activity? = null
    fun init(activity: Activity?, videoView: TextureView?, infoContentView: View) {
        this.activity = activity
        this.videoView = videoView
        pkControlView = infoContentView.findViewById(R.id.pkv_control)
    }

    fun onAnchorCoinChanged(
        anchorTotal: Long, otherAnchorTotal: Long,
        selfRewardTop: List<RewardAudience>?,
        otherRewardTop: List<RewardAudience>?
    ) {
        if (pkControlView?.visibility == View.VISIBLE) {
            pkControlView?.updateScore(anchorTotal, otherAnchorTotal)
            pkControlView?.updateRanking(selfRewardTop, otherRewardTop)
        }
    }


    /**
     * leftTime (s)
     */
    fun onPkStart(otherAnchor: NEPKUser, leftTime: Int, setVideoSize: Boolean = true) {
        ALog.d(TAG, "onPkStart leftTime$leftTime")
        countDownTimer?.stop()

        // pk 状态下view渲染
        pkControlView?.visibility = View.VISIBLE
        // 重置pk控制view
        pkControlView?.reset()
        // 设置pk 主播昵称/头像
        pkControlView?.updatePkAnchorInfo(
            otherAnchor.nickname,
            otherAnchor.avatar
        )
        // 调整视频播放比例
        if (setVideoSize) {
            adjustVideoSizeForPk(true)
        }
        countDownTimer =
            pkControlView?.createCountDownTimer(LiveTimeDef.TYPE_PK, leftTime * 1000L)
        countDownTimer?.start()
    }

    fun adjustVideoSizeForPk(isPrepared: Boolean) {
        val width = SpUtils.getScreenWidth()
        val height = (width / Constants.StreamLayout.WH_RATIO_PK).toInt()
        val x = width / 2f
        val y = StatusBarConfig.getStatusBarHeight(activity) + SpUtils.dp2pix(
            64f
        ) + height / 2f
        val pivot = PointF(x, y)
        ALog.e(TAG, "pk video view center point is $pivot")
        if (isPrepared) {
            PlayerVideoSizeUtils.adjustForPreparePk(videoView, pivot)
        } else {
            PlayerVideoSizeUtils.adjustViewSizePosition(videoView, true, pivot)
        }
    }


    fun onPunishmentStart(
        otherAnchor: NEPKUser? = null,
        pkResult: Int,
        countDown: Int,
        initView: Boolean = false
    ) {
        countDownTimer?.stop()
        if (initView) {
            // pk 状态下view渲染
            pkControlView?.visibility = View.VISIBLE
            // 重置pk控制view
            pkControlView?.reset()
            // 设置pk 主播昵称/头像
            pkControlView?.updatePkAnchorInfo(
                otherAnchor?.nickname,
                otherAnchor?.avatar
            )
//            adjustVideoSizeForPk(true)
        }
        pkControlView?.handleResultFlag(true, pkResult)
        // 定时器倒计时
        if (pkResult != PKControlView.PK_RESULT_DRAW) {
            val leftTime = countDown * 1000L
            countDownTimer = pkControlView?.createCountDownTimer(
                Utils.getApp().getString(R.string.biz_live_punishment),
                leftTime
            )
            countDownTimer?.start()
        }
    }

    fun onPkEnd(){
        pkControlView?.visibility = View.INVISIBLE
    }


    fun isPk(): Boolean {
        return pkControlView?.visibility == View.VISIBLE
    }

    fun release() {
        // pk 状态隐藏
        countDownTimer?.stop()
        pkControlView?.visibility = View.INVISIBLE
    }

    companion object {
        private const val TAG: String = "AudiencePKControl"
    }
}