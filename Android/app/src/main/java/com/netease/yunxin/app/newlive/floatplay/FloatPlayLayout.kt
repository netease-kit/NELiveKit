package com.netease.yunxin.app.newlive.floatplay

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.utils.SpUtils.dp2pix
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.livekit.NELiveCallback

/**
 * 小窗播放UI
 */
class FloatPlayLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var videoView: TextureView? = null
        private set
    var rtmpPullUrl=""
    private val onNetworkStatusChangedListener: NetworkUtils.OnNetworkStatusChangedListener =
        object : NetworkUtils.OnNetworkStatusChangedListener {
            override fun onDisconnected() {
                FloatPlayLogUtil.log(TAG, "onDisconnected")
            }

            override fun onConnected(networkType: NetworkUtils.NetworkType) {
                FloatPlayLogUtil.log(TAG, "onConnected:$networkType")
                if (!TextUtils.isEmpty(rtmpPullUrl)){
                    LiveVideoPlayerManager.getInstance().resumePlay(rtmpPullUrl)
                }
            }
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.live_float_play_ui, this)
        videoView = findViewById<View>(R.id.videoView) as TextureView
        val ivClose = findViewById<View>(R.id.iv_close) as ImageView
        ivClose.setOnClickListener {
            release()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        NetworkUtils.registerNetworkStatusChangedListener(onNetworkStatusChangedListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        NetworkUtils.unregisterNetworkStatusChangedListener(onNetworkStatusChangedListener)
    }

    fun setPlayUrl(url:String){
        rtmpPullUrl=url
    }

    fun release() {
        LiveKitManager.getInstance().liveKit.leaveLive(object : NELiveCallback<Unit>{
            override fun onSuccess(t: Unit?) {
                ALog.d(TAG,"leaveRoom success")
            }

            override fun onFailure(code: Int, msg: String?) {
                ALog.d(TAG,"leaveRoom error")
            }

        })
        FloatPlayManager.stopFloatPlay()
        FloatPlayManager.release()
        LiveVideoPlayerManager.getInstance().release()
        AudienceDataManager.clear()
    }

    companion object {
        private const val TAG="FloatPlayLayout"
        //  pk直播 width 720, height 640
        //  单主播 width 720, height 1280
        @JvmField
        val SINGLE_ANCHOR_WIDTH = dp2pix(90f)

        @JvmField
        val SINGLE_ANCHOR_HEIGHT = dp2pix(160f)

        @JvmField
        val PK_LIVE_WIDTH = dp2pix(144f)

        @JvmField
        val PK_LIVE_HEIGHT = dp2pix(128f)

        @JvmField
        val MARGIN_RIGHT = dp2pix(20f)

        @JvmField
        val MARGIN_BOTTOM = dp2pix(120f)
    }
}