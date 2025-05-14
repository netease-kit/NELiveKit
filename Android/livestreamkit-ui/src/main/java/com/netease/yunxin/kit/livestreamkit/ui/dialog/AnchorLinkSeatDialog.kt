/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.netease.yunxin.kit.livestreamkit.ui.R

/**
 * 观众连麦dialog
 */
class AnchorLinkSeatDialog : BaseLinkSeatDialog() {
    private var tvInviteCount: TextView? = null
    private var tvApplyCount: TextView? = null
    private var tvConnectManage: TextView? = null

    fun initView(rootView: View) {
        val audiencePages: ViewPager = rootView.findViewById(R.id.vp_audience)
        audiencePages.adapter = AudiencePageAdapter(childFragmentManager)
        audiencePages.offscreenPageLimit = 3
        val tabLayout: TabLayout = rootView.findViewById(R.id.tab_audience_type)
        tabLayout.setupWithViewPager(audiencePages)
        tabLayout.removeAllTabs()
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val tab1 = tabLayout.newTab().setCustomView(R.layout.live_view_item_audience_tab)
        tvInviteCount = tab1.customView?.findViewById(R.id.tv_tab_name)
        tvInviteCount?.text = getString(R.string.live_invite_join_seats)
        tabLayout.addTab(tab1, 0, false)
        val tab2 = tabLayout.newTab().setCustomView(R.layout.live_view_item_audience_tab)
        tvApplyCount = tab2.customView?.findViewById(R.id.tv_tab_name)
        tvApplyCount?.setText(R.string.live_apply_seat)
        tabLayout.addTab(tab2, 1, true)
        val tab3 = tabLayout.newTab().setCustomView(R.layout.live_view_item_audience_tab)
        tvConnectManage = tab3.customView?.findViewById(R.id.tv_tab_name)
        tvConnectManage?.setText(R.string.live_apply_seat_manager)
        tabLayout.addTab(tab3, 2, false)
        audiencePages.currentItem = 1
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        val rootView =
            LayoutInflater.from(context)
                .inflate(R.layout.live_audience_connect_dialog_layout, container, container != null)
        initView(rootView)
        return rootView
    }
}
