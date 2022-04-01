package com.netease.yunxin.app.newlive.floatplay

import android.content.Context
import android.graphics.SurfaceTexture
import android.widget.FrameLayout
import com.netease.yunxin.app.newlive.audience.ui.LiveAudienceActivity
import com.netease.yunxin.app.newlive.audience.ui.LiveAudienceActivity.Companion.launchAudiencePage
import com.netease.yunxin.app.newlive.chatroom.ChatRoomMsgCreator
import com.netease.yunxin.app.newlive.floatplay.FloatPlayLogUtil.log
import com.netease.yunxin.app.newlive.gift.GiftCache
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.live.MyLiveListener
import com.netease.yunxin.app.newlive.utils.SpUtils.getScreenHeight
import com.netease.yunxin.app.newlive.utils.SpUtils.getScreenWidth
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.livekit.LiveTypeManager
import com.netease.yunxin.kit.livekit.NELiveConstants
import com.netease.yunxin.kit.livekit.model.LiveConfig
import com.netease.yunxin.kit.livekit.model.RewardMsg
import com.netease.yunxin.kit.livekit.utils.GsonUtils
import com.netease.yunxin.kit.livekit.utils.SysUtil
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomTextMessage

object FloatPlayManager {
    private const val TAG = "FloatPlayManager"
    private var mIsShowing = false
    private var floatPlayLayout: FloatPlayLayout? = null
    private var floatView: FloatView? = null
    private var currentLiveType = NELiveConstants.LiveType.LIVE_TYPE_DEFAULT
    var flotWindowWidth = 0
    var flotWindowHeight = 0
    var roomId=""

    private val liveListener = object : MyLiveListener() {

        override fun onTextMessageReceived(message: NERoomTextMessage) {
            ALog.d(TAG,"onRecvRoomTextMsg:${message.fromAccount},msg:"+message.text)
            if (AudienceDataManager.hasCache(roomId)){
                val msg = ChatRoomMsgCreator.createText(
                    isAnchor(message.fromAccount),
                    message.fromAccount,
                    message.text
                )
                AudienceDataManager.getDataFromCache()?.chatRoomMsgList?.add(msg)
            }
        }

        override fun onRewardReceived(rewardInfo: RewardMsg) {
            ALog.d(TAG,"onUserReward:$rewardInfo")
            if (AudienceDataManager.hasCache(roomId)){
                val msg = ChatRoomMsgCreator.createGiftReward(
                    rewardInfo.rewarderNickname,
                    1, GiftCache.getGift(rewardInfo.giftId).staticIconResId
                )
                AudienceDataManager.getDataFromCache()?.rewardTotal=rewardInfo.anchorReward.rewardTotal
                AudienceDataManager.getDataFromCache()?.chatRoomMsgList?.add(msg!!)
            }
        }

        override fun onMembersJoin(members: List<NERoomMember>) {
            for(roomMember in members) {
                ALog.d(TAG, "onUserEntered:${roomMember.uuid}")
                if (AudienceDataManager.hasCache(roomId)) {
                    val msg = ChatRoomMsgCreator.createRoomEnter(roomMember.uuid)
                    AudienceDataManager.getDataFromCache()?.chatRoomMsgList?.add(msg)
                }
            }
        }

        override fun onMembersLeave(members: List<NERoomMember>) {
            for(roomMember in members) {
                ALog.d(TAG, "onUserLeft:$roomMember.uuid")
                if (AudienceDataManager.hasCache(roomId)) {
                    val msg = ChatRoomMsgCreator.createRoomExit(roomMember.uuid)
                    AudienceDataManager.getDataFromCache()?.chatRoomMsgList?.add(msg)
                }
            }
        }

        override fun onLoginKickedOut() {
            ALog.d(TAG,"onKickedOut")
            closeFloatPlay()
        }

        override fun onLiveEnd(reason: Int) {
            ALog.d(TAG,"onLiveEnd")
            closeFloatPlay()
        }

        override fun onMemberCountChange(members: List<NERoomMember>?) {
            ALog.d(TAG,"onUserCountChange")
            if (AudienceDataManager.hasCache(roomId)){
                AudienceDataManager.getDataFromCache()?.userCount = members?.size?: 0
            }

            ALog.d(TAG,"onAudienceChange")
            if (AudienceDataManager.hasCache(roomId)){
                AudienceDataManager.getDataFromCache()?.userList=members
            }
        }
    }

    private val playerNotify: LiveVideoPlayerManager.PlayerNotify =
        object : LiveVideoPlayerManager.PlayerNotify {
            override fun onPreparing() {}
            override fun onPlaying() {}
            override fun onError() {

            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                if (floatPlayLayout != null) {
                    if (!isStartFloatWindow) {
                        return
                    }
                    AudienceDataManager.getDataFromCache()?.videoInfo?.videoWidth=width
                    AudienceDataManager.getDataFromCache()?.videoInfo?.videoHeight=height
                    if (CDNStreamTextureView.isPkSize(width, height)) {
                        flotWindowWidth = FloatPlayLayout.PK_LIVE_WIDTH
                        flotWindowHeight = FloatPlayLayout.PK_LIVE_HEIGHT
                        currentLiveType = NELiveConstants.LiveType.LIVE_TYPE_PK
                    } else {
                        flotWindowWidth = FloatPlayLayout.SINGLE_ANCHOR_WIDTH
                        flotWindowHeight = FloatPlayLayout.SINGLE_ANCHOR_HEIGHT
                        currentLiveType = NELiveConstants.LiveType.LIVE_TYPE_DEFAULT
                    }
                    floatView?.update(flotWindowWidth, flotWindowHeight)
                }
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }
        }

    fun startFloatPlay(context: Context, roomId: String) {
        mIsShowing = true
        FloatPlayManager.roomId =roomId
        ALog.d(TAG, "startFloatPlay,roomId:$roomId")
        currentLiveType = LiveTypeManager.getCurrentLiveType()
        if (LiveTypeManager.getCurrentLiveType()== NELiveConstants.LiveType.LIVE_TYPE_DEFAULT
            && CDNStreamTextureView.isSingleAnchorSize(
                AudienceDataManager.getDataFromCache()?.videoInfo?.videoWidth!!
                , AudienceDataManager.getDataFromCache()?.videoInfo?.videoHeight!!)) {
            flotWindowWidth = FloatPlayLayout.SINGLE_ANCHOR_WIDTH
            flotWindowHeight = FloatPlayLayout.SINGLE_ANCHOR_HEIGHT
        } else if (LiveTypeManager.getCurrentLiveType()== NELiveConstants.LiveType.LIVE_TYPE_PK
            && CDNStreamTextureView.isPkSize(
                AudienceDataManager.getDataFromCache()?.videoInfo?.videoWidth!!
                , AudienceDataManager.getDataFromCache()?.videoInfo?.videoHeight!!)){
            flotWindowWidth = FloatPlayLayout.PK_LIVE_WIDTH
            flotWindowHeight = FloatPlayLayout.PK_LIVE_HEIGHT
        }
        log(TAG, "currentLiveType:$currentLiveType")
        floatPlayLayout = FloatPlayLayout(context.applicationContext)
        floatView = FloatView(context.applicationContext)
        floatView?.layoutParams = FrameLayout.LayoutParams(flotWindowWidth, flotWindowHeight)
        floatView?.addView(floatPlayLayout)
        floatView?.addToWindow()
        floatView?.setOnFloatViewClickListener(object : FloatView.OnFloatViewClickListener {
            override fun onClick() {
                if (!SysUtil.isAppRunningForeground(context)){
                    SysUtil.wakeupAppToForeground(context, LiveAudienceActivity::class.java)
                }
                launchAudiencePage(
                    context,
                    AudienceDataManager.getDataFromCache()?.infoList,
                    AudienceDataManager.getDataFromCache()?.currentPosition!!
                )
            }

        })
        floatView?.update(
            flotWindowWidth,
            flotWindowHeight,
            getScreenWidth() - FloatPlayLayout.MARGIN_RIGHT - flotWindowWidth,
            getScreenHeight() - FloatPlayLayout.MARGIN_BOTTOM - flotWindowHeight
        )
        val videoView = floatPlayLayout!!.videoView
        LiveVideoPlayerManager.getInstance().addVideoPlayerObserver(playerNotify)

        val liveConfig = AudienceDataManager.getDataFromCache()?.liveInfo!!.live.liveConfig
        val rtmpPullUrl = GsonUtils.fromJson(liveConfig, LiveConfig::class.java).pullRtmpUrl

        LiveVideoPlayerManager.getInstance().startPlay(
            rtmpPullUrl,
            videoView!!
        )
        floatPlayLayout?.setPlayUrl(rtmpPullUrl)
        log(TAG, " startFloatPlay:$mIsShowing")
        LiveKitManager.getInstance().addLiveListener(liveListener)
    }

    fun stopFloatPlay() {
        if (!mIsShowing) {
            log(TAG, "stopFloatPlay return mIsShowing:$mIsShowing")
            return
        }
        floatView?.removeFromWindow()
        //todo addDelegate
//        LiveRoomService.sharedInstance().removeDelegate(roomDelegate)
        mIsShowing = false
        log(TAG, "stopFloatPlay mIsShowing:$mIsShowing")
    }

    val isStartFloatWindow: Boolean
        get() {
            log(TAG, "isStartFloatWindow:$mIsShowing")
            return mIsShowing
        }

    fun release() {
        log(TAG, "release()")
        mIsShowing = false
        floatView = null
        floatPlayLayout = null
        LiveVideoPlayerManager.getInstance().removeVideoPlayerObserver(playerNotify)
        LiveKitManager.getInstance().removeLiveListener(liveListener)
    }

    fun closeFloatPlay(){
        floatPlayLayout?.release()
    }

    private fun isAnchor(fromAccount : String): Boolean{
        return AudienceDataManager.getDataFromCache()?.liveInfo?.anchor?.userUuid == fromAccount
    }
}