package com.netease.yunxin.app.newlive;

import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.netease.yunxin.android.lib.network.common.NetworkClient;
import com.netease.yunxin.app.newlive.pager.MainPagerAdapter;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.app.newlive.activity.BaseActivity;
import com.netease.yunxin.app.newlive.config.StatusBarConfig;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(AuthorManager.INSTANCE.getUserInfo() != null) {
            NetworkClient.getInstance().configAccessToken(AuthorManager.INSTANCE.getUserInfo().getAccessToken());
        }

        ViewPager mainPager = findViewById(R.id.vp_fragment);
        mainPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        mainPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = findViewById(R.id.tl_tab);
        tabLayout.setupWithViewPager(mainPager);
        tabLayout.removeAllTabs();
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setSelectedTabIndicator(null);
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.view_item_home_tab_app), 0, true);
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.view_item_home_tab_user), 1, false);
        mainPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab item = tabLayout.getTabAt(position);
                if(item != null) {
                    item.select();
                }
                super.onPageSelected(position);
            }
        });

        AuthorManager.INSTANCE.registerLoginObserver(new LoginObserver<LoginEvent>() {

            @Override
            public void onEvent(LoginEvent loginEvent) {
                ALog.d(TAG, "loginEvent = " + loginEvent.getEventType());
                if(loginEvent.getEventType() == EventType.TYPE_LOGOUT){
                    //todo 小窗适配

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ALog.flush(true);
    }

    @Override
    protected StatusBarConfig provideStatusBarConfig() {
        return new StatusBarConfig.Builder().statusBarDarkFont(false).build();
    }

    @Override
    protected void onKickOut() {
        AuthorManager.INSTANCE.launchLogin(MainActivity.this, Constants.MAIN_PAGE_ACTION, false);
    }
}