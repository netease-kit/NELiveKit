/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.coaudience.fragment

import HostLinkSeatListAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.kit.common.ui.utils.ToastX
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfoList
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomLog
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIConstants
import com.netease.yunxin.kit.livestreamkit.ui.R
import com.netease.yunxin.kit.livestreamkit.ui.view.SeatView

class HostLinkSeatFragment : BaseLinkSeatFragment() {
    private var type = TYPE_INVITE
    private var audienceLinkSeatListAdapter: HostLinkSeatListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.live_fragment_audience_list_layout,
            container,
            false
        )
        initView(rootView)
        return rootView
    }

    private fun initView(view: View?) {
        arguments?.let {
            val bundle = it
            type = bundle.getInt(TYPE)
        }
        val rvAudienceList: RecyclerView? = view?.findViewById(R.id.rcv_audience)
        rvAudienceList?.layoutManager = LinearLayoutManager(context)
        audienceLinkSeatListAdapter = HostLinkSeatListAdapter(activity, type)
        rvAudienceList?.adapter = audienceLinkSeatListAdapter
    }

    override fun onResume() {
        super.onResume()
        if (type == TYPE_INVITE) {
            NELiveStreamKit.getInstance().getAudienceList(
                1,
                LiveStreamUIConstants.PAGE_SIZE,
                object :
                    NELiveStreamCallback<NEAudienceInfoList> {
                    override fun onSuccess(t: NEAudienceInfoList?) {
                        LiveRoomLog.i(TAG, "getAudienceList success:${t?.list}")
                        audienceLinkSeatListAdapter?.setData(
                            t?.list?.map { neAudienceInfo ->
                                SeatView.SeatInfo(
                                    neAudienceInfo.userUuid,
                                    neAudienceInfo.userName,
                                    neAudienceInfo.icon
                                )
                            }
                        )
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        LiveRoomLog.e(TAG, "getAudienceList error: $code, msg:$msg")
                        ToastX.showShortToast(msg)
                    }
                }
            )
        } else if (type == TYPE_APPLY) {
            refreshSeatRequestList2UI()
        } else if (type == TYPE_MANAGE) {
            refreshSeatInfoList2UI()
        }
    }

    override fun onLocalSeatRequest() {
        super.onLocalSeatRequest()
        refreshSeatRequestList2UI()
    }

    override fun onRemoteSeatRequest(account: String?) {
        super.onRemoteSeatRequest(account)
        refreshSeatRequestList2UI()
    }

    override fun onLocalSeatRequestCanceled() {
        super.onLocalSeatRequestCanceled()
        refreshSeatRequestList2UI()
    }

    override fun onLocalSeatLinked() {
        super.onLocalSeatLinked()
        refreshSeatRequestList2UI()
    }

    override fun onRemoteSeatLinked(account: String?) {
        super.onRemoteSeatLinked(account)
        refreshSeatRequestList2UI()
    }

    override fun onLocalSeatUnlinked() {
        super.onLocalSeatUnlinked()
        refreshSeatRequestList2UI()
    }

    override fun onRemoteSeatUnlinked(account: String?) {
        super.onRemoteSeatUnlinked(account)
        refreshSeatRequestList2UI()
    }

    private fun refreshSeatRequestList2UI() {
        if (type == TYPE_APPLY) {
            getSeatRequestList(
                object : NELiveStreamCallback<List<SeatView.SeatInfo?>?> {
                    override fun onSuccess(t: List<SeatView.SeatInfo?>?) {
                        audienceLinkSeatListAdapter?.setData(
                            t?.map { neAudienceInfo ->
                                SeatView.SeatInfo(
                                    neAudienceInfo?.uuid,
                                    neAudienceInfo?.nickname,
                                    neAudienceInfo?.avatar
                                )
                            }
                        )
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        ToastX.showShortToast(msg)
                    }
                }
            )
        }
    }

    private fun refreshSeatInfoList2UI() {
        if (type == TYPE_MANAGE) {
            getOnSeatInfo(object : NELiveStreamCallback<List<SeatView.SeatInfo?>?> {
                override fun onSuccess(t: List<SeatView.SeatInfo?>?) {
                    audienceLinkSeatListAdapter?.setData(
                        t?.map { neAudienceInfo ->
                            SeatView.SeatInfo(
                                neAudienceInfo?.uuid,
                                neAudienceInfo?.nickname,
                                neAudienceInfo?.avatar
                            )
                        }
                    )
                }

                override fun onFailure(code: Int, msg: String?) {
                    ToastX.showShortToast(msg)
                }
            })
        }
    }

    companion object {
        private const val TAG = "AnchorLinkSeatFragment"
        const val TYPE: String = "fragment_type"
        const val TYPE_EMPTY: Int = 0
        const val TYPE_INVITE: Int = 1
        const val TYPE_APPLY: Int = 2
        const val TYPE_MANAGE: Int = 3
    }
}
