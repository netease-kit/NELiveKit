/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.live

import com.netease.yunxin.android.lib.network.common.BaseResponse
import com.netease.yunxin.android.lib.network.common.NetworkClient
import com.netease.yunxin.android.lib.network.common.transform.ErrorTransform
import com.netease.yunxin.kit.livekit.model.response.LiveListResponse
import io.reactivex.Single
import java.util.*

/**
 * 直播网络访问交互
 */
object LiveInteraction {
    /**
     * 获取随机主题
     *
     * @return
     */
    fun getTopic(): Single<BaseResponse<String?>?>? {
        val api = NetworkClient.getInstance().getService(
            LiveServerApi::class.java
        )
        val params: MutableMap<String?, Any?> = HashMap(1)
        return api.getTopic(params).compose(ErrorTransform())
            .map { stringBaseResponse: BaseResponse<String?>? -> stringBaseResponse }
    }

    /**
     * 获取随机封面
     *
     * @return
     */
    fun getCover(): Single<BaseResponse<String?>?>? {
        val api = NetworkClient.getInstance().getService(
            LiveServerApi::class.java
        )
        val params: MutableMap<String?, Any?> = HashMap(1)
        return api.getCover(params).compose(ErrorTransform())
            .map { stringBaseResponse: BaseResponse<String?>? -> stringBaseResponse }
    }
}