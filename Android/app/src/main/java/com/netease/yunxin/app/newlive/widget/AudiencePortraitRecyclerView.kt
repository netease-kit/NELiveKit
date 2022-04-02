/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.android.lib.picture.ImageLoader
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.app.newlive.list.LiveBaseAdapter
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import java.util.*

/**
 * Created by luc on 2020/11/23.
 */
class AudiencePortraitRecyclerView : RecyclerView {
    private val layoutManager by lazy { LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) }
    private val adapter by lazy { InnerAdapter(
        context
    ) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setLayoutManager(layoutManager)
        setAdapter(adapter)
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setLayoutManager(null)
        setAdapter(null)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(
                false
            )
        }
        return super.dispatchTouchEvent(ev)
    }

    fun addItem(audience: NERoomMember) {
        adapter.addItem(audience)
    }

    fun addItems(audienceList: List<NERoomMember>?) {
        adapter.addItems(audienceList)
    }

    fun removeItem(audience: NERoomMember?) {
        adapter.removeItem(audience)
    }

    fun updateAll(audienceList: List<NERoomMember>) {
        adapter.clear()
        addItems(audienceList)
    }

    private class InnerAdapter(private val context: Context?) : Adapter<LiveBaseAdapter.LiveViewHolder?>() {
        private val dataSource: MutableList<NERoomMember> = ArrayList()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveBaseAdapter.LiveViewHolder {
            return LiveBaseAdapter.LiveViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.view_item_audience_portrait_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: LiveBaseAdapter.LiveViewHolder, position: Int) {
            val ivPortrait = holder.getView<ImageView?>(R.id.iv_item_audience_portrait)
            val info = dataSource[position]
            //todo avatar
            ImageLoader.with(context).circleLoad("", ivPortrait)
        }

        override fun getItemCount(): Int {
            return Math.min(dataSource.size, MAX_SHOWN_COUNT)
        }

        fun addItem(audience: NERoomMember) {
            dataSource.add(audience)
            notifyDataSetChanged()
        }

        fun addItems(audienceList: List<NERoomMember>?) {
            if (audienceList == null) {
                return
            }
            dataSource.addAll(audienceList)
            notifyDataSetChanged()
        }

        fun removeItem(audience: NERoomMember?) {
            if (dataSource.remove(audience)) {
                notifyDataSetChanged()
            }
        }

        fun clear() {
            dataSource.clear()
        }
    }

    companion object {
        private const val MAX_SHOWN_COUNT = 10
    }
}