package com.netease.yunxin.app.newlive.activity;

import android.os.Bundle;
import android.view.View;

import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.newlive.Constants;
import com.netease.yunxin.app.newlive.config.StatusBarConfig;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity  extends AppCompatActivity {

    private LoginObserver<LoginEvent> loginObserver = new LoginObserver<LoginEvent>() {

        @Override
        public void onEvent(LoginEvent event) {
            if (event.getEventType() == EventType.TYPE_LOGOUT && !ignoredLoginEvent()){
                finish();
                onKickOut();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthorManager.INSTANCE.registerLoginObserver(loginObserver);
        StatusBarConfig config = provideStatusBarConfig();
        if (config != null) {
            ImmersionBar bar = ImmersionBar.with(this)
                                  .statusBarDarkFont(config.isDarkFont())
                                  .statusBarColor(config.getBarColor());
            if (config.isFits()) {
                bar.fitsSystemWindows(true);
            }
            if (config.isFullScreen()) {
                bar.fullScreen(true);
            }
            bar.init();
        }
    }

    @Override
    protected void onDestroy() {
        AuthorManager.INSTANCE.unregisterLoginObserver(loginObserver);
        super.onDestroy();
    }

    protected StatusBarConfig provideStatusBarConfig() {
        return null;
    }

    protected boolean ignoredLoginEvent(){
        return false;
    }

    protected void paddingStatusBarHeight(View view){
        StatusBarConfig.paddingStatusBarHeight(this, view);
    }

    protected void paddingStatusBarHeight(@IdRes int rootViewId){
        paddingStatusBarHeight(findViewById(rootViewId));
    }

    protected void onKickOut(){}

}
