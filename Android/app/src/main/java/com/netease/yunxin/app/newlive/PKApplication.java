package com.netease.yunxin.app.newlive;

import android.app.Application;

import com.beautyFaceunity.FURenderer;
import com.beautyFaceunity.utils.FileUtils;
import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.neliveplayer.sdk.model.NESDKConfig;
import com.netease.yunxin.android.lib.network.common.NetworkClient;
import com.netease.yunxin.app.newlive.live.LiveKitManager;
import com.netease.yunxin.app.newlive.live.MyLiveListener;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.livekit.NELiveCallback;
import com.netease.yunxin.kit.livekit.NELiveKitOptions;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.AuthorConfig;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;
import com.netease.yunxin.kit.login.model.LoginType;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import kotlin.Unit;

public class PKApplication extends Application {

    private final static String TAG = "PKApplication";
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        wrapperUncaughtExceptionHandler();
        application = this;
        //初始化登录模块
        AuthorConfig authorConfig = new AuthorConfig(AppConfig.getAppKey(), AppConfig.getParentScope(), AppConfig.getScope(), !AppConfig.isOnline);
        authorConfig.setLoginType(LoginType.LANGUAGE_SWITCH);
        AuthorManager.INSTANCE.initAuthor(getApplicationContext(), authorConfig);
        AuthorManager.INSTANCE.registerLoginObserver(new LoginObserver<LoginEvent>() {

            @Override
            public void onEvent(LoginEvent loginEvent) {
                ALog.d(TAG, "LoginObserver loginEvent = " + loginEvent.getEventType() + " userInfo = "
                            + (loginEvent.getUserInfo() == null ? "" : loginEvent.getUserInfo().toJson()));
                if(loginEvent.getEventType() == EventType.TYPE_LOGIN){
                    LiveKitManager.getInstance().login(
                            loginEvent.getUserInfo().getAccountId(),
                            loginEvent.getUserInfo().getAccessToken(),
                            new NELiveCallback<Unit>() {

                                @Override
                                public void onSuccess(Unit unit) {
                                    ALog.d(TAG, "LiveKit login success");
                                }
                                @Override
                                public void onFailure(int code, String msg) {
                                    ALog.d(TAG, "LiveKit login failed code = "  + code + ", msg = " + msg);
                                }
                            });
                }else if(loginEvent.getEventType() == EventType.TYPE_LOGOUT){
                    LiveKitManager.getInstance().logout(new NELiveCallback<Unit>() {

                        @Override
                        public void onSuccess(Unit unit) {
                            ALog.d(TAG, "LiveKit logout success");
                        }
                        @Override
                        public void onFailure(int code, String msg) {
                            ALog.d(TAG, "LiveKit logout failed code = " + code + " msg = " + msg);
                        }
                    });
                }
            }
        });
        //初始化应用网络
        NetworkClient.getInstance()
                     .configBaseUrl(AppConfig.getAppBaseUrl())
                     .appKey(AppConfig.getAppKey())
                     .configDebuggable(true);

        String language = getResources().getConfiguration().locale.getLanguage();
        if (!language.contains("zh")) {
            NetworkClient.getInstance().configLanguage("en");
        }
        //初始化livekit
        Map<String, String> extras = new HashMap<String, String>();
        if(!AppConfig.isOnline) {
            extras.put("serverUrl", "test");
        }
        LiveKitManager.getInstance().init(this, new NELiveKitOptions(AppConfig.getAppKey(), extras), null);
        LiveKitManager.getInstance().addLiveListener(new MyLiveListener(){

            @Override
            public void onLoginKickedOut() {
                AuthorManager.INSTANCE.logout(new LoginCallback<Void>() {

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        ALog.d(TAG, "logout success");
                    }
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        ALog.d(TAG, "logout failed code = " + errorCode + ", msg = " + errorMsg);
                    }
                });
            }
        });

        //初始化播放器
        NESDKConfig config = new NESDKConfig();
        config.dataUploadListener = new NELivePlayer.OnDataUploadListener() {

            @Override
            public boolean onDataUpload(String url, String data) {
                ALog.d(TAG, "stream url is " + url + ", detail data is " + data);
                return true;
            }
            @Override
            public boolean onDocumentUpload(String url, Map<String, String> params, Map<String, String> filepaths) {
                return true;
            }
        };
        NELivePlayer.init(application, config);
        //初始化美颜
        initFaceunity();
    }

    // 美颜
    private void initFaceunity() {
        FURenderer.initFURenderer(this);
        // 异步拷贝 assets 资源
        new Thread(new Runnable() {

            @Override
            public void run() {
                FileUtils.copyAssetsChangeFaceTemplate(PKApplication.this);
            }
        }).start();
    }

    public static Application getApplication() {
        return application;
    }

    private void wrapperUncaughtExceptionHandler() {
        ALog.d(TAG, "wrapperUncaughtExceptionHandler");
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (exceptionHandler instanceof InnerExceptionHandler) {
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(new InnerExceptionHandler(exceptionHandler));
    }

    private static class InnerExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler exceptionHandler;
        public InnerExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            ALog.e(TAG, "ThreadName is " + Thread.currentThread().getName() + ", pid is " + android.os.Process.myPid() + " tid is " + android.os.Process.myTid(), e);
            this.exceptionHandler.uncaughtException(t, e);
        }
    }
}
