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
import com.netease.yunxin.kit.common.ui.utils.ToastX
import com.netease.yunxin.kit.common.utils.XKitUtils
import com.netease.yunxin.kit.entertainment.common.dialog.ChoiceDialog
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit
import com.netease.yunxin.kit.livestreamkit.ui.R
import com.netease.yunxin.kit.livestreamkit.ui.coaudience.fragment.HostLinkSeatFragment
import com.netease.yunxin.kit.livestreamkit.ui.view.SeatView

/**
 * 观众列表
 */
class HostLinkSeatListAdapter(context: Activity?, type: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val memberInfos: ArrayList<SeatView.SeatInfo> = ArrayList()
    private val type: Int = type
    private val context: Activity? = context

    fun setData(members: List<SeatView.SeatInfo>?) {
        if (members == null) {
            return
        }
        memberInfos.clear()
        memberInfos.addAll(members)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HostLinkSeatFragment.TYPE_EMPTY -> {
                val emptyView =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.live_apply_seat_list_empty_layout, parent, false)
                object : RecyclerView.ViewHolder(emptyView) {}
            }

            HostLinkSeatFragment.TYPE_APPLY -> {
                val apply = LayoutInflater.from(parent.context)
                    .inflate(R.layout.live_view_item_link_seat_apply, parent, false)
                LinkSeatApplyViewHolder(apply)
            }
            HostLinkSeatFragment.TYPE_MANAGE -> {
                val seat = LayoutInflater.from(parent.context)
                    .inflate(R.layout.live_view_item_link_seat_manager, parent, false)
                LinkSeatManagerViewHolder(seat)
            }
            else -> {
                val common = LayoutInflater.from(parent.context)
                    .inflate(R.layout.live_view_item_link_seat_invite, parent, false)
                LinkSeatInviteViewHolder(common)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (context == null) {
            return
        }
        if (holder is AudienceViewHolder) {
            val member = memberInfos.get(position)
            holder.mTvNumber.text = (position + 1).toString()
            holder.mTvNick.text = member.nickname
            ImageLoader.with(context)
                .circleLoad(member.avatar, holder.mIvAvatar)
            when (holder) {
                is LinkSeatInviteViewHolder -> {
                    holder.mTvInvite.setOnClickListener(
                        View.OnClickListener {
                            if (ClickUtils.isFastClick()) {
                                return@OnClickListener
                            }

                            sendSeatInvitation(member)
                        }
                    )
                }
                is LinkSeatApplyViewHolder -> {
                    holder.mTvAccept.setOnClickListener(
                        View.OnClickListener {
                            if (ClickUtils.isFastClick()) {
                                return@OnClickListener
                            }
                            if (NELiveStreamKit.getInstance().getCoHostManager().coHostState.connectedUserList.get().size > 0) {
                                rejectSeatRequest(member)
                            } else {
                                approveSeatRequest(member)
                            }
                        }
                    )
                    holder.mTvReject.setOnClickListener(
                        View.OnClickListener { v: View? ->
                            if (ClickUtils.isFastClick()) {
                                return@OnClickListener
                            }

                            rejectSeatRequest(member)
                        }
                    )
                }
                is LinkSeatManagerViewHolder -> {
                    holder.mIvAudio.isSelected = member.isAudioMute
                    holder.mIvVideo.isSelected = member.isVideoMute
                    holder.mIvAudio.setOnClickListener(
                        View.OnClickListener { v: View ->
                            if (ClickUtils.isFastClick()) {
                                return@OnClickListener
                            }
//                        val audioParams = SetSeatAVMuteStateParams(
//                            null,
//                            member.accountId,
//                            if (v.isSelected) SeatAVState.OPEN else SeatAVState.CLOSE
//                        )
//                        seatService.setSeatAudioMuteState(
//                            audioParams,
//                            object : CompletionCallback<Void> {
//                                override fun success(info: Void?) {
//                                    v.isSelected = !v.isSelected
//                                    member.audioState = if (v.isSelected) 0 else 1
//                                }
//
//                                override fun error(code: Int, msg: String) {
//                                    ToastUtils.showShort(msg)
//                                }
//                            })
                        }
                    )
                    holder.mIvVideo.setOnClickListener(
                        View.OnClickListener { v: View ->
                            if (ClickUtils.isFastClick()) {
                                return@OnClickListener
                            }
//                        val videoParams = SetSeatAVMuteStateParams(
//                            null,
//                            member.accountId,
//                            if (v.isSelected) SeatAVState.OPEN else SeatAVState.CLOSE
//                        )
//                        seatService.setSeatVideoMuteState(
//                            videoParams,
//                            object : CompletionCallback<Void> {
//                                override fun success(info: Void?) {
//                                    v.isSelected = !v.isSelected
//                                    member.videoState = if (v.isSelected) 0 else 1
//                                }
//
//                                override fun error(code: Int, msg: String) {
//                                    ToastUtils.showShort(msg)
//                                }
//                            })
                        }
                    )
                    holder.mTvHangup.setOnClickListener(
                        View.OnClickListener { v: View ->
                            if (ClickUtils.isFastClick()) {
                                return@OnClickListener
                            }
                            showKickDialog(member)
                        }
                    )
                }
            }
        }
    }

    private fun removeMember(seatInfo: SeatView.SeatInfo?) {
        val index = memberInfos.indexOf(seatInfo)
        if (index >= 0) {
            memberInfos.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * 踢下麦二次确认
     *
     * @param member
     */
    private fun showKickDialog(member: SeatView.SeatInfo) {
        if (context == null) {
            return
        }
        val dialog = ChoiceDialog(context)
        dialog.setContent(
            String.format(
                XKitUtils.getApplicationContext().getString(R.string.live_sure_hanup_link_seat),
                member.nickname
            )
        )
            .setNegativeButton(XKitUtils.getApplicationContext().getString(R.string.live_cancel)) {
            }
            .setPositiveButton(XKitUtils.getApplicationContext().getString(R.string.live_hangup)) {
                NELiveStreamKit.getInstance().kickSeat(
                    member.uuid,
                    object : NELiveStreamCallback<Unit> {
                        override fun onSuccess(t: Unit?) {
                            removeMember(member)
                            ToastX.showShortToast(R.string.live_have_kick_seat)
                        }

                        override fun onFailure(code: Int, msg: String?) {
                            ToastX.showShortToast(msg)
                        }
                    }
                )
            }.show()
    }

    override fun getItemViewType(position: Int): Int {
        if (memberInfos.isEmpty()) {
            return HostLinkSeatFragment.TYPE_EMPTY
        } else {
            return type
        }
    }

    override fun getItemCount(): Int {
        return if (memberInfos.isNotEmpty()) {
            memberInfos.size
        } else {
            1
        }
    }

    private open class AudienceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvNumber: TextView = itemView.findViewById(R.id.tv_audience_no)
        var mIvAvatar: ImageView = itemView.findViewById(R.id.iv_audience_avatar)
        var mTvNick: TextView = itemView.findViewById(R.id.tv_audience_nickname)
    }

    private class LinkSeatInviteViewHolder(itemView: View) : AudienceViewHolder(itemView) {
        var mTvInvite: TextView = itemView.findViewById(R.id.tv_invite)
    }

    private class LinkSeatApplyViewHolder(itemView: View) : AudienceViewHolder(itemView) {
        var mTvReject: TextView = itemView.findViewById(R.id.tv_reject)
        var mTvAccept: TextView = itemView.findViewById(R.id.tv_accept)
    }

    private class LinkSeatManagerViewHolder(itemView: View) : AudienceViewHolder(itemView) {
        var mIvVideo: ImageView = itemView.findViewById(R.id.iv_video)
        var mIvAudio: ImageView = itemView.findViewById(R.id.iv_audio)
        var mTvHangup: TextView = itemView.findViewById(R.id.tv_hangup)
    }

    private fun sendSeatInvitation(seatInfo: SeatView.SeatInfo) {
        NELiveStreamKit.getInstance().sendSeatInvitation(
            seatInfo.uuid,
            object :
                NELiveStreamCallback<Unit> {
                override fun onSuccess(t: Unit?) {
                    ToastX.showShortToast(R.string.live_anchor_invite_success)
                }

                override fun onFailure(code: Int, msg: String?) {
                    ToastX.showShortToast(msg)
                }
            }
        )
    }

    private fun approveSeatRequest(seatInfo: SeatView.SeatInfo) {
        NELiveStreamKit.getInstance().approveSeatRequest(
            seatInfo.uuid,
            object : NELiveStreamCallback<Unit> {
                override fun onSuccess(t: Unit?) {
                    removeMember(seatInfo)
                    ToastX.showShortToast(R.string.live_have_accepted)
                }

                override fun onFailure(code: Int, msg: String?) {
                    ToastX.showShortToast(msg)
                }
            }
        )
    }

    private fun rejectSeatRequest(seatInfo: SeatView.SeatInfo) {
        NELiveStreamKit.getInstance().rejectSeatRequest(
            seatInfo.uuid,
            object : NELiveStreamCallback<Unit> {
                override fun onSuccess(t: Unit?) {
                    removeMember(seatInfo)
                    if (NELiveStreamKit.getInstance().getCoHostManager().coHostState.connectedUserList.get().size > 0) {
                        ToastX.showShortToast(R.string.co_host_can_not_on_seat)
                    } else {
                        ToastX.showShortToast(R.string.live_have_reject)
                    }
                }

                override fun onFailure(code: Int, msg: String?) {
                    ToastX.showShortToast(msg)
                }
            }
        )
    }
}
