// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc public extension NSObject {
  /// 判断对象是否为空
  /// - Parameter object: 要判断的对象
  /// - Returns: 如果对象为 nil、NSNull、空字符串或 0，返回 true；否则返回 false
  static func isNullOrNilWithObject(_ object: Any?) -> Bool {
    guard let object = object else {
      return true
    }

    if object is NSNull {
      return true
    }

    if let string = object as? String {
      let trimmedString = string.replacingOccurrences(of: " ", with: "")
      return trimmedString.isEmpty
    }

    if let number = object as? NSNumber {
      return number == 0
    }

    return false
  }
}
