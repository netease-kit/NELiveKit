// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';
import 'package:netease_livekit/netease_livekit.dart';

typedef LoadDataCallback = void Function(
    List<NELiveDetail> liveInfoList, bool isRefresh, int value);

mixin LiveListDataMixin {
  List<NELiveDetail> liveList = [];
  static const int pageSize = 20;
  int nextPageNum = 1;
  bool haveMore = false;

  void setDataList(List<NELiveDetail> liveInfoList, bool isRefresh) {
    if (isRefresh) {
      liveList.clear();
    }
    if (liveInfoList.isNotEmpty && liveInfoList.isNotEmpty) {
      liveList.addAll(liveInfoList);
    }
  }

  void getLiveLists(bool isRefresh, LoadDataCallback callback) {
    if (isRefresh) {
      nextPageNum = 1;
    }
    NELiveKit.instance
        .fetchLiveList(
            nextPageNum, pageSize, NELiveStatus.living, NELiveRoomType.pkLiveEx)
        .then((value) {
      if (kDebugMode) {
        print('fetchLiveList  ====> ${value.toString()} ');
      }
      if (value.code == 0) {
        nextPageNum++;
        NELiveList? liveList = value.data;
        if (liveList?.hasNextPage != null) {
          haveMore = liveList!.hasNextPage;
        }
        if (liveList?.list != null) {
          callback(liveList!.list!, isRefresh, 0);
        }
      } else {
        callback([], isRefresh, value.code);
      }
    });
  }
}
