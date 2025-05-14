// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit

/// UI 屏幕适配工具类

public enum NEUIScreenAdapter {
  /// 屏幕宽度
  public static var screenWidth: CGFloat {
    UIScreen.main.bounds.size.width
  }

  /// 屏幕高度
  public static var screenHeight: CGFloat {
    UIScreen.main.bounds.size.height
  }

  /// 宽度适配（基于 375 设计稿）
  /// - Parameter x: 设计稿尺寸
  /// - Returns: 适配后的宽度
  public static func width(_ x: CGFloat) -> CGFloat {
    x * screenWidth / 375.0
  }

  /// 高度适配（基于 667 设计稿）
  /// - Parameter x: 设计稿尺寸
  /// - Returns: 适配后的高度
  public static func height(_ x: CGFloat) -> CGFloat {
    x * screenHeight / 667.0
  }
}

// MARK: - 便捷方法

public extension CGFloat {
  /// 宽度适配
  var w: CGFloat {
    NEUIScreenAdapter.width(self)
  }

  /// 高度适配
  var h: CGFloat {
    NEUIScreenAdapter.height(self)
  }
}

public extension Int {
  /// 宽度适配
  var w: CGFloat {
    NEUIScreenAdapter.width(CGFloat(self))
  }

  /// 高度适配
  var h: CGFloat {
    NEUIScreenAdapter.height(CGFloat(self))
  }
}

public extension Double {
  /// 宽度适配
  var w: CGFloat {
    NEUIScreenAdapter.width(CGFloat(self))
  }

  /// 高度适配
  var h: CGFloat {
    NEUIScreenAdapter.height(CGFloat(self))
  }
}
