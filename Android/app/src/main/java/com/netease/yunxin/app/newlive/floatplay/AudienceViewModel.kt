package com.netease.yunxin.app.newlive.floatplay

import android.app.Application
import android.graphics.SurfaceTexture
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.netease.yunxin.app.newlive.audience.ui.view.AudienceErrorStateView
import com.netease.yunxin.app.newlive.chatroom.ChatRoomMsgCreator
import com.netease.yunxin.app.newlive.gift.GiftCache
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.live.MyLiveListener
import com.netease.yunxin.kit.livekit.NELiveCallback
import com.netease.yunxin.kit.livekit.model.ErrorInfo
import com.netease.yunxin.kit.livekit.model.LiveInfo
import com.netease.yunxin.kit.livekit.model.RewardMsg
import com.netease.yunxin.kit.livekit.model.pk.PkEndInfo
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomTextMessage

class AudienceViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AudienceViewModel"
    }

    val liveKitManager = LiveKitManager.getInstance()

    /**
     * 直播间基本信息
     */
    val liveInfoData by lazy {
        MutableLiveData<LiveInfo>()
    }

    val userListData by lazy {
        MutableLiveData<MutableList<NERoomMember>>()
    }

    val kickedOutData by lazy {
        MutableLiveData<Boolean>()
    }

    val userRewardData by lazy {
        MutableLiveData<RewardMsg>()
    }

    val newChatRoomMsgData by lazy {
        MutableLiveData<CharSequence>()
    }

    /**
     * 正在播放视频的高度，维护大窗，小窗单主播与PK主播的UI切换
     */
    val videoHeightData by lazy {
        MutableLiveData<Int>()
    }

    val errorInfoData by lazy {
        MutableLiveData<ErrorInfo>()
    }

    val errorStateData by lazy {
        MutableLiveData<Pair<Boolean, Int>>()
    }

    val userCountData by lazy {
        MutableLiveData<Int>()
    }

    var data: AudienceData? = null

    var liveInfo: LiveInfo? = null

    val cacheData by lazy {
        MutableLiveData<AudienceData>()
    }

    private val liveListener = object : MyLiveListener() {
        override fun onTextMessageReceived(message: NERoomTextMessage) {
            val msg = ChatRoomMsgCreator.createText(
                isAnchor(message.fromAccount),
                message.fromAccount,
                message.text
            )
            data?.chatRoomMsgList?.add(msg)
            newChatRoomMsgData.value = msg
            FloatPlayLogUtil.log(TAG, "onRecvRoomTextMsg,size:" + data?.chatRoomMsgList?.size)
            saveCache()
        }

        override fun onRewardReceived(rewardInfo: RewardMsg) {
            val msg = ChatRoomMsgCreator.createGiftReward(
                rewardInfo.rewarderNickname,
                1, GiftCache.getGift(rewardInfo.giftId).staticIconResId
            )
            if (TextUtils.equals(rewardInfo.anchorReward.userUuid, data!!.liveInfo?.anchor?.userUuid)){
                data?.chatRoomMsgList?.add(msg!!)
                newChatRoomMsgData.value = msg
            }
            userRewardData.value = rewardInfo
            data?.rewardTotal=rewardInfo.anchorReward.rewardTotal
            //update liveInfo list的数据
            liveInfo?.live?.rewardTotal = rewardInfo.anchorReward.rewardTotal
            FloatPlayLogUtil.log(TAG, "onUserReward")
            saveCache()
        }

        override fun onLiveStarted() {

        }

        override fun onMembersJoin(members: List<NERoomMember>) {
            for(member in members) {
                if (!TextUtils.equals(member.name, data?.liveInfo?.anchor?.userName)) {
                    val msg = ChatRoomMsgCreator.createRoomEnter(member.uuid)
                    data?.chatRoomMsgList?.add(msg)
                    newChatRoomMsgData.value = msg
                    FloatPlayLogUtil.log(TAG, "onUserEntered")
                    saveCache()
                }
            }
        }

        override fun onMembersLeave(members: List<NERoomMember>) {
            for(member in members) {
                if (!TextUtils.equals(member.name, data?.liveInfo?.anchor?.userName)) {
                    val msg = ChatRoomMsgCreator.createRoomExit(member.uuid)
                    data?.chatRoomMsgList?.add(msg)
                    newChatRoomMsgData.value = msg
                    FloatPlayLogUtil.log(TAG, "onUserLeft")
                    saveCache()
                }

            }
        }

        override fun onMemberCountChange(members: MutableList<NERoomMember>?) {
            userListData.value = members
            data?.userList = members
            saveCache()

            userCountData.value = members?.size?:0
            FloatPlayLogUtil.log(TAG, "onUserCountChange:$members")
            data?.userCount = members?.size?:0
            saveCache()
        }

        override fun onLoginKickedOut() {
            kickedOutData.value = true
            FloatPlayLogUtil.log(TAG, "onKickedOut")
        }

        override fun onPKEnd(endInfo: PkEndInfo) {

        }

        override fun onLiveEnd(reason: Int) {
            FloatPlayLogUtil.log(TAG, "onRoomDestroy")

            errorStateData.value =
                Pair(true, AudienceErrorStateView.TYPE_FINISHED)
            FloatPlayLogUtil.log(TAG, "onAnchorLeave,audienceViewModel:")
            FloatPlayLogUtil.log(TAG, "onAnchorLeave,errorStateData:$errorStateData")
        }

    }

    private val playNotify: LiveVideoPlayerManager.PlayerNotify =
        object : LiveVideoPlayerManager.PlayerNotify {
            override fun onPreparing() {
                FloatPlayLogUtil.log(TAG, "video play onPreparing()")
            }

            override fun onPlaying() {
                FloatPlayLogUtil.log(TAG, "video play onPlaying()")
            }

            override fun onError() {
                FloatPlayLogUtil.log(TAG, "video play onError()")
                errorStateData.value =
                    Pair(true, AudienceErrorStateView.TYPE_ERROR)
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                FloatPlayLogUtil.log(TAG, "onVideoSizeChanged(),width:$width,height:$height")
                videoHeightData.value = height
                data?.videoInfo?.videoWidth = width
                data?.videoInfo?.videoHeight = height
                saveCache()
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

        }

    fun select(liveInfo: LiveInfo) {
        this.liveInfo = liveInfo
        val roomId = liveInfo.live.roomUuid
        if (AudienceDataManager.hasCache(roomId)) {
            data = AudienceDataManager.getDataFromCache()
            cacheData.value = data
            FloatPlayLogUtil.log(TAG, "select has cache:" + data.toString())
        } else {
            data = AudienceData()
            FloatPlayLogUtil.log(TAG, "select no cache:")
            data?.liveInfo = liveInfo
            liveInfoData.value = liveInfo
            saveCache()
        }
        AudienceDataManager.setRoomId(roomId)
        // 监听房间信息，把相关UI需要用到的数据传到直播间
        liveKitManager.addLiveListener(liveListener)
        if (LiveVideoPlayerManager.getInstance().containsVideoPlayerObserver(playNotify)) {
            LiveVideoPlayerManager.getInstance()
                .removeVideoPlayerObserver(playNotify)
        }
        LiveVideoPlayerManager.getInstance()
            .addVideoPlayerObserver(playNotify)
    }

    fun appendChatRoomMsg(msg: CharSequence) {
        data?.chatRoomMsgList?.add(msg)
        newChatRoomMsgData.value = msg
        FloatPlayLogUtil.log(TAG, "appendChatRoomMsg")
    }

    override fun onCleared() {
        super.onCleared()
        FloatPlayLogUtil.log(TAG, "onCleared()")
        LiveVideoPlayerManager.getInstance()
            .removeVideoPlayerObserver(playNotify)
    }

    fun saveListInfoAndPosition(infoList: MutableList<LiveInfo>, currentPosition: Int) {
        data?.infoList = infoList as ArrayList<LiveInfo>
        data?.currentPosition = currentPosition
        saveCache()
    }

    fun refreshLiveInfo(liveInfo: LiveInfo) {
        data?.liveInfo = liveInfo
        saveCache()
    }

    fun queryRoomDetailInfo(liveInfo: LiveInfo){
       queryLiveRoomInfo(liveInfo)
       queryChatRoomInfo(liveInfo)
    }

    private fun isAnchor(fromAccount : String): Boolean{
        return data?.liveInfo?.anchor?.userUuid == fromAccount
    }

    private fun queryLiveRoomInfo(liveInfo: LiveInfo) {
        liveKitManager.liveKit.requestLiveInfo(liveInfo.live.liveRecordId, object : NELiveCallback<LiveInfo>{
            override fun onSuccess(it: LiveInfo?) {
                it?.let {
                    liveInfoData.value = it
                    data?.liveInfo=it
                    saveCache()
                    FloatPlayLogUtil.log(TAG, "queryLiveRoomInfo success:$it")
                }
            }

            override fun onFailure(code: Int, msg: String?) {
                errorInfoData.value= ErrorInfo(false,code,msg)
            }

        })
    }

    private fun queryChatRoomInfo(liveInfo: LiveInfo){
        //todo queryChatRoomInfo
//        LiveRoomService.sharedInstance().queryChatRoomInfo(liveInfo.live.chatRoomId,object : NetRequestCallback<ChatRoomInfo> {
//            override fun success(info: ChatRoomInfo?) {
//                info?.let {
//                    var audienceCount=info.onlineUserCount-1
//                    if (audienceCount<0){
//                        audienceCount=0
//                    }
//                    userCountData.value = audienceCount
//                    data?.userCount=audienceCount
//                    saveCache()
//                    FloatPlayLogUtil.log(TAG, "queryChatRoomInfo audienceCount:$audienceCount")
//                }
//            }
//
//            override fun error(code: Int, msg: String) {
//                FloatPlayLogUtil.log(TAG, "queryChatRoomInfo error,code:$code,msg:$msg")
//            }
//
//        })
    }

    fun saveCache() {
        data?.let {
            AudienceDataManager.setDataToCache(data!!)
        }
    }

    fun release(){
        liveKitManager.removeLiveListener(liveListener)
    }
}