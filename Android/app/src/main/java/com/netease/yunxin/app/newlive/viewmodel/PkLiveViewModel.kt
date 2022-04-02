/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.viewmodel

import android.os.CountDownTimer
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.live.MyLiveListener
import com.netease.yunxin.kit.livekit.model.pk.*
import com.netease.yunxin.kit.login.AuthorManager

/**
 * viewModel for [AnchorPkLiveActivity]
 */
class PkLiveViewModel : ViewModel() {

    companion object {
        const val TAG = "PkLiveViewModel"
        const val PK_STATE_IDLE = 0
        const val PK_STATE_REQUEST = 1
        const val PK_STATE_AGREED = 2
        const val PK_STATE_PKING = 3
        const val PK_STATE_PUNISH = 4
    }

    var pkState = 0

    private var inviteTimerTask: PkCountTimeTask? = null

    private var agreeTimerTask: PkCountTimeTask? = null

    var currentPkConfig: PkConfigInfo? = null

    /**
     * the pk request you have received
     */
    val pkActionData = MutableLiveData<PkActionMsg?>()

    val pkStartData = MutableLiveData<PkStartInfo?>()

    val punishData = MutableLiveData<PkPunishInfo?>()

    /**
     * Count down timeOut data
     */
    val countDownTimeOutData = MutableLiveData<Boolean>()

    val pkEndData = MutableLiveData<PkEndInfo?>()

    val pkOtherAnchorJoinedData = MutableLiveData<String>()

    var otherAnchorUuid :String? = null

    private var liveListener  = object : MyLiveListener() {
        override fun onPKInvited(pkUser: PkActionMsg) {
            pkState = PK_STATE_REQUEST
            pkActionData.postValue(pkUser)
        }

        override fun onPKRejected(pkUser: PkActionMsg) {
            pkState = PK_STATE_IDLE
            pkActionData.postValue(pkUser)
        }

        override fun onPKCanceled(pkUser: PkActionMsg) {
            pkState = PK_STATE_IDLE
            pkActionData.postValue(pkUser)
        }

        override fun onPKAccepted(pkUser: PkActionMsg) {
            pkState = PK_STATE_AGREED
            pkActionData.postValue(pkUser)
        }

        override fun onPKTimeoutCanceled(pkUser: PkActionMsg) {
            pkState = PK_STATE_IDLE
            currentPkConfig = null
            pkActionData.postValue(pkUser)
        }

        override fun onPKStart(startInfo: PkStartInfo) {
            pkState = PK_STATE_PKING
            pkStartData.postValue(startInfo)

            otherAnchorUuid = if(isSelf(startInfo.invitee.userUuid)) startInfo.inviter.userUuid else startInfo.invitee.userUuid
            pkOtherAnchorJoinedData.postValue(otherAnchorUuid)
        }

        override fun onPKPunishStart(punishInfo: PkPunishInfo) {
            pkState = PK_STATE_PUNISH
            punishData.postValue(punishInfo)
        }

        override fun onPKEnd(endInfo: PkEndInfo) {
            pkState = PK_STATE_IDLE
            currentPkConfig = null
            pkEndData.postValue(endInfo)
        }

    }

    /**
     * Stat invite count timer
     *
     * @param leftTime second
     */
    fun startInviteCountTimer(leftTime: Int?, newPkId: String?) {
        leftTime?.let {
            inviteTimerTask?.cancel()
            inviteTimerTask = object : PkCountTimeTask(newPkId, it * 1000L) {
                /**
                 * Callback fired on regular interval.
                 * @param millisUntilFinished The amount of time until finished.
                 */
                override fun onTick(millisUntilFinished: Long) {
                    currentPkConfig?.let { config ->
                        if (pkState != PK_STATE_REQUEST && TextUtils.equals(pkId, config.pkId)) {
                            cancel()
                        }
                    }
                }

                /**
                 * Callback fired when the time is up.
                 */
                override fun onFinish() {
                    currentPkConfig?.let { config ->
                        if (pkState == PK_STATE_REQUEST && TextUtils.equals(pkId, config.pkId)) {
                            pkState = PK_STATE_IDLE
                            countDownTimeOutData.postValue(true)
                        }
                    }
                }
            }
            inviteTimerTask?.start()
        }
    }

    /**
     * Start agree count timer
     *
     * @param leftTime second
     */
    fun startAgreeCountTimer(leftTime: Int?, newPkId: String?) {
        inviteTimerTask?.let {
            if (TextUtils.equals(it.pkId, newPkId)) {
                it.cancel()
            }
        }
        leftTime?.let {
            agreeTimerTask?.cancel()
            agreeTimerTask = object : PkCountTimeTask(newPkId, it * 1000L) {
                /**
                 * Callback fired on regular interval.
                 * @param millisUntilFinished The amount of time until finished.
                 */
                override fun onTick(millisUntilFinished: Long) {
                    currentPkConfig?.let { config ->
                        if (pkState != PK_STATE_AGREED && TextUtils.equals(pkId, config.pkId)) {
                            cancel()
                        }
                    }

                }

                /**
                 * Callback fired when the time is up.
                 */
                override fun onFinish() {
                    currentPkConfig?.let { config ->
                        if (pkState == PK_STATE_AGREED && TextUtils.equals(pkId, config.pkId)) {
                            pkState = PK_STATE_IDLE
                            countDownTimeOutData.postValue(true)
                        }
                    }
                }
            }
            agreeTimerTask?.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        inviteTimerTask?.cancel()
        agreeTimerTask?.cancel()
        inviteTimerTask = null
        agreeTimerTask = null
    }

    fun init() {
        LiveKitManager.getInstance().addLiveListener(liveListener)
        pkActionData.value = null
        pkEndData.value = null
        pkStartData.value = null
        punishData.value = null
        pkOtherAnchorJoinedData.value = null
    }

    fun destroy(){
        LiveKitManager.getInstance().removeLiveListener(liveListener)
    }

    abstract class PkCountTimeTask(
        val pkId: String?,
        val millisInFuture: Long,
        val countDownInterval: Long = COUNT_DOWN_INTERVAL
    ) :
        CountDownTimer(millisInFuture, countDownInterval) {

        companion object {
            const val COUNT_DOWN_INTERVAL = 1000L
        }

    }

    data class PkConfigInfo(
        val pkId: String?,
        val agreeTaskTime: Int?,
        val inviteTaskTime: Int?
    )

    fun isSelf(uuid: String): Boolean{
        return AuthorManager.getUserInfo()?.accountId == uuid
    }
}