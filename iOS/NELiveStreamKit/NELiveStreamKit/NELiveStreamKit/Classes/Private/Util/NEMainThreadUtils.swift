// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 在主线程同步安全执行 block
public func mainSyncSafe(_ block: @escaping () -> Void) {
  if Thread.isMainThread {
    block()
  } else {
    DispatchQueue.main.sync(execute: block)
  }
}

/// 在主线程异步安全执行 block
public func mainAsyncSafe(_ block: @escaping () -> Void) {
  if Thread.isMainThread {
    block()
  } else {
    DispatchQueue.main.async(execute: block)
  }
}
