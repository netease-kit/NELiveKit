/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl

internal object Constants {
    const val TYPE_BATCH_GIFT = 1005 // 批量礼物
    const val TYPE_LIVE_PAUSE = 1105 // 暂停直播
    const val TYPE_LIVE_RESUME = 1106 // 恢复直播
    const val TYPE_LIVE_CO_REQUEST_RECEIVE = 10001 // 请求主播连麦
    const val TYPE_LIVE_CO_REQUEST_CANCEL = 10002 // 取消请求主播连麦
    const val TYPE_LIVE_CO_REQUEST_ACCEPT = 10003 // 同意请求主播连麦
    const val TYPE_LIVE_CO_REQUEST_REJECT = 10004 // 拒绝请求主播连麦
    const val TYPE_LIVE_CO_HOST_DISCONNECT = 10005 // 结束主播连麦
    const val TYPE_LIVE_CO_REQUEST_TIMEOUT = 10008 // 请求主播连麦超时
}
