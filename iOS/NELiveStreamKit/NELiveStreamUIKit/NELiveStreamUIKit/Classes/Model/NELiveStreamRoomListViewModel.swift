//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NELiveStreamKit
import NESocialUIKit
import UIKit

@objcMembers public class NELiveStreamRoomListViewModel: NSObject {
  // 无网络
  public static let NO_NETWORK_ERROR = -1005
  // 无列表数据
  public static let EMPTY_LIST_ERROR = 1003

  public var datas: [NELiveStreamRoomInfo] = [] {
    didSet {
      datasChanged?(datas)
    }
  }

  public var isEnd: Bool = false
  public var isLoading: Bool = false {
    didSet {
      isLoadingChanged?(isLoading)
    }
  }

  public var error: NSError? {
    didSet {
      errorChanged?(error)
    }
  }

  public var pageNum: Int = 1
  public var pageSize: Int = 20

  public var datasChanged: (([NELiveStreamRoomInfo]) -> Void)?
  public var isLoadingChanged: ((Bool) -> Void)?
  public var errorChanged: ((NSError?) -> Void)?
  public var liveType: NELiveStreamLiveRoomType = .liveStream

  override public init() {
    super.init()
    try? reachability?.startNotifier()
  }

  public lazy var reachability: NESocialReachability? = {
    let reachability = try? NESocialReachability(hostname: "163.com")
    return reachability
  }()

  func checkNetwork() -> Bool {
    if reachability?.connection == .cellular || reachability?.connection == .wifi {
      return true
    }
    return false
  }

  public func requestNewData() {
    if !checkNetwork() {
      isLoading = false
      error = NSError(domain: NSCocoaErrorDomain, code: NELiveStreamRoomListViewModel.NO_NETWORK_ERROR)
      return
    }
    isLoading = true
    NELiveStreamKit.getInstance().getRoomList(.live, type: liveType.rawValue, pageNum: pageNum, pageSize: pageSize) { [weak self] code, msg, data in
      DispatchQueue.main.async {
        if code != 0 {
          self?.datas = []
          self?.error = NSError(domain: NSCocoaErrorDomain, code: code, userInfo: [NSLocalizedDescriptionKey: msg ?? ""])
          self?.isEnd = true
        } else if let data = data {
          self?.datas = data.list ?? []
          self?.error = nil
          self?.isEnd = (data.list?.count ?? 0 < self?.pageSize ?? 0)
        }
        self?.isLoading = false
      }
    }
  }

  public func requestMoreData() {
    if !checkNetwork() {
      isLoading = false
      error = NSError(domain: NSCocoaErrorDomain, code: NELiveStreamRoomListViewModel.NO_NETWORK_ERROR)
      return
    }
    if isEnd {
      return
    }

    isLoading = true
    pageNum += 1
    NELiveStreamKit.getInstance().getRoomList(.live, type: liveType.rawValue, pageNum: pageNum, pageSize: pageSize) { [weak self] code, msg, data in
      DispatchQueue.main.async {
        if code != 0 {
          self?.datas = []
          self?.error = NSError(domain: NSCocoaErrorDomain, code: code, userInfo: [NSLocalizedDescriptionKey: msg ?? ""])
          self?.isEnd = true
        } else if let data = data {
          var temp = self?.datas ?? []
          temp.append(contentsOf: data.list ?? [])
          self?.datas = temp
          self?.error = nil
          self?.isEnd = (data.list?.count ?? 0 < self?.pageSize ?? 0)
        }
        self?.isLoading = false
      }
    }
  }
}
