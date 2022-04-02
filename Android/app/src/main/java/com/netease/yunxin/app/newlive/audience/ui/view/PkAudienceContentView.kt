/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.audience.ui.view

import android.annotation.SuppressLint
import android.text.TextUtils
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.app.newlive.audience.utils.AudiencePKControl
import com.netease.yunxin.app.newlive.activity.BaseActivity
import com.netease.yunxin.app.newlive.floatplay.AudienceData
import com.netease.yunxin.app.newlive.floatplay.CDNStreamTextureView
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.live.MyLiveListener
import com.netease.yunxin.app.newlive.widget.PKControlView
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.livekit.LiveTypeManager
import com.netease.yunxin.kit.livekit.NELiveCallback
import com.netease.yunxin.kit.livekit.NELiveConstants
import com.netease.yunxin.kit.livekit.model.RewardMsg
import com.netease.yunxin.kit.livekit.model.pk.*
import com.netease.yunxin.kit.livekit.model.reward.RewardAudience


@SuppressLint("ViewConstructor")
class PkAudienceContentView(activity: BaseActivity) : BaseAudienceContentView(activity) {

    companion object{
        const val LOG_TAG = "PkAudienceContentView"
    }

    var isPking = false

    /**
     * pk 状态整体控制
     */
    private var audiencePKControl: AudiencePKControl? = null

    /**
     * 是否是被邀请方
     */
    private var isInvited = false

    private val liveListener = object : MyLiveListener(){
        override fun onPKStart(startInfo: PkStartInfo) {
            val otherAnchor: NEPKUser =
                if (TextUtils.equals(audienceViewModel?.data!!.liveInfo?.anchor?.userUuid, startInfo.invitee.userUuid)) {
                    isInvited = true
                    startInfo.inviter
                } else {
                    isInvited = false
                    startInfo.invitee
                }
            isPking = true
            getAudiencePKControl().onPkStart(otherAnchor,startInfo.pkCountDown)
        }

        override fun onPKPunishStart(punishInfo: PkPunishInfo) {
            isPking = false
            val pkResult = if(punishInfo.inviteeRewards == punishInfo.inviterRewards){
                PKControlView.PK_RESULT_DRAW
            }else if (punishInfo.inviteeRewards > punishInfo.inviterRewards){
                if(isInvited) {
                    PKControlView.PK_RESULT_SUCCESS
                }else{
                    PKControlView.PK_RESULT_FAILED
                }
            }else {
                if(isInvited) {
                    PKControlView.PK_RESULT_FAILED
                }else{
                    PKControlView.PK_RESULT_SUCCESS
                }
            }
            getAudiencePKControl().onPunishmentStart(null, pkResult, punishInfo.pkPenaltyCountDown)
        }

        override fun onPKEnd(endInfo: PkEndInfo) {
            getAudiencePKControl().onPkEnd()
            isPking = false
        }

    }

    override fun onUserRewardImpl(rewardInfo: RewardMsg) {
        super.onUserRewardImpl(rewardInfo)
        if (isPking) {
            when {
                TextUtils.equals(
                    rewardInfo.anchorReward.userUuid,
                    audienceViewModel?.data!!.liveInfo?.anchor?.userUuid
                ) -> {
                    getAudiencePKControl().onAnchorCoinChanged(
                        rewardInfo.anchorReward.pkRewardTotal,
                        rewardInfo.otherAnchorReward!!.pkRewardTotal,
                        rewardInfo.anchorReward.pkRewardTop,
                        rewardInfo.otherAnchorReward!!.pkRewardTop
                    )
                }
                TextUtils.equals(
                    rewardInfo.otherAnchorReward!!.userUuid,
                    audienceViewModel?.data!!.liveInfo?.anchor?.userUuid
                ) -> {
                    getAudiencePKControl().onAnchorCoinChanged(
                        rewardInfo.otherAnchorReward!!.pkRewardTotal,
                        rewardInfo.anchorReward.pkRewardTotal,
                        rewardInfo.otherAnchorReward!!.pkRewardTop,
                        rewardInfo.anchorReward.pkRewardTop
                    )
                }
                else -> {
                    ALog.e(LOG_TAG, "reward is not for this live room")
                }
            }
        }
    }

    override fun initLiveType(isRetry: Boolean) {
        super.initLiveType(isRetry)
        LiveKitManager.getInstance().addLiveListener(liveListener)
        if (audienceViewModel?.data!!.liveInfo?.live?.live == NELiveConstants.LiveStatus.LIVE_STATUS_ON_PUNISHMENT
            || audienceViewModel?.data!!.liveInfo?.live?.live == NELiveConstants.LiveStatus.LIVE_STATUS_PKING
        ) {
            liveKit.requestPKInfo(audienceViewModel?.data!!.liveInfo?.live?.liveRecordId?:0, object : NELiveCallback<PkInfo> {
                override fun onSuccess(info: PkInfo?) {
                    info?.let {
                        val selfAnchor: NEPKUser
                        val otherAnchor: NEPKUser
                        val selfReward: PkReward
                        val otherReward: PkReward
                        if (TextUtils.equals(audienceViewModel?.data!!.liveInfo?.anchor?.userUuid, it.invitee.userUuid)) {
                            isInvited = true
                            selfAnchor = it.invitee
                            selfReward = it.inviteeReward
                            otherAnchor = it.inviter
                            otherReward = it.inviterReward
                        } else {
                            isInvited = false
                            selfAnchor = it.inviter
                            selfReward = it.inviterReward
                            otherAnchor = it.invitee
                            otherReward = it.inviteeReward
                        }
                        when (info.state) {
                            NELiveConstants.PkStatus.PK_STATUS_PKING -> {
                                isPking = true
                                getAudiencePKControl().onPkStart(otherAnchor, it.countDown, false)
                            }
                            NELiveConstants.PkStatus.PK_STATUS_PUNISHMENT -> {
                                val pkResult = when {
                                    selfAnchor.rewardTotal == otherAnchor.rewardTotal -> {
                                        PKControlView.PK_RESULT_DRAW
                                    }
                                    selfAnchor.rewardTotal > otherAnchor.rewardTotal -> {
                                        PKControlView.PK_RESULT_SUCCESS
                                    }
                                    else -> {
                                        PKControlView.PK_RESULT_FAILED
                                    }
                                }
                                getAudiencePKControl().onPunishmentStart(
                                    otherAnchor,
                                    pkResult,
                                    it.countDown,
                                    true
                                )

                            }
                        }
                        getAudiencePKControl().onAnchorCoinChanged(
                            selfReward.rewardCoinTotal, otherReward.rewardCoinTotal,
                            transferOfAudienceList(selfReward.rewardTop),
                            transferOfAudienceList(otherReward.rewardTop)
                        )
                    }
                    // 基于直播类型，来调整播放内容样式.
                    if (isPking){
                        LiveTypeManager.setCurrentLiveType(NELiveConstants.LiveType.LIVE_TYPE_PK)
                    }else{
                        LiveTypeManager.setCurrentLiveType(NELiveConstants.LiveType.LIVE_TYPE_DEFAULT)
                    }
                }

                override fun onFailure(code: Int, msg: String?) {
                    if (code != NELiveConstants.ErrorCode.CODE_NO_PK) {
                        ToastUtils.showLong(msg)
                    }
                }
            })
        }
    }

    /**
     * 观众列表数据结构转换
     */
    private fun transferOfAudienceList(audiences: List<PkRewardAudience>): List<RewardAudience> {
        val audienceList = ArrayList<RewardAudience>(audiences.size)
        for (audience in audiences) {
            audienceList.add(
                RewardAudience(
                    audience.userUuid,
                    "",
                    audience.rewardCoin
                )
            )
        }
        return audienceList
    }

    private fun getAudiencePKControl(): AudiencePKControl {
        if (audiencePKControl == null) {
            audiencePKControl = AudiencePKControl()
            audiencePKControl!!.init(activity, videoView, infoBinding.root)
        }
        return audiencePKControl!!
    }

    override fun release() {
        super.release()
        audiencePKControl?.release()
        LiveKitManager.getInstance().removeLiveListener(liveListener)
    }

    override fun adjustVideoSize(data: AudienceData) {
        // 现有的方案描述：PK蒙层与视频画面大小变更存在时间差，画面是CDN流，延迟2-5s，蒙层由透传消息触发。
        // 以下代码是解决小窗切换到大窗的瞬间直播状态发生变化导致PK蒙层与视频画面不匹配问题
        if (LiveTypeManager.getCurrentLiveType()== NELiveConstants.LiveType.LIVE_TYPE_DEFAULT
            && CDNStreamTextureView.isSingleAnchorSize(data.videoInfo?.videoWidth!!,data.videoInfo?.videoHeight!!)) {
            videoView?.adjustVideoSizeForNormal()
            ALog.d(LOG_TAG,"adjustVideoSizeForNormal")
        }else if (LiveTypeManager.getCurrentLiveType()== NELiveConstants.LiveType.LIVE_TYPE_PK
            && CDNStreamTextureView.isPkSize(data.videoInfo?.videoWidth!!,data.videoInfo?.videoHeight!!)){
            videoView?.adjustVideoSizeForPk(false)
            ALog.d(LOG_TAG,"adjustVideoSizeForPk")
        }else{
            // 继续走现有方案，与当前进直播间逻辑保持同步
            ALog.d(LOG_TAG,"adjust video canvas by onVideoSizeChanged callback")
        }
    }
}