/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.utils
import android.content.Context
import android.os.Build
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.corekit.XKitLog
import com.netease.yunxin.kit.corekit.XKitLog.getNimExtraLogPath

class LiveRoomLog {

    companion object {
        private val prefix = "[LiveRoomLog]" + "phone:" + Build.BRAND + ":"

        @JvmStatic
        fun getPlayerLogDir(context: Context): String {
            return getNimExtraLogPath(context) + "/player"
        }

        @JvmStatic
        fun i(tag: String, log: String) {
            XKitLog.i("$prefix $tag", log)
        }

        @JvmStatic
        fun w(tag: String, log: String) {
            XKitLog.w("$prefix $tag", log)
        }

        @JvmStatic
        fun d(tag: String, log: String) {
            XKitLog.d("$prefix $tag", log)
        }

        @JvmStatic
        fun e(tag: String, log: String) {
            XKitLog.e("$prefix $tag", log)
        }

        @JvmStatic
        fun logApi(log: String) {
            XKitLog.api(prefix, log)
        }

        @JvmStatic
        fun flush(isFlush: Boolean) {
            ALog.flush(isFlush)
        }
    }
}
