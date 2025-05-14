//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers public class NELiveStreamBundle: NSObject {
  public static func bundle() -> Bundle {
    Bundle(for: NELiveStreamBundle.self)
  }

  public static func localized(_ key: String, value: String? = nil) -> String {
    var language = "en"
    if let preferred = NSLocale.preferredLanguages.first,
       preferred.hasPrefix("zh-Hans") {
      language = "zh-Hans"
    }
    if let path = bundle().path(forResource: language, ofType: "lproj"),
       let bundle = Bundle(path: path) {
      return bundle.localizedString(forKey: key, value: value, table: nil)
    }
    return key
  }
}
