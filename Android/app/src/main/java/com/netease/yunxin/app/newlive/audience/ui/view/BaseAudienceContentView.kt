/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.audience.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.NetworkUtils.NetworkType
import com.blankj.utilcode.util.NetworkUtils.OnNetworkStatusChangedListener
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.app.newlive.audience.ui.dialog.GiftDialog
import com.netease.yunxin.app.newlive.audience.ui.dialog.GiftDialog.GiftSendListener
import com.netease.yunxin.app.newlive.audience.ui.view.AudienceErrorStateView.ClickButtonListener
import com.netease.yunxin.app.newlive.audience.utils.DialogHelperActivity
import com.netease.yunxin.app.newlive.audience.utils.InputUtils
import com.netease.yunxin.app.newlive.audience.utils.InputUtils.InputParamHelper
import com.netease.yunxin.app.newlive.audience.utils.StringUtils
import com.netease.biz_live.yunxin.live.gift.ui.GifAnimationView
import com.netease.yunxin.android.lib.picture.ImageLoader
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.app.newlive.activity.BaseActivity
import com.netease.yunxin.app.newlive.chatroom.ChatRoomMsgCreator
import com.netease.yunxin.app.newlive.config.StatusBarConfig
import com.netease.yunxin.app.newlive.databinding.ViewIncludeRoomTopBinding
import com.netease.yunxin.app.newlive.databinding.ViewItemAudienceLiveRoomInfoBinding
import com.netease.yunxin.app.newlive.floatplay.*
import com.netease.yunxin.app.newlive.gift.GiftCache
import com.netease.yunxin.app.newlive.gift.GiftRender
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.utils.SpUtils
import com.netease.yunxin.app.newlive.utils.ViewUtils
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.livekit.NELiveCallback
import com.netease.yunxin.kit.livekit.NELiveConstants
import com.netease.yunxin.kit.livekit.RoomInfo
import com.netease.yunxin.kit.livekit.model.ErrorInfo
import com.netease.yunxin.kit.livekit.model.LiveInfo
import com.netease.yunxin.kit.livekit.model.RewardMsg
import com.netease.yunxin.kit.login.AuthorManager
import com.netease.yunxin.kit.roomkit.api.NERoomMember

/**
 * Created by luc on 2020/11/19.
 *
 *
 * ?????????????????????????????????[FrameLayout] ????????? [TextureView] ?????? [ExtraTransparentView] ????????????????????????
 *
 *
 * TextureView ???????????????????????????
 *
 *
 * ExtraTransparentView ??????????????????????????????????????????????????????????????????????????????view ????????? [RecyclerView] ?????????????????????????????????
 * // * ????????????????????? R.layout.view_item_audience_live_room_info
 *
 *
 *
 * ?????? [.prepare] ???????????????recyclerView ??? view ??? [androidx.recyclerview.widget.RecyclerView.onChildAttachedToWindow],
 * [androidx.recyclerview.widget.RecyclerView.onChildDetachedFromWindow] ?????????
 * ?????????[.renderData] ????????? [androidx.recyclerview.widget.RecyclerView.Adapter.onBindViewHolder]
 * ???????????? [androidx.recyclerview.widget.LinearLayoutManager] ??????????????????????????? renderData ?????????????????? prepare ?????????
 *
 */
@SuppressLint("ViewConstructor")
abstract class BaseAudienceContentView(val activity: BaseActivity) : FrameLayout(activity) {



//    protected val roomService by lazy { LiveRoomService.sharedInstance() }

    protected val liveKit by lazy { LiveKitManager.getInstance().liveKit }


    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    private val giftRender: GiftRender = GiftRender()

    /**
     * ????????????View
     */
    protected var videoView: CDNStreamTextureView? = null

    /**
     * ????????????????????????
     */
    private var horSwitchView: ExtraTransparentView? = null

    /**
     * ????????????????????????viewbinding ????????????:https://developer.android.com/topic/libraries/view-bindinghl=zh-cn#java
     */
    protected val infoBinding by lazy {
        ViewItemAudienceLiveRoomInfoBinding.inflate(
            LayoutInflater.from(
                context
            ), this, false
        )
    }

    private val includeRoomTopBinding by lazy { ViewIncludeRoomTopBinding.bind(infoBinding.root) }

    /**
     * ????????????????????????????????????????????????
     */
    protected var errorStateView: AudienceErrorStateView? = null

    /**
     * ????????????
     */
    private var giftDialog: GiftDialog? = null

    private var joinRoomSuccess = false

    var audienceViewModel: AudienceViewModel? = null
    var roomDestroyed=false
    /**
     * ??????????????????
     */
    private val onNetworkStatusChangedListener: OnNetworkStatusChangedListener =
        object : OnNetworkStatusChangedListener {
            override fun onDisconnected() {
                onNetworkDisconnected()
            }

            override fun onConnected(networkType: NetworkType) {
                onNetworkConnected(networkType)
            }
        }

    protected open fun onNetworkDisconnected() {
        ToastUtils.showLong(R.string.biz_live_network_error)
        ALog.d(LOG_TAG, "onDisconnected():" + System.currentTimeMillis())
        changeErrorState(true, AudienceErrorStateView.TYPE_ERROR)
        if (giftDialog?.isShowing == true) {
            giftDialog?.dismiss()
        }
    }

    protected open fun onNetworkConnected(networkType: NetworkType) {
        ALog.d(LOG_TAG, "onConnected():" + System.currentTimeMillis())
    }

    protected open fun showCdnView() {
        changeErrorState(false, -1)
        videoView?.visibility = VISIBLE
        // ???????????????????????????
        horSwitchView?.toSelectedPosition()
        // ?????????????????????????????????????????????
        infoBinding.crvMsgList.toLatestMsg()
        errorStateView?.visibility = GONE
    }

    open fun onUserRewardImpl(rewardInfo: RewardMsg) {
        if (TextUtils.equals(
                rewardInfo.anchorReward.userUuid,
                audienceViewModel?.data!!.liveInfo?.anchor?.userUuid
            )
        ) {
            audienceViewModel?.data?.liveInfo?.live?.rewardTotal = rewardInfo.anchorReward.rewardTotal
            refreshCoinCount(StringUtils.getCoinCount(rewardInfo.anchorReward.rewardTotal))
            giftRender.addGift(GiftCache.getGift(rewardInfo.giftId).dynamicIconResId)
        }
    }

    private fun refreshCoinCount(coinCount: String?) {
        coinCount?.let {
            includeRoomTopBinding.tvAnchorCoinCount.text =coinCount
        }
    }

    fun onMsgArrived(msg: CharSequence?) {
        infoBinding.crvMsgList.appendItem(msg)
    }


    /**
     * ??????????????????????????????
     */
    private val clickButtonListener: ClickButtonListener = object : ClickButtonListener {
        override fun onBackClick(view: View?) {
            ALog.d(LOG_TAG, "onBackClick")
            finishLiveRoomActivity(true)
        }

        override fun onRetryClick(view: View?) {
            ALog.d(LOG_TAG, "onRetryClick")
            if (!NetworkUtils.isConnected()){
                ALog.d(LOG_TAG,"onRetryClick failed")
                return
            }
            if ( audienceViewModel?.data!!.liveInfo != null) {
                if (joinRoomSuccess) {
                    initLiveType(true)
                    select(audienceViewModel?.data!!.liveInfo!!)
                }
            }
        }
    }

    /**
     * ??????????????????????????? view
     */
    fun initViews() {
        ALog.d(LOG_TAG,"initViews()")
        // ?????? view ????????????
        setBackgroundColor(Color.parseColor("#ff201C23"))
        // ?????????????????? TextureView
        videoView = CDNStreamTextureView(context)
        addView(videoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        horSwitchView = ExtraTransparentView(context, infoBinding.root)
        // ???????????????????????????????????????????????????
        horSwitchView?.registerSelectedRunnable { infoBinding.crvMsgList.toLatestMsg() }
        addView(
            horSwitchView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        // ???????????????????????? status bar ?????????????????????
        StatusBarConfig.paddingStatusBarHeight(activity, horSwitchView)

        // ????????????????????????
        errorStateView = AudienceErrorStateView(context)
        addView(errorStateView)
        errorStateView?.visibility = GONE

        // ????????????????????????
        // ?????????????????? view
        val gifAnimationView = GifAnimationView(context)
        val size = SpUtils.getScreenWidth()
        val layoutParams = generateDefaultLayoutParams()
        layoutParams.width = size
        layoutParams.height = size
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.bottomMargin = SpUtils.dp2pix(166f)
        addView(gifAnimationView, layoutParams)
        gifAnimationView.bringToFront()
        // ?????????????????? view
        giftRender.init(gifAnimationView)

        // ?????????????????????
        activity.let {
            InputUtils.registerSoftInputListener(it, object : InputParamHelper {
                override fun getHeight(): Int {
                    return this@BaseAudienceContentView.height
                }

                override fun getInputView(): EditText {
                    return infoBinding.etRoomMsgInput
                }
            })
        }

        infoBinding.apply {
            // ????????????
            ivRoomClose.setOnClickListener {
                closeBtnClick()
            }
            // ????????????
            ivRoomGift.setOnClickListener {
                if (giftDialog == null) {
                    giftDialog = GiftDialog(activity)
                }
                giftDialog!!.show(object : GiftSendListener {
                    override fun onSendGift(giftId: Int?) {
                        giftId?.let {
                            liveKit.reward(it, object : NELiveCallback<Unit> {
                                override fun onSuccess(t: Unit?) {
                                }

                                override fun onFailure(code: Int, msg: String?) {
                                    ToastUtils.showShort(R.string.biz_live_reward_failed)
                                }

                            })
                        }
                    }
                })
            }

            // ?????????????????????
            tvRoomMsgInput.setOnClickListener {
                InputUtils.showSoftInput(
                    infoBinding.etRoomMsgInput
                )
            }

            // ???????????????
            etRoomMsgInput.setOnEditorActionListener(TextView.OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (v === etRoomMsgInput) {
                    var liveInfo = audienceViewModel?.data!!.liveInfo
                    ALog.d(LOG_TAG,"audienceViewModel:"+audienceViewModel?.data!!.liveInfo.toString())
                    val input = etRoomMsgInput.text.toString()
                    InputUtils.hideSoftInput(etRoomMsgInput)
                    liveKit.sendMessage(input, object : NELiveCallback<Unit>{
                        override fun onSuccess(t: Unit?) {

                        }

                        override fun onFailure(code: Int, msg: String?) {

                        }
                    })
                    audienceViewModel?.appendChatRoomMsg(
                        ChatRoomMsgCreator.createText(
                            false,
                            AuthorManager.getUserInfo()?.accountId,
                            input
                        )
                    )
                    return@OnEditorActionListener true
                }
                false
            })
        }

    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param info ???????????????
     */
    open fun renderData(info: LiveInfo) {




    }

    /**
     * ??????????????????
     */
    fun prepare() {
        showCdnView()
    }

    var roomId=""
    /**
     * ????????????
     */
    fun select(liveInfo: LiveInfo) {
        roomId=liveInfo.live.roomUuid
        ALog.d(LOG_TAG,"select(),roomId:$roomId")
        audienceViewModel = ViewModelProvider(activity).get(AudienceViewModel::class.java)
        subscribeUI()
        if (!joinRoomSuccess&&!AudienceDataManager.hasCache(roomId)){

            liveKit.requestLiveInfo(liveInfo.live.liveRecordId, object : NELiveCallback<LiveInfo>{
                override fun onSuccess(newInfo: LiveInfo) {
                    ALog.d(LOG_TAG, "requestLiveInfo success newInfo = $newInfo")
                    // ?????????????????????????????????????????????
                    newInfo.let {
                        audienceViewModel?.refreshLiveInfo(newInfo)
                    }
                    liveKit.joinLive(newInfo, object : NELiveCallback<RoomInfo> {
                        override fun onSuccess(info: RoomInfo?) {
                            ALog.d(LOG_TAG, "audience join room success,roomId:$roomId")
                            joinRoomSuccess = true

                            if (!roomDestroyed){
                                initLiveType(false)
                            }
                        }

                        override fun onFailure(code: Int, msg: String?) {
                            ALog.e(LOG_TAG, "join room failed msg:$msg code= $code")
                            if(code == NELiveConstants.ErrorCode.LIVE_NOT_EXIST){
                                audienceViewModel?.apply {
                                    errorStateData.value = Pair(true, AudienceErrorStateView.TYPE_FINISHED)
                                }
                            }else {
                                ToastUtils.showShort(R.string.biz_live_network_error_try_again)
                                // ???????????????????????????????????????????????????
                                finishLiveRoomActivity(true)
                            }
                        }
                    })
                }

                override fun onFailure(code: Int, msg: String?) {
                    ALog.e(LOG_TAG, "requestLiveInfo failed msg:$msg code= $code")
                    if(code == NELiveConstants.ErrorCode.LIVE_NOT_EXIST){
                        audienceViewModel?.apply {
                            errorStateData.value = Pair(true, AudienceErrorStateView.TYPE_FINISHED)
                        }
                    }else {
                        ToastUtils.showShort(R.string.biz_live_network_error_try_again)
                        // ???????????????????????????????????????????????????
                        finishLiveRoomActivity(true)
                    }
                }
            })

        }else{
            audienceViewModel?.queryRoomDetailInfo(liveInfo)
            if (!roomDestroyed){
                initLiveType(false)
            }
        }
        audienceViewModel?.select(liveInfo)
        videoView?.prepare(audienceViewModel?.data?.liveInfo)
    }

    fun saveListInfoAndPosition(infoList: MutableList<LiveInfo>, currentPosition: Int) {
        audienceViewModel?.saveListInfoAndPosition(infoList, currentPosition)
    }

    private val cacheObserver=Observer<AudienceData>{
            if (!needRefresh()){
                return@Observer
            }
            it?.let {
                ALog.d(LOG_TAG, "cacheObserver22:$it")
                refreshBasicUI(it.liveInfo)
                refreshAudienceCount(it.userCount)
                infoBinding.crvMsgList.appendItems(it.chatRoomMsgList as MutableList<CharSequence?>)
                refreshAudienceCount(it.userCount)
                refreshCoinCount(StringUtils.getCoinCount(it.rewardTotal))
                refreshUserList(it.userList)
                adjustVideoSize(it)
            }
    }

    open fun adjustVideoSize(data:AudienceData){

    }

    private val liveInfoObserver = Observer<LiveInfo> {
        if (!needRefresh()){
            return@Observer
        }
        ALog.d(LOG_TAG, "liveInfoObserver111,roomId:$roomId")
        ALog.d(LOG_TAG, "liveInfoObserver222,roomId:${it.live.roomUuid}")
        ALog.d(LOG_TAG, "liveInfoObserver444,anchor:${it.anchor.userName}")
        ALog.d(LOG_TAG, "liveInfoObserver444,audienceCount:${it.live.audienceCount?:0}")
        refreshBasicUI(it)
    }

    private val errorInfoObserver = Observer<ErrorInfo> {
        if (!needRefresh()){
            return@Observer
        }
        ALog.d(LOG_TAG, "onError $it")
        if (it.serious) {
            finishLiveRoomActivity(true)
        } else {
            if (!TextUtils.isEmpty(it.msg)) {
                ToastUtils.showShort(it.msg)
            }
        }
    }

    private val errorStateObserver = Observer<Pair<Boolean, Int>> {
        if (!needRefresh()){
            return@Observer
        }
        roomDestroyed=it.first
        ALog.d(LOG_TAG,"roomDestroyed:$roomDestroyed")
        changeErrorState(it.first, it.second)
    }

    private val userCountObserver = Observer<Int> {
        if (!needRefresh()){
            return@Observer
        }
        refreshAudienceCount(it)

    }

    private val newChatRoomMsgObserver = Observer<CharSequence> {
        if (!needRefresh()){
            return@Observer
        }
        onMsgArrived(it)
    }
    private val kickedOutObserver = Observer<Boolean> {
        if (!needRefresh()){
            return@Observer
        }
        if (it) {
            finishLiveRoomActivity(true)
            context.startActivity(Intent(context, DialogHelperActivity::class.java))
        }
    }
    private val userRewardObserver = Observer<RewardMsg> {
        if (!needRefresh()){
            return@Observer
        }
        onUserRewardImpl(it)
    }
    private val videoHeightObserver = Observer<Int> {
        if (!needRefresh()){
            return@Observer
        }

    }
    private val userListObserver = Observer<MutableList<NERoomMember>> {
        if (!needRefresh()){
            return@Observer
        }
        refreshUserList(it)
    }

    private fun refreshUserList(userList: List<NERoomMember>?) {
        userList?.let {
            includeRoomTopBinding.rvAnchorPortraitList.updateAll(userList)
        }
    }

    private fun refreshAudienceCount(count: Int) {
        includeRoomTopBinding.tvAudienceCount.text = StringUtils.getAudienceCount(count + 1)
    }

    private fun refreshBasicUI(liveInfo: LiveInfo?) {
        liveInfo.let {
            errorStateView?.renderInfo(liveInfo?.anchor?.avatar, liveInfo?.anchor?.userName)
            // ????????????
            ImageLoader.with(context.applicationContext)
                .circleLoad(liveInfo?.anchor?.avatar, includeRoomTopBinding.ivAnchorPortrait)
            // ????????????
            includeRoomTopBinding.tvAnchorNickname.text = liveInfo?.anchor?.userUuid
            includeRoomTopBinding.tvAnchorCoinCount.text =
                StringUtils.getCoinCount(liveInfo?.live?.rewardTotal!!)
            refreshAudienceCount(liveInfo.live.audienceCount?:0)
        }
    }


    private fun subscribeUI() {
        ALog.d(LOG_TAG, "subscribeUI:$audienceViewModel")
        audienceViewModel?.apply {
            cacheData.observe(activity,cacheObserver)
            liveInfoData.observe(activity,liveInfoObserver)
            errorInfoData.observe(activity, errorInfoObserver)
            errorStateData.observe(activity, errorStateObserver)
            userCountData.observe(activity, userCountObserver)
            newChatRoomMsgData.observe(activity, newChatRoomMsgObserver)
            kickedOutData.observe(activity, kickedOutObserver)
            userRewardData.observe(activity, userRewardObserver)
            videoHeightData.observe(activity, videoHeightObserver)
            userListData.observe(activity, userListObserver)
        }

    }

    protected open fun initLiveType(isRetry: Boolean) {
        if (isRetry) {
            showCdnView()
            FloatPlayLogUtil.log(LOG_TAG, "initLiveType,showCdnView")
        }
        changeErrorState(false, -1)
    }


    /**
     * ??????????????????
     */
    open fun release() {
        roomId=""
        unSubscribeUI()
        ALog.d(LOG_TAG,"leaveRoom")
        audienceViewModel?.release()
        liveKit.leaveLive(object : NELiveCallback<Unit> {
            override fun onSuccess(t: Unit?) {
                ALog.d(LOG_TAG,"leaveRoom success")
            }

            override fun onFailure(code: Int, msg: String?) {
                ALog.d(LOG_TAG,"leaveRoom error")
            }
        })
        // ??????????????????
        giftRender.release()
        // ??????????????????
        infoBinding.crvMsgList.clearAllInfo()
        joinRoomSuccess = false
    }

    private fun unSubscribeUI() {
        audienceViewModel?.apply {
            cacheData.removeObserver(cacheObserver)
            liveInfoData.removeObserver(liveInfoObserver)
            errorInfoData.removeObserver(errorInfoObserver)
            errorStateData.removeObserver(errorStateObserver)
            userCountData.removeObserver(userCountObserver)
            newChatRoomMsgData.removeObserver(newChatRoomMsgObserver)
            kickedOutData.removeObserver(kickedOutObserver)
            userRewardData.removeObserver(userRewardObserver)
            videoHeightData.removeObserver(videoHeightObserver)
            userListData.removeObserver(userListObserver)
        }
    }

    protected open fun changeErrorState(error: Boolean, type: Int) {
        FloatPlayLogUtil.log(LOG_TAG, "changeErrorState,error:$error,type:$type")
        if (error) {
            videoView?.visibility = GONE
            infoBinding.groupNormal.visibility= GONE
            errorStateView?.visibility= VISIBLE
            errorStateView?.updateType(type, clickButtonListener)
            if (type== AudienceErrorStateView.TYPE_FINISHED){
                release()
            }
        }else{
            roomDestroyed=false
            errorStateView?.visibility= GONE
            if (!roomDestroyed&&audienceViewModel?.data?.liveInfo!=null){
                infoBinding.groupNormal.visibility= VISIBLE
            }else{
                infoBinding.groupNormal.visibility= GONE
            }
        }
        if (roomDestroyed){
            InputUtils.hideSoftInput(infoBinding.etRoomMsgInput)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.rawX.toInt()
        val y = ev.rawY.toInt()
        // ?????????????????????????????????
        if (!ViewUtils.isInView(infoBinding.etRoomMsgInput, x, y)) {
            InputUtils.hideSoftInput(infoBinding.etRoomMsgInput)
        }
        return super.dispatchTouchEvent(ev)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        NetworkUtils.registerNetworkStatusChangedListener(onNetworkStatusChangedListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        NetworkUtils.unregisterNetworkStatusChangedListener(onNetworkStatusChangedListener)
    }


    fun finishLiveRoomActivity(needRelease: Boolean) {
        if (needRelease) {
            release()
        }
        if (!activity.isFinishing) {
            activity.finish()
        }
    }

    private fun needRefresh():Boolean{
        val needRefresh=!TextUtils.isEmpty(roomId)&& roomId == audienceViewModel?.data?.liveInfo?.live?.roomUuid
        ALog.d(LOG_TAG,"needRefreshRoom,needRefresh:$needRefresh")
        return needRefresh
    }

    open fun closeBtnClick(){
        // ???????????????????????????
        val dialog=AudienceBottomTipsDialog()
        dialog.show(activity.supportFragmentManager, LOG_TAG)
        dialog.setClickCallback(object :AudienceBottomTipsDialog.OnClickCallback{
            override fun minimize() {
                if (FloatWindowPermissionManager.isFloatWindowOpAllowed(activity)) {
                    FloatPlayManager.startFloatPlay(
                        activity, roomId
                    )
                    finishLiveRoomActivity(false)
                } else {
                    FloatWindowPermissionManager.requestFloatWindowPermission(activity)
                }
            }

            override fun exit() {
                finishLiveRoomActivity(true)
            }

        })
    }

    companion object {
        const val LOG_TAG = "BaseAudienceContentView"
    }

    init {
        initViews()
    }
}