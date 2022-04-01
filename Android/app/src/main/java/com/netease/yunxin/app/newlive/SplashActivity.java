package com.netease.yunxin.app.newlive;


import android.content.Intent;
import android.os.Bundle;

import com.netease.yunxin.app.newlive.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.app.newlive.activity.BaseActivity;
import com.netease.yunxin.app.newlive.config.StatusBarConfig;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.UserInfo;

import androidx.annotation.Nullable;

public class SplashActivity extends BaseActivity {

    private final static String TAG = "SplashActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isTaskRoot()){
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if(mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)){
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_splash);
        AuthorManager.INSTANCE.autoLogin(false, new LoginCallback<UserInfo>() {

            @Override
            public void onSuccess(UserInfo userInfo) {
                ALog.d(TAG, "autoLogin success");
                NavUtils.toMainPage(SplashActivity.this);
                finish();
            }
            @Override
            public void onError(int code, String message) {
                ALog.d(TAG, "autoLogin failed code = " + code + " message = " + message);
                AuthorManager.INSTANCE.launchLogin(SplashActivity.this, Constants.MAIN_PAGE_ACTION, false);
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ALog.d(TAG, "onNewIntent: intent -> " + intent.getData());
        setIntent(intent);
    }


    @Override
    protected StatusBarConfig provideStatusBarConfig() {
        return new StatusBarConfig.Builder()
                .statusBarDarkFont(true)
                .fullScreen(true)
                .build();
    }
}
