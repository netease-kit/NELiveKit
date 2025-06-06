// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.faceunity.nama.base.BaseDelegate;
import com.faceunity.nama.base.BaseListAdapter;
import com.faceunity.nama.base.BaseViewHolder;
import com.faceunity.nama.entity.PropBean;
import com.faceunity.nama.infe.AbstractPropDataFactory;
import com.netease.yunxin.kit.beauty.faceunity.R;
import java.util.ArrayList;

/** DESC： Created on 2021/4/26 */
public class PropControlView extends BaseControlView {

  private AbstractPropDataFactory mDataFactory;
  private BaseListAdapter<PropBean> mPropAdapter;

  private RecyclerView recyclerView;

  public PropControlView(@NonNull Context context) {
    super(context);
    init();
  }

  public PropControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PropControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  // region  init

  private void init() {
    LayoutInflater.from(mContext).inflate(R.layout.layout_effect_control, this);
    initView();
    initAdapter();
  }

  /**
   * 给控制绑定 EffectController，数据工厂
   *
   * @param dataFactory IFaceBeautyDataFactory
   */
  public void bindDataFactory(AbstractPropDataFactory dataFactory) {
    mDataFactory = dataFactory;
    mPropAdapter.setData(dataFactory.getPropBeans());
  }

  /** View初始化 */
  private void initView() {
    recyclerView = findViewById(R.id.recycler_view);
    initHorizontalRecycleView(recyclerView);
  }

  /** Adapter初始化 */
  private void initAdapter() {
    mPropAdapter =
        new BaseListAdapter<>(
            new ArrayList<>(),
            new BaseDelegate<PropBean>() {
              @Override
              public void convert(
                  int viewType, BaseViewHolder helper, PropBean data, int position) {
                helper.setImageResource(R.id.iv_control, data.getIconId());
                helper.itemView.setSelected(position == mDataFactory.getCurrentPropIndex());
              }

              @Override
              public void onItemClickListener(View view, PropBean data, int position) {
                if (mDataFactory.getCurrentPropIndex() != position) {
                  changeAdapterSelected(mPropAdapter, mDataFactory.getCurrentPropIndex(), position);
                  mDataFactory.setCurrentPropIndex(position);
                  mDataFactory.onItemSelected(data);
                }
              }
            },
            R.layout.list_item_control_image_circle);
    recyclerView.setAdapter(mPropAdapter);
  }

  // endregion
}
