/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.dialog

import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.app.newlive.R

/**
 * dump dialog
 */
class DumpDialog : BaseBottomDialog() {
    var btnStart: Button? = null
    var btnStop: Button? = null
    override fun getResourceLayout(): Int {
        return R.layout.test_dump_layout
    }

    override fun initView(rootView: View) {
        super.initView(rootView)
        btnStart = rootView.findViewById(R.id.btn_start_dump)
        btnStop = rootView.findViewById(R.id.btn_stop_dump)
    }

    override fun initData() {
        super.initData()
        btnStart?.setOnClickListener(View.OnClickListener { v: View? ->
            btnStart?.isEnabled = false
            ToastUtils.showLong(R.string.biz_live_start_audio_dump)
            //todo startAudioDump
//            NERtcEx.getInstance().startAudioDump()
        })
        btnStop?.setOnClickListener(View.OnClickListener { v: View? ->
            btnStart?.isEnabled = true
            ToastUtils.showLong(R.string.biz_live_dump_end)
            //todo stopAudioDump
//            NERtcEx.getInstance().stopAudioDump()
        })
    }

    companion object {
        fun showDialog(fragmentManager: FragmentManager) {
            val dumpDialog = DumpDialog()
            dumpDialog.show(fragmentManager, "dumpDialog")
        }
    }
}