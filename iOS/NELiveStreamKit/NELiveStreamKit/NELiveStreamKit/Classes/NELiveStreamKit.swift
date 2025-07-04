// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

let kitTag = "NELiveStreamKit"

@objcMembers public class NELiveStreamKit: NSObject {
  // MARK: - Public Properties

  /// 单例访问方法
  public static func getInstance() -> NELiveStreamKit {
    instance
  }

  /// 初始化状态
  public var isInitialized: Bool = false

  /// Kit配置信息
  public var config: NELiveStreamKitConfig?

  /// 本端成员信息
  /// 加入房间后获取
  public var localMember: NELiveStreamMember? {
    Judge.syncResult {
      NELiveStreamMember(self.roomContext!.localMember)
    }
  }

  /// 所有成员信息(包含本端)
  /// 加入房间后获取
  public var allMemberList: [NELiveStreamMember] {
    Judge.syncResult {
      var allMembers = [NERoomMember]()
      allMembers.append(self.roomContext!.localMember)
      self.roomContext!.remoteMembers.forEach { allMembers.append($0) }
      return allMembers.map { NELiveStreamMember($0) }
    } ?? []
  }

  /// 上次收到服务器下发的麦位列表
  public var localSeats: [NELiveStreamSeatItem]?

  /// 连麦管理器
  public var coHostManager: NECoHostManager { _coHostManager }

  // MARK: - Internal Properties

  /// 房间服务
  var roomService: NELiveStreamRoomService { _roomService }

  /// 维护房间上下文
  var roomContext: NERoomContext?

  // MARK: - Private Properties

  /// 单例
  private static let instance = NELiveStreamKit()

  /// 是否调试模式
  var isDebug: Bool = false

  /// 是否出海
  var isOversea: Bool = false

  // 房间监听器数组
  var listeners = NSPointerArray.weakObjects()

  /// 自己操作后的mute状态，区别于ban之后的mute
  var isSelfMuted: Bool = true

  /// 登录监听器数组
  var authListeners = NSPointerArray.weakObjects()

  /// 直播信息
  var liveInfo: NELiveStreamRoomInfo?

  /// 直播信息
  var liveRecordId: Int = 0

  /// 房间服务实例
  var _roomService = NELiveStreamRoomService()

  /// 连麦管理器
  var _coHostManager = NECoHostManager()

  var useNewSeatCallback: Bool = false

  // MARK: - Initialization

  override private init() {
    super.init()
  }

  deinit {
    // 清理资源
  }

  // MARK: - Public Methods

  /// NELiveStreamKit 初始化
  /// - Parameters:
  ///   - config: 初始化配置
  ///   - callback: 回调
  public func initialize(config: NELiveStreamKitConfig,
                         callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.setUp(config.appKey)
    NELiveStreamLog.apiLog(kitTag, desc: "Initialize")
    self.config = config

    configureAndInitializeRoomKit(with: config, callback: callback)
  }

  /// 调整录音音量
  public func adjustRecordingSignalVolume(_ volume: UInt32) -> Int {
    // 实现调整录音音量逻辑
    0
  }

  /// 添加直播流监听器
  public func addLiveStreamListener(_ listener: NELiveStreamListener) {
    NELiveStreamLog.apiLog(kitTag, desc: "Add LiveStream listener.")
    listeners.addWeakObject(listener)
  }

  /// 移除直播流监听器
  public func removeLiveStreamListener(_ listener: NELiveStreamListener) {
    NELiveStreamLog.apiLog(kitTag, desc: "Remove LiveStream listener.")
    listeners.removeWeakObject(listener)
  }

  /// 添加连麦监听器
  public func addCoHostListener(_ listener: NECoHostListener) {
    NELiveStreamLog.apiLog(kitTag, desc: "Add CoHost listener.")
    coHostManager.addListener(listener)
  }

  /// 移除连麦监听器
  public func removeCoHostListener(_ listener: NECoHostListener) {
    NELiveStreamLog.apiLog(kitTag, desc: "Remove CoHost listener.")
    coHostManager.removeListener(listener)
  }

  // MARK: - Private Methods

  private func configureAndInitializeRoomKit(with config: NELiveStreamKitConfig,
                                             callback: NELiveStreamCallback<AnyObject>?) {
    /// 非私有化 且要出海 使用默认海外环境
    var overseaAndNotPrivte = false

    if let baseUrl = config.extras["baseUrl"] {
      NE.config.customUrl = baseUrl
    }
    if let serverUrl = config.extras["serverUrl"] {
      isDebug = serverUrl == "test"
      isOversea = serverUrl == "oversea"
      if !serverUrl.contains("http"), isOversea {
        overseaAndNotPrivte = true
        config.extras["serverUrl"] = "https://roomkit-sg.netease.im"
      }
    }

    NE.config.isDebug = isDebug
    NE.config.isOverSea = isOversea

    let options = NERoomKitOptions(appKey: config.appKey)
    options.APNSCerName = config.APNSCerName
    options.extras = config.extras

    if overseaAndNotPrivte {
      configureOverseaServer(options)
    }

    NERoomKit.shared().initialize(options: options) { [weak self] code, str, _ in
      guard let self = self else { return }
      if code == 0 {
        self.isInitialized = true
        NELiveStreamLog.successLog(kitTag, desc: "Successfully initialize.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to initialize. Code: \(code)")
      }
      callback?(code, str, nil)
    }
  }

  private func configureOverseaServer(_ options: NERoomKitOptions) {
    let serverConfig = NEServerConfig()
    serverConfig.imServerConfig = NEIMServerConfig()
    serverConfig.roomKitServerConfig = NERoomKitServerConfig()
    serverConfig.roomKitServerConfig?.roomServer = "https://roomkit-sg.netease.im"
    serverConfig.imServerConfig?.lbs = "https://lbs.netease.im/lbs/conf.jsp"
    serverConfig.imServerConfig?.link = "link-sg.netease.im:7000"
    options.serverConfig = serverConfig
  }
}
