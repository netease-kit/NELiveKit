/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.app.newlive.Constants
import com.netease.yunxin.app.newlive.utils.NavUtils
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.app.newlive.list.FunctionAdapter
import com.netease.yunxin.app.newlive.list.FunctionItem
import com.netease.yunxin.kit.livekit.NELiveConstants
import java.util.*

class AppEntranceFragment : BaseFragment() {
    private fun initView(rootView: View) {
        // 功能列表初始化
        val rvFunctionList: RecyclerView = rootView.findViewById(R.id.rv_function_list)
        rvFunctionList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvFunctionList.adapter = FunctionAdapter(
            context, Arrays.asList( // 每个业务功能入口均在此处生成 item
                FunctionItem(
                    R.drawable.icon_pk_live,
                    getString(R.string.app_pk_live),
                    getString(R.string.app_pk_live_desc_text)
                ) {
                    NavUtils.toLiveListPage(requireContext(), getString(R.string
                        .app_pk_live2), NELiveConstants.LiveType.LIVE_TYPE_PK);
                },
                //先隐藏多人连麦
//                FunctionItem(
//                    R.drawable.icon_multi_micro,
//                    getString(R.string.app_multiple_link_seat_live),
//                    getString(R.string.app_multiple_link_seat_live_desc_text)
//                ) {
//                    NavUtils.toLiveListPage(requireContext(), getString(R.string
//                        .app_multiple_link_seat_live), Constants.LiveType.LIVE_TYPE_SEAT)
//                }
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_app_entrance, container, false)
        initView(rootView)
        paddingStatusBarHeight(rootView)
        return rootView
    }
}