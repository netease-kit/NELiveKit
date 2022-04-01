package com.netease.yunxin.app.newlive.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netease.yunxin.app.newlive.Constants;
import com.netease.yunxin.app.newlive.R;
import com.netease.yunxin.app.newlive.list.LiveListAdapter;
import com.netease.yunxin.app.newlive.utils.ClickUtils;
import com.netease.yunxin.app.newlive.utils.NavUtils;
import com.netease.yunxin.app.newlive.utils.SpUtils;
import com.netease.yunxin.app.newlive.widget.FooterView;
import com.netease.yunxin.app.newlive.widget.HeaderView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.livekit.NELiveConstants;
import com.netease.yunxin.kit.livekit.NELiveKit;
import com.netease.yunxin.kit.livekit.NELiveCallback;
import com.netease.yunxin.kit.livekit.model.LiveInfo;
import com.netease.yunxin.kit.livekit.model.response.LiveListResponse;
import com.netease.yunxin.app.newlive.config.StatusBarConfig;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 直播列表页面
 */
public class LiveListActivity extends BaseActivity implements OnRefreshListener, OnLoadMoreListener {

    public final static String TAG = "LiveListActivity";
    public final static String KEY_PARAM_TITLE = "key_param_title";
    public final static String KEY_PARAM_TYPE  = "key_param_type";
    //每页大小
    public final static int PAGE_SIZE = 20;

    private RecyclerView recyclerView = null;
    private LinearLayout llyCreateLive = null;
    private SmartRefreshLayout refreshLayout = null;
    private RelativeLayout rlyEmpty = null;
    private LiveListAdapter liveListAdapter = null;
    private ImageView ivClose = null;
    private int type = NELiveConstants.LiveType.LIVE_TYPE_PK;
    //页码
    private boolean haveMore = false;
    //下一页请求页码
    private int nextPageNum = 1;

    private GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
//    private ILiveAudience liveAudience = new LiveAudienceImpl();
    private NELiveKit liveAnchor = NELiveKit.getInstance();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_list_activity_layout);
        paddingStatusBarHeight(R.id.rl_root);
        initView();
    }

    private void initView() {
        String title = getIntent().getStringExtra(KEY_PARAM_TITLE);
        type = getIntent().getIntExtra(KEY_PARAM_TYPE,NELiveConstants.LiveType.LIVE_TYPE_PK);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText((TextUtils.isEmpty(title)) ? getString(R.string.biz_live_pk_live) : title);
        recyclerView = findViewById(R.id.rcv_live);
        llyCreateLive = findViewById(R.id.lly_new_live);
        refreshLayout = findViewById(R.id.refreshLayout);
        ivClose = findViewById(R.id.iv_back);
        refreshLayout.setRefreshHeader(new HeaderView(this));
        refreshLayout.setRefreshFooter(new FooterView(this));
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);
        llyCreateLive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //todo 小窗适配
//                if (FloatPlayManager.isStartFloatWindow){
//                    FloatPlayManager.closeFloatPlay()
//                }
                if (!ClickUtils.INSTANCE.isFastClick()) {
                    if (type == NELiveConstants.LiveType.LIVE_TYPE_PK) {
                        NavUtils.toAnchorPkLivePage(LiveListActivity.this);
                    } else if (type == NELiveConstants.LiveType.LIVE_TYPE_SEAT) {
                        NavUtils.toAnchorSeatLivePage(LiveListActivity.this);
                    }
                }
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        liveListAdapter = new LiveListAdapter(this);
        liveListAdapter.setOnItemClickListener(new LiveListAdapter.OnItemClickListener() {


            public void onItemClick(ArrayList<LiveInfo> liveList, int position) {
                //goto audience page
                if (!ClickUtils.INSTANCE.isFastClick()) {
                    NavUtils.toAudiencePage(LiveListActivity.this, liveList, position);
                }
            }
        });

        recyclerView.addItemDecoration(new MyItemDecoration());
        gridLayoutManager.setSpanSizeLookup(new MySpanSizeLookup());
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(liveListAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData(){
        getLiveLists(true);
    }

    private void getLiveLists(boolean isRefresh){
        if(isRefresh){
            nextPageNum = 1;
        }


        liveAnchor.requestLiveList(type, 1, nextPageNum, PAGE_SIZE, new NELiveCallback<LiveListResponse>() {

            @Override
            public void onSuccess(LiveListResponse liveListResponse) {
                if(liveListResponse == null){
                    ALog.e(TAG, "requestLiveList onSuccess but liveListResponse == null");
                    return;
                }
                nextPageNum++;
                if (liveListAdapter != null) {
                    liveListAdapter.setDataList(
                            liveListResponse.getList(),
                            isRefresh
                        );
                }
                haveMore = liveListResponse.getHasNextPage();
                if (isRefresh) {
                    refreshLayout.finishRefresh(true);
                } else {
                    if (liveListResponse.getList() == null || liveListResponse.getList().size() == 0) {
                        refreshLayout.finishLoadMoreWithNoMoreData();
                    } else {
                        refreshLayout.finishLoadMore(true);
                    }
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                if (isRefresh) {
                    refreshLayout.finishRefresh(false);
                } else {
                    refreshLayout.finishLoadMore(false);
                }
            }
        });
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        if (!haveMore) {
            refreshLayout.finishLoadMoreWithNoMoreData();
        } else {
            getLiveLists(false);
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        nextPageNum = 1;
        getLiveLists(true);
    }

    @Override
    protected StatusBarConfig provideStatusBarConfig() {
        return new StatusBarConfig.Builder().statusBarDarkFont(false).statusBarColor(R.color.color_1a1a24).build();
    }



    class MyItemDecoration extends RecyclerView.ItemDecoration{

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            int pixel8 = SpUtils.INSTANCE.dp2pix(8f);
            int pixel4 = SpUtils.INSTANCE.dp2pix(4f);
            int position = parent.getChildAdapterPosition(view);
            int left;
            int right;
            if (position % 2 == 0) {
                left = pixel8;
                right = pixel4;
            } else {
                left = pixel4;
                right = pixel8;
            }
            outRect.set(left, pixel4, right, pixel4);
        }
    }

    class MySpanSizeLookup extends GridLayoutManager.SpanSizeLookup{

        @Override
        public int getSpanSize(int position) {
            // 如果是空布局，让它占满一行
            if (liveListAdapter.isEmptyPosition(position)) {
                return gridLayoutManager.getSpanCount();
            } else {
                return 1;
            }
        }
    }
}
