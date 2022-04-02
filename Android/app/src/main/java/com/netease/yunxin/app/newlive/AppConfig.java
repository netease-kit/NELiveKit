package com.netease.yunxin.app.newlive;

public class AppConfig {
    private final static String APP_KEY = ********your appkey**********;
    private final static String APP_BASE_URL = "http://yiyong-ne-live.netease.im";
    private final static int PARENT_SCOPE = 5;
    private final static int SCOPE = 3;

    public static String getAppKey(){
        return APP_KEY;
    }

    public static String getAppBaseUrl(){
        return APP_BASE_URL;
    }

    public static int getParentScope(){
        return PARENT_SCOPE;
    }

    public static int getScope(){
        return SCOPE;
    }

}
