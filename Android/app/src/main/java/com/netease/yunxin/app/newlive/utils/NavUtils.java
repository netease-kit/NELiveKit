package com.netease.yunxin.app.newlive.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.netease.yunxin.app.newlive.audience.ui.LiveAudienceActivity;
import com.netease.yunxin.app.newlive.MainActivity;
import com.netease.yunxin.app.newlive.activity.AnchorPkLiveActivity;
import com.netease.yunxin.app.newlive.activity.LiveListActivity;
import com.netease.yunxin.app.newlive.user.AppAboutActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.app.newlive.user.UserInfoActivity;
import com.netease.yunxin.kit.livekit.model.LiveInfo;

import java.util.ArrayList;
import java.util.List;

public class NavUtils {

    private final static String TAG = "NavUtil";

    public static void toMainPage(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void toBrowsePage(Context context, String title, String url){

    }

    public static void toUserInfoPage(Context context){
        if(context == null){
            ALog.d(TAG, "toUserInfoPage but context == null");
            return;
        }
        context.startActivity(new Intent(context, UserInfoActivity.class));
    }

    public static void toAppAboutPage(Context context){
        if(context == null){
            ALog.d(TAG, "toAppAboutPage but context == null");
            return;
        }
        context.startActivity(new Intent(context, AppAboutActivity.class));
    }

    public static void toLiveListPage(Context context, String title, int type){
        if(context == null){
            ALog.d(TAG, "toLiveListPage but context == null");
            return;
        }
        Intent intent = new Intent(context, LiveListActivity.class);
        intent.putExtra(LiveListActivity.KEY_PARAM_TITLE, title);
        intent.putExtra(LiveListActivity.KEY_PARAM_TYPE,type);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toAnchorPkLivePage(Context context){
        if(context == null){
            ALog.d(TAG, "toAnchorPkLivePage but context == null");
            return;
        }
        context.startActivity(new Intent(context, AnchorPkLiveActivity.class));
    }

    public static void toAnchorSeatLivePage(Context context){
//        AnchorSeatLiveActivity.startActivity(this);
    }

    public static void toAudiencePage(Context context, ArrayList<LiveInfo> liveList, int position){
        LiveAudienceActivity.launchAudiencePage(context, liveList, position);
    }

}
