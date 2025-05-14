//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

extension Dictionary {
  var prettyJSON: String {
    do {
      let jsonData = try JSONSerialization.data(
        withJSONObject: self,
        options: JSONSerialization.WritingOptions(rawValue: 0)
      )
      guard let jsonString = String(data: jsonData, encoding: String.Encoding.utf8) else {
        print("Can't create string with data.")
        return "{}"
      }
      return jsonString
    } catch let parseError {
      print("json serialization error: \(parseError)")
      return "{}"
    }
  }
}
