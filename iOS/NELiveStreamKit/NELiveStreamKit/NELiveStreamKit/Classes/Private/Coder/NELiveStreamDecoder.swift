//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

enum NELiveStreamDecoder {
  static let tag: String = "NELiveStreamDecoder"

  // MARK: - ------------------------- 字典转模型 --------------------------

  static func decode<T>(_ type: T.Type,
                        param: [String: Any]) -> T? where T: Decodable {
    guard let jsonData = getJsonData(with: param) else {
      return nil
    }

    return decode(type, data: jsonData)
  }

  // MARK: - ------------------------- 字典数组 转 模型数组 --------------------------

  static func decode<T>(_: T.Type,
                        array: [[String: Any]]) -> [T]? where T: Decodable {
    guard let data = getJsonData(with: array) else {
      return nil
    }
    return decode([T].self, data: data)
  }

  // MARK: - ------------------------- json字符串 转 模型 --------------------------

  static func decode<T>(_: T.Type, jsonString: String) -> T? where T: Decodable {
    guard let json = jsonString.data(using: .utf8) else { return nil }
    return decode(T.self, data: json)
  }

  /// 转data
  static func getJsonData(with param: Any) -> Data? {
    guard JSONSerialization.isValidJSONObject(param) else {
      return nil
    }
    guard let data = try? JSONSerialization.data(withJSONObject: param, options: []) else {
      return nil
    }
    return data
  }

  static func decode<T>(_ type: T.Type, data: Data) -> T? where T: Decodable {
    var model: T?
    do {
      model = try JSONDecoder().decode(type, from: data)
    } catch let DecodingError.dataCorrupted(context) {
      NELiveStreamLog.errorLog(
        tag,
        desc: "Property data corrupted. \(context.debugDescription). Path: \(context.chainPath())"
      )
    } catch let DecodingError.keyNotFound(key, context) {
      NELiveStreamLog.errorLog(
        tag,
        desc: "Property name :\(key.stringValue) not fount. Path: \(context.chainPath())"
      )
    } catch let DecodingError.valueNotFound(value, context) {
      NELiveStreamLog.errorLog(
        tag,
        desc: "Value :\(value) not fount. Path: \(context.chainPath())"
      )
    } catch let DecodingError.typeMismatch(type, context) {
      NELiveStreamLog.errorLog(
        tag,
        desc: "Type '\(type)' mismatch. \(context.debugDescription). Path: \(context.chainPath())"
      )
    } catch {
      NELiveStreamLog.errorLog(tag, desc: "Failed to decode.")
    }
    return model
  }
}

extension DecodingError.Context {
  func chainPath() -> String {
    var path = ""
    for item in 0 ..< codingPath.count {
      let key = codingPath[item]
      if item == codingPath.count - 1 {
        path += key.stringValue
      } else {
        path += "\(key.stringValue) -> "
      }
    }
    return path
  }
}

extension String {
  // MARK: - ------------------------- 字符串转字典 --------------------------

  func toDictionary() -> [String: Any]? {
    let data = data(using: String.Encoding.utf8)
    guard let dict = try? JSONSerialization.jsonObject(
      with: data!,
      options: JSONSerialization.ReadingOptions.mutableContainers
    ) as? [String: Any] else {
      return nil
    }
    return dict
  }
}
