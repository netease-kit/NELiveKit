/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.cohost.dialog

import CoHostListAdapter
import android.app.Activity
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.kit.common.ui.utils.ToastX
import com.netease.yunxin.kit.common.utils.XKitUtils
import com.netease.yunxin.kit.entertainment.common.dialog.ChoiceDialog
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit.Companion.getInstance
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.manager.NECoHostListener
import com.netease.yunxin.kit.livestreamkit.impl.model.ConnectionUser
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomLog
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIConstants
import com.netease.yunxin.kit.livestreamkit.ui.R
import com.netease.yunxin.kit.livestreamkit.ui.dialog.BottomBaseDialog

/** 主播端底部更多弹窗  */
class CoHostInviteDialog(activity: Activity) : BottomBaseDialog(activity) {

    companion object {
        const val TAG = "CoHostInviteDialog"
        const val PAGE_SIZE = 20
    }

    private var pageNum: Int = 1
    private var coHostListAdapter: CoHostListAdapter? = null
    private lateinit var contentView: View

    private var coHostListener: NECoHostListener = object : NECoHostListener {
        override fun onConnectionUserListChanged(
            connectedList: List<ConnectionUser>,
            joinedList: List<ConnectionUser>,
            leavedList: List<ConnectionUser>
        ) {
            refreshCoHostState()
            fetchCoHostList()
        }

        override fun onConnectionRequestReceived(
            inviter: ConnectionUser,
            inviteeList: List<ConnectionUser>,
            ext: String?
        ) {
            fetchCoHostList()
        }

        override fun onConnectionRequestCancelled(inviter: ConnectionUser) {
            fetchCoHostList()
        }

        override fun onConnectionRequestAccept(invitee: ConnectionUser) {
            fetchCoHostList()
        }

        override fun onConnectionRequestReject(invitee: ConnectionUser) {
            fetchCoHostList()
        }

        override fun onConnectionRequestTimeout(
            inviter: ConnectionUser,
            inviteeList: List<ConnectionUser>,
            ext: String?
        ) {
            fetchCoHostList()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getInstance().getCoHostManager().addListener(coHostListener)
    }

    override fun onDetachedFromWindow() {
        getInstance().getCoHostManager().removeListener(coHostListener)
        super.onDetachedFromWindow()
    }

    override fun renderTopView(parent: FrameLayout) {
        val titleView = TextView(context)
        titleView.text = context.getString(R.string.co_host_invite)
        titleView.setTypeface(null, Typeface.BOLD)
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        titleView.setGravity(Gravity.CENTER)
        titleView.setTextColor(
            ResourcesCompat.getColor(context.resources, R.color.color_333333, null)
        )
        val layoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        parent.addView(titleView, layoutParams)
    }

    override fun renderContentView(parent: FrameLayout) {
        contentView =
            LayoutInflater.from(context).inflate(R.layout.live_dialog_co_host_invite, parent)
        refreshCoHostState()
        val recyclerView = contentView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER)
        recyclerView.layoutManager = LinearLayoutManager(context)
        coHostListAdapter = CoHostListAdapter(activity)
        coHostListAdapter!!.inviteClickListener = object : CoHostListAdapter.OnInviteClickListener {
            override fun onInviteClick(hostInfo: CoHostListAdapter.COHostInfo) {
                sendCoHostInvitation(hostInfo)
            }

            override fun onCancelInviteClick(hostInfo: CoHostListAdapter.COHostInfo) {
                sendCancelCoHostInvitation(hostInfo)
            }
        }
        recyclerView.adapter = coHostListAdapter
        fetchCoHostList()
    }

    private fun sendCoHostInvitation(hostInfo: CoHostListAdapter.COHostInfo) {
        NELiveStreamKit.getInstance().getCoHostManager().requestConnection(
            hostInfo.roomUuid,
            LiveStreamUIConstants.CO_HOST_CONNECT_TIMEOUT,
            "",
            object :
                NELiveStreamCallback<Unit> {
                override fun onSuccess(t: Unit?) {
                    ToastX.showShortToast(R.string.co_host_invite_success)
                    fetchCoHostList()
                }

                override fun onFailure(code: Int, msg: String?) {
                    ToastX.showShortToast(msg)
                }
            }
        )
    }

    private fun sendCancelCoHostInvitation(hostInfo: CoHostListAdapter.COHostInfo) {
        NELiveStreamKit.getInstance().getCoHostManager().cancelConnectionRequest(
            hostInfo.roomUuid,
            object :
                NELiveStreamCallback<Unit> {
                override fun onSuccess(t: Unit?) {
                    ToastX.showShortToast(R.string.live_anchor_invite_success_cancel)
                    fetchCoHostList()
                }

                override fun onFailure(code: Int, msg: String?) {
                    ToastX.showShortToast(msg)
                }
            }
        )
    }

    private fun refreshCoHostState() {
        val clInfoCL = contentView.findViewById<View>(R.id.cl_co_info)
        if (getConnectedHosts().isNotEmpty()) {
            clInfoCL.visibility = View.VISIBLE
            val stateTV = contentView.findViewById<TextView>(R.id.tv_state)
            val stateStopTV = contentView.findViewById<TextView>(R.id.tv_state_stop)
            stateTV.text = String.format(
                XKitUtils.getApplicationContext().getString(R.string.co_with_user),
                getConnectedHostNicks() ?: ""
            )
            stateStopTV.setOnClickListener {
                showDisCoHostDialog(getConnectedHostNicks() ?: "")
            }
        } else {
            clInfoCL.visibility = View.GONE
        }
    }

    private fun getConnectedHosts(): List<ConnectionUser> {
        return NELiveStreamKit.getInstance().getCoHostManager().coHostState.connectedUserList.get()
    }

    private fun getConnectedHostNicks(): String? {
        val connectedUserList = getConnectedHosts()
        if (connectedUserList.isNotEmpty()) {
            var coUsers = ""
            connectedUserList.forEach {
                coUsers += it.name + "、"
            }
            if (coUsers.endsWith("、")) {
                coUsers = coUsers.substring(0, coUsers.length - 1)
            }
            return coUsers
        }
        return null
    }

    private fun fetchCoHostList() {
        NELiveStreamKit.getInstance().fetchCoLiveRooms(
            pageNum,
            PAGE_SIZE,
            object : NELiveStreamCallback<NELiveRoomList> {
                override fun onSuccess(t: NELiveRoomList?) {
                    LiveRoomLog.d(TAG, "fetchCoLiveRooms onSuccess")
                    t?.let {
                        val coHostInfoList = mutableListOf<CoHostListAdapter.COHostInfo>()
                        it.list?.forEach { roomInfo ->
                            coHostInfoList.add(
                                CoHostListAdapter.COHostInfo(
                                    roomInfo.anchor.account,
                                    roomInfo.anchor.nick,
                                    roomInfo.anchor.avatar,
                                    roomInfo.liveModel.roomUuid,
                                    roomInfo.liveModel.connectionStatus
                                )
                            )
                        }
                        coHostListAdapter?.setData(coHostInfoList)
                    }
                }

                override fun onFailure(code: Int, msg: String?) {
                    LiveRoomLog.e(TAG, "fetchCoLiveRooms onFailure code: $code msg: $msg")
                }
            }
        )
    }

    /**
     * 结束主播PK二次确认
     */
    private fun showDisCoHostDialog(content: String) {
        val dialog = ChoiceDialog(activity)
        dialog.setTitle(
            String.format(
                XKitUtils.getApplicationContext().getString(R.string.co_host_hanup),
                content
            )
        )
            .setNegativeButton(XKitUtils.getApplicationContext().getString(R.string.live_cancel)) {
            }
            .setPositiveButton(XKitUtils.getApplicationContext().getString(R.string.co_leave)) {
                NELiveStreamKit.getInstance().getCoHostManager().disconnect()
            }.show()
    }
}
