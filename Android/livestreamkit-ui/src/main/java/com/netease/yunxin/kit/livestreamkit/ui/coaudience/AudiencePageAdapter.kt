/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.coaudience

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.netease.yunxin.kit.livestreamkit.ui.coaudience.fragment.HostLinkSeatFragment
import java.util.ArrayList

class AudiencePageAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        when (position) {
            0 -> bundle.putInt(
                HostLinkSeatFragment.Companion.TYPE,
                HostLinkSeatFragment.Companion.TYPE_INVITE
            )
            1 -> bundle.putInt(
                HostLinkSeatFragment.Companion.TYPE,
                HostLinkSeatFragment.Companion.TYPE_APPLY
            )
            2 -> bundle.putInt(
                HostLinkSeatFragment.Companion.TYPE,
                HostLinkSeatFragment.Companion.TYPE_MANAGE
            )
        }
        cacheFragment[position].arguments = bundle
        return cacheFragment[position]
    }

    override fun getCount(): Int {
        return SIZE
    }

    private val cacheFragment: MutableList<HostLinkSeatFragment> = ArrayList(SIZE)
    private fun initFragment() {
        for (i in 0 until SIZE) {
            cacheFragment.add(HostLinkSeatFragment())
        }
    }

    companion object {
        private const val SIZE = 3
    }

    init {
        initFragment()
    }
}
