/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.user

import android.os.Bundle
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.android.lib.picture.ImageLoader
import com.netease.yunxin.app.newlive.Constants
import com.netease.yunxin.app.newlive.R
import com.netease.yunxin.app.newlive.activity.BaseActivity
import com.netease.yunxin.app.newlive.config.StatusBarConfig
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.login.AuthorManager
import com.netease.yunxin.kit.login.model.*

class UserInfoActivity : BaseActivity() {
    companion object val TAG = "UserInfoActivity"
    private val loginObserver: LoginObserver<LoginEvent> = object : LoginObserver<LoginEvent> {
        override fun onEvent(event: LoginEvent) {
            if (event.eventType == EventType.TYPE_UPDATE){
                currentUserInfo = event.userInfo
                initUser()
            }
        }
    }
    private var currentUserInfo: UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthorManager.registerLoginObserver(loginObserver)
        currentUserInfo = AuthorManager.getUserInfo()
        setContentView(R.layout.activity_user_info)
        initViews()
        paddingStatusBarHeight(findViewById(R.id.cl_root))
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthorManager.unregisterLoginObserver(loginObserver)
    }

    private fun initViews() {
        val logout = findViewById<View>(R.id.tv_logout)
        logout.setOnClickListener { v: View? ->
            AuthorManager.logoutWitDialog(this,object :LoginCallback<Void>{
                override fun onSuccess(data: Void?) {
                    AuthorManager.logout(object: LoginCallback<Void>{
                        override fun onSuccess(data: Void?) {
                            ALog.d(TAG, "logout success")
                        }

                        override fun onError(errorCode: Int, errorMsg: String) {
                            ALog.e(TAG, "logout failed code = $errorCode, message = $errorMsg")
                        }
                    })
                    finish()
                }

                override fun onError(errorCode: Int, errorMsg: String) {
                    ToastUtils.showShort(getString(R.string.logout_error_msg))
                }
            })
        }
        val close = findViewById<View>(R.id.iv_close)
        close.setOnClickListener { v: View? -> finish() }
        initUser()
    }

    private fun initUser() {
        val ivUserPortrait = findViewById<ImageView>(R.id.iv_user_portrait)
        ImageLoader.with(applicationContext).circleLoad(currentUserInfo!!.avatar, ivUserPortrait)
        val tvNickname = findViewById<TextView>(R.id.tv_nick_name)
        //暂时关闭昵称修改入口
//        tvNickname.setOnClickListener { v: View? ->
//            startActivity(
//                Intent(
//                    this@UserInfoActivity,
//                    EditUserInfoActivity::class.java
//                )
//            )
//        }
        tvNickname.text = currentUserInfo!!.nickname
    }

    override fun provideStatusBarConfig(): StatusBarConfig? {
        return StatusBarConfig.Builder()
            .statusBarDarkFont(false)
            .build()
    }

}