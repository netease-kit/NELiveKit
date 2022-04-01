package com.netease.yunxin.app.newlive;


public class Constants {
    public final static String MAIN_PAGE_ACTION = "https://netease.yunxin.newlive.home";

    /**
     * 隐私协议链接
     */
    public final static String URL_PRIVACY = "https://yunxin.163.com/clauses?serviceType=3";

    /**
     * 用户政策链接
     */
    public final static String URL_USER_POLICE = "https://yunxin.163.com/clauses";

    /**
     * 免费试用
     */
    public final static String URL_FREE_TRAIL =
            "https://id.163yun.com/register?h=media&t=media&clueFrom=nim&referrer=https%3A%2F%2Fapp.yunxin.163.com%2F";

    /**
     * 免责声明
     */
    public final static String URL_DISCLAIMER = "file:///android_asset/disclaimer.html";

    public class  PushType{
        public final static int PUSH_TYPE_CDN = 0;
        public final static int PUSH_TYPE_RTC = 1;
    }

    public final static String TYPE_PK = "PK";

    public class StreamLayout {
        //signal live stream layout
        public final static int SIGNAL_HOST_LIVE_WIDTH = 720;
        public final static int SIGNAL_HOST_LIVE_HEIGHT = 1280;

        //pk live stream layout
        public final static int PK_LIVE_WIDTH = 360;
        public final static int PK_LIVE_HEIGHT = 640;
        public final static float WH_RATIO_PK = PK_LIVE_WIDTH * 2f / PK_LIVE_HEIGHT;

        //mutil seat live stream
        //麦位宽度
        public final static int AUDIENCE_LINKED_WIDTH = 132;

        //麦位高度
        public final static int AUDIENCE_LINKED_HEIGHT = 170;

        //观众麦位距离左侧
        public final static int AUDIENCE_LINKED_LEFT_MARGIN = 575;

        //观众麦位距离顶部
        public final static int AUDIENCE_LINKED_FIRST_TOP_MARGIN = 200;

        //观众麦位之间距离
        public final static int AUDIENCE_LINKED_BETWEEN_MARGIN = 12;
    }
}
