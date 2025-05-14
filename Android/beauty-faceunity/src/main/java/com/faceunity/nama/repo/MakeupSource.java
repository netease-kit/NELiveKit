// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.repo;

import com.faceunity.nama.entity.MakeupCombinationBean;
import com.netease.yunxin.kit.beauty.faceunity.R;
import java.io.File;
import java.util.ArrayList;

/** DESC：美妆数据构造 Created on 2021/3/28 */
public class MakeupSource {
  public static String BUNDLE_FACE_MAKEUP = "graphics" + File.separator + "face_makeup.bundle";

  //region 组合妆容

  /**
   * 构造美妆组合妆容配置
   *
   * @return ArrayList<MakeupCombinationBean>
   */
  public static ArrayList<MakeupCombinationBean> buildCombinations() {
    ArrayList<MakeupCombinationBean> combinations = new ArrayList<MakeupCombinationBean>();
    combinations.add(
        new MakeupCombinationBean(
            "origin", R.mipmap.icon_control_none, R.string.makeup_radio_remove, null));
    combinations.add(
        new MakeupCombinationBean(
            "naicha",
            R.mipmap.icon_makeup_combination_tea_with_milk,
            R.string.makeup_combination_naicha,
            "makeup/naicha.bundle"));
    combinations.add(
        new MakeupCombinationBean(
            "dousha",
            R.mipmap.icon_makeup_combination_red_bean_paste,
            R.string.makeup_combination_dousha,
            "makeup/dousha.bundle"));
    combinations.add(
        new MakeupCombinationBean(
            "chaoa",
            R.mipmap.icon_makeup_combination_super_a,
            R.string.makeup_combination_chaoa,
            "makeup/chaoa.bundle"));
    return combinations;
  }

  //endregion
}
