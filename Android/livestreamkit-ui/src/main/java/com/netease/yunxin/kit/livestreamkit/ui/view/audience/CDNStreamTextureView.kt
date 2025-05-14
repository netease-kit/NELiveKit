/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.view.audience

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.TextureView
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.common.utils.ScreenUtils
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIConstants
import com.netease.yunxin.kit.livestreamkit.ui.utils.PlayerVideoSizeUtils

/**
 * 播放直播间的CDN流
 */
class CDNStreamTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextureView(context, attrs) {

    companion object {
        private const val TAG: String = "CDNStreamTextureView"
        fun isPkSize(videoWidth: Int, videoHeight: Int): Boolean {
            if (videoWidth == 0 || videoHeight == 0) {
                return false
            }
            return videoWidth / videoHeight == LiveStreamUIConstants.StreamLayout.PK_LIVE_WIDTH * 2 / LiveStreamUIConstants.StreamLayout.PK_LIVE_HEIGHT
        }

        fun isSingleAnchorSize(videoWidth: Int, videoHeight: Int): Boolean {
            if (videoWidth == 0 || videoHeight == 0) {
                return false
            }
            return videoWidth / videoHeight == LiveStreamUIConstants.StreamLayout.SIGNAL_HOST_LIVE_WIDTH / LiveStreamUIConstants.StreamLayout.SIGNAL_HOST_LIVE_HEIGHT
        }

        private val MAX_RETRY_COUNT: Int = Int.MAX_VALUE // 无限重试
        private const val RETRY_INTERVAL: Long = 3000 // 3秒重试间隔
    }

    /**
     * 是否正在连麦
     */
    private var isLinkingSeats = false

    private var isPK = false
    private var currentRetryCount = 0
    private var isRetrying = false
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var pullUrl: String? = null

    private val retryPlayRunnable = Runnable {
        if (isRetrying && currentRetryCount < MAX_RETRY_COUNT) {
            ALog.i(TAG, "Retrying playback, attempt: " + (currentRetryCount + 1))
            retryPlay()
        }
    }

    private val playerNotify = object :
        LiveVideoPlayerManager.PlayerNotify {
        override fun onPreparing() {
            ALog.d(TAG, "onPreparing()")
            stopRetry()
        }

        override fun onPlaying() {
            ALog.d(TAG, "onPlaying()")
            stopRetry()
        }

        override fun onError(code: Int, msg: String) {
            ALog.d(TAG, "onError code:$code, msg:$msg")
            startRetry()
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            ALog.d(TAG, "onVideoSizeChanged(),width:$width,height:$height")
            isPK = isPkSize(width, height)
            refreshTextureView()
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            ALog.d(TAG, "onSurfaceTextureAvailable()")
            refreshTextureView()
        }
    }

    fun prepare() {
        ALog.d(TAG, "prepare playNotify:$playerNotify")
        LiveVideoPlayerManager.getInstance().addVideoPlayerObserver(playerNotify)
    }

    fun startPlay(pullUrl: String) {
        ALog.d(TAG, "startPlay pullUrl:$pullUrl")
        this.pullUrl = pullUrl
        LiveVideoPlayerManager.getInstance().startPlay(pullUrl, this)
    }

    fun setLinkingSeats(linkingSeats: Boolean) {
        isLinkingSeats = linkingSeats
        post {
            if (isLinkingSeats) {
                adjustVideoSizeForLinkSeats()
            } else {
                adjustVideoSizeForNormal()
            }
        }
    }

    private fun adjustVideoSizeForLinkSeats() {
        // 宽满屏，VideoView按视频的宽高比同比例放大，VideoView在屏幕居中展示
        // 目标视频比例
        val videoWidth: Float = LiveStreamUIConstants.StreamLayout.SIGNAL_HOST_LIVE_WIDTH.toFloat()
        val videoHeight: Float = LiveStreamUIConstants.StreamLayout.SIGNAL_HOST_LIVE_HEIGHT.toFloat()
        val viewWidth = ScreenUtils.getDisplayWidth()
        val viewHeight = ScreenUtils.getDisplayHeight()
        // 填充满 720*1280区域
        val matrix = Matrix()
        // 平移 使 view 中心和 video 中心一致
        matrix.preTranslate((viewWidth - videoWidth) / 2f, (viewHeight - videoHeight) / 2f)
        // 缩放 view 至原视频大小
        matrix.preScale(videoWidth / viewWidth, videoHeight / viewHeight)
        matrix.postScale(
            viewWidth / videoWidth,
            viewWidth / videoWidth,
            viewWidth / 2f,
            viewHeight / 2f
        )
        setTransform(matrix)
        postInvalidate()
    }

    fun adjustVideoSizeForPk(isPrepared: Boolean) {
//        val width = SpUtils.getScreenWidth(context)
//        val height = (width / Constants.StreamLayout.WH_RATIO_PK).toInt()
//        val x = width / 2f
//        val y = StatusBarConfig.getStatusBarHeight(ActivityUtils.getTopActivity()) + SpUtils.dp2pix(
//            context,
//            64f
//        ) + height / 2f
//        val pivot = PointF(x, y)
//        ALog.d(TAG, "pk video view center point is $pivot")
//        if (isPrepared) {
//            PlayerVideoSizeUtils.adjustForPreparePk(this, pivot)
//        } else {
//            PlayerVideoSizeUtils.adjustViewSizePosition(this, true, pivot)
//        }
    }

    fun adjustVideoSizeForNormal() {
        PlayerVideoSizeUtils.adjustViewSizePosition(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRetry()
        LiveVideoPlayerManager.getInstance().removeVideoPlayerObserver(playerNotify)
        LiveVideoPlayerManager.getInstance().release()
    }

    fun refreshTextureView() {
        if (isPK) {
            adjustVideoSizeForPk(false)
        } else if (isLinkingSeats) {
            adjustVideoSizeForLinkSeats()
        } else {
            adjustVideoSizeForNormal()
        }
    }

    private fun startRetry() {
        if (!isRetrying) {
            isRetrying = true
            currentRetryCount = 0
            retryPlay()
        }
    }

    private fun retryPlay() {
        ALog.i(TAG, "Retrying playback with URL: $pullUrl")
        currentRetryCount++
        pullUrl?.let {
            startPlay(it)
        }

        // 安排下一次重试
        if (currentRetryCount < MAX_RETRY_COUNT) {
            handler.postDelayed(retryPlayRunnable, RETRY_INTERVAL)
        }
    }

    private fun stopRetry() {
        isRetrying = false
        handler.removeCallbacks(retryPlayRunnable)
    }
}
