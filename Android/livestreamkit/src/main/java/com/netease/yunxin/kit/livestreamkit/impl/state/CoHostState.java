// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.impl.state;

import com.netease.yunxin.kit.common.utils.livedata.*;
import com.netease.yunxin.kit.livestreamkit.impl.model.*;
import java.util.ArrayList;
import java.util.List;

public class CoHostState {
  public final LiveData<List<ConnectionUser>> connectedUserList = new LiveData<>(new ArrayList<>());

  public void reset() {
    connectedUserList.get().clear();
  }
}
