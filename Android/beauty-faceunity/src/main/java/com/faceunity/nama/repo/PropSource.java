// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.repo;

import com.faceunity.nama.entity.PropBean;
import com.netease.yunxin.kit.beauty.faceunity.R;
import java.util.ArrayList;

/** DESC：道具数据构造 Created on 2021/3/28 */
public class PropSource {

  /**
   * 构造贴纸列表
   *
   * @return
   */
  public static ArrayList<PropBean> buildPropBeans() {
    ArrayList<PropBean> propBeans = new ArrayList<>();
    propBeans.add(new PropBean(R.mipmap.icon_control_delete_all, null));
    propBeans.add(new PropBean(R.mipmap.icon_sticker_sdlu, "sticker/sdlu.bundle"));
    propBeans.add(new PropBean(R.mipmap.icon_sticker_fashi, "sticker/fashi.bundle"));
    return propBeans;
  }
}
