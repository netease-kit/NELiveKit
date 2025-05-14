//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import UIKit

public let kScreenWidth: CGFloat = UIScreen.main.bounds.size.width
public let kScreenHeight: CGFloat = UIScreen.main.bounds.size.height

@objcMembers
public class NELiveStreamUI: NSObject {
  // MARK: - 图片资源加载

  public class func ne_livestream_imageName(_ imageName: String) -> UIImage? {
    let bundle = Bundle(for: self)
    if #available(iOS 13.0, *) {
      return UIImage(named: imageName, in: bundle, with: nil)
    } else {
      return UIImage(named: imageName, in: bundle, compatibleWith: nil)
    }
  }

  // MARK: - Bundle 资源路径

  public class var ne_livestream_sourceBundle: Bundle? {
    Bundle(for: self)
  }

  // MARK: - 界面布局相关

  /// 状态栏高度（动态适配全面屏）
  public class var ne_statusBarHeight: CGFloat {
    if #available(iOS 13.0, *) {
      guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let statusBarManager = windowScene.statusBarManager else {
        return 20.0 // 默认高度
      }
      return statusBarManager.statusBarFrame.height
    } else {
      return UIApplication.shared.statusBarFrame.height
    }
  }

  /// 通用边距
  public class var margin: CGFloat {
    30.0
  }

  /// 麦位水平间距
  public class var seatItemSpace: CGFloat {
    30.0
  }

  /// 麦位垂直间距
  public class var seatLineSpace: CGFloat {
    10.0
  }

  public static var bottomSafeHeight: CGFloat {
    getCurrentWindow()?.safeAreaInsets.bottom ?? 0
  }

  public static func getCurrentWindow() -> UIWindow? {
    if #available(iOS 13.0, *) {
      if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
        if let keyWindow = windowScene.windows.first {
          return keyWindow
        }
      }
    }
    return UIApplication.shared.windows.first
  }
}

// MARK: - 扩展

extension UIColor {
  convenience init(hex: Int, alpha: CGFloat = 1.0) {
    self.init(
      red: CGFloat((hex >> 16) & 0xFF) / 255.0,
      green: CGFloat((hex >> 8) & 0xFF) / 255.0,
      blue: CGFloat(hex & 0xFF) / 255.0,
      alpha: alpha
    )
  }
}
