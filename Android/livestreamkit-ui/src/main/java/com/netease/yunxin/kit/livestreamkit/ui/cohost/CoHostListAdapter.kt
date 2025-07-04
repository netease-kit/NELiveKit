/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.kit.common.image.ImageLoader
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils
import com.netease.yunxin.kit.livestreamkit.ui.R

/**
 * 邀请主播列表
 */
class CoHostListAdapter(val context: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val coHostInfos: ArrayList<COHostInfo> = ArrayList()
    private val type: Int = TYPE_INVITE
    var inviteClickListener: OnInviteClickListener? = null

    fun setData(infos: List<COHostInfo>) {
        coHostInfos.clear()
        coHostInfos.addAll(infos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_EMPTY -> {
                val emptyView =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.live_apply_seat_list_empty_layout, parent, false)
                object : RecyclerView.ViewHolder(emptyView) {}
            }

            else -> {
                val common = LayoutInflater.from(parent.context)
                    .inflate(R.layout.live_view_item_co_host_invite, parent, false)
                CoHostInviteViewHolder(common)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HostViewHolder) {
            val member = coHostInfos[position]
            holder.mTvNick.text = member.nickname
            ImageLoader.with(context)
                .circleLoad(member.avatar, holder.mIvAvatar)
            when (holder) {
                is CoHostInviteViewHolder -> {
                    if (member.connectionStatus == CONNECTION_STATUS_INVITING) {
                        holder.mTvInvite.text =
                            context.getString(R.string.live_cancel_invite_host)
                        holder.mTvInvite.setOnClickListener(
                            View.OnClickListener {
                                if (ClickUtils.isFastClick()) {
                                    return@OnClickListener
                                }
                                inviteClickListener?.onCancelInviteClick(member)
                            }
                        )
                    } else {
                        holder.mTvInvite.text =
                            context.getString(R.string.live_invite_host)
                        holder.mTvInvite.setOnClickListener(
                            View.OnClickListener {
                                if (ClickUtils.isFastClick()) {
                                    return@OnClickListener
                                }
                                inviteClickListener?.onInviteClick(member)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun removeMember(seatInfo: COHostInfo) {
        val index = coHostInfos.indexOf(seatInfo)
        if (index >= 0) {
            coHostInfos.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (coHostInfos.isEmpty()) {
            return TYPE_EMPTY
        } else {
            return type
        }
    }

    override fun getItemCount(): Int {
        return if (coHostInfos.isNotEmpty()) {
            coHostInfos.size
        } else {
            1
        }
    }

    private open class HostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mIvAvatar: ImageView = itemView.findViewById(R.id.iv_audience_avatar)
        var mTvNick: TextView = itemView.findViewById(R.id.tv_audience_nickname)
    }

    private class CoHostInviteViewHolder(itemView: View) : HostViewHolder(itemView) {
        var mTvInvite: TextView = itemView.findViewById(R.id.tv_invite)
    }

    interface OnInviteClickListener {
        fun onInviteClick(hostInfo: COHostInfo)
        fun onCancelInviteClick(hostInfo: COHostInfo)
    }

    companion object {
        private const val TAG = "CoHostListAdapter"
        const val TYPE_EMPTY: Int = 0
        const val TYPE_INVITE: Int = 1

        // 0 空闲 1 申请中 2 已接受 3 已拒绝 4 已取消 6 已断开 7 已连线 8 已超时
        const val CONNECTION_STATUS_IDLE = 0
        const val CONNECTION_STATUS_INVITING = 1
        const val CONNECTION_STATUS_ACCEPTED = 2
        const val CONNECTION_STATUS_REJECTED = 3
        const val CONNECTION_STATUS_CANCELED = 4
        const val CONNECTION_STATUS_DISCONNECTED = 6
        const val CONNECTION_STATUS_CONNECTED = 7
        const val CONNECTION_STATUS_TIMEOUT = 8
    }

    data class COHostInfo(
        var uuid: String,
        var nickname: String?,
        var avatar: String?,
        var roomUuid: String,
        var connectionStatus: Int? = 0
    )
}
