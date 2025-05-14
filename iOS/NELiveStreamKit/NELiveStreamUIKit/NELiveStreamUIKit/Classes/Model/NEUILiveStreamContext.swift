//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NELiveStreamKit

private let kEarbackVolume: UInt32 = 80

@objcMembers
public class NEUIRtcConfig: NSObject {
  /// 麦克风开关
  public dynamic var micOn: Bool = true

  /// 扬声器开关
  public dynamic var speakerOn: Bool = true

  /// 效果音量
  public dynamic var effectVolume: UInt32 = 100

  /// 伴音音量
  public dynamic var audioMixingVolume: UInt32 = 100

  /// 人声（采集音量）
  public dynamic var audioRecordVolume: UInt32 = 100 {
    didSet {
      let code = NELiveStreamKit.getInstance().adjustRecordingSignalVolume(audioRecordVolume)
      if code != 0 {
        audioRecordVolume = oldValue
        return
      }
    }
  }

  override public init() {
    super.init()
  }
}

@objcMembers
public class NEUILiveStreamContext: NSObject {
  /// 用户角色
  public dynamic var role: NELiveStreamRole = .audience

  /// 麦位信息
  public dynamic var seatInfo: NELiveStreamSeatInfo?

  /// 是否全部禁言
  public dynamic var isMuteAll: Bool = false

  /// 自己是否禁言
  public dynamic var meIsMute: Bool = false

  /// 是否被语音屏蔽
  public dynamic var isMasked: Bool = false

  /// 所有声音关闭（主播）
  public dynamic var isAllSoundMute: Bool = false

  /// 当前背景音乐
  public dynamic var currentBgm: NEUIBackgroundMusicModel?

  /// 当前背景乐是否暂停
  public dynamic var isBackgroundMusicPaused: Bool = false

  /// rtc 配置
  public dynamic var rtcConfig: NEUIRtcConfig

  override public init() {
    rtcConfig = NEUIRtcConfig()
    super.init()
    NELiveStreamKit.getInstance().addLiveStreamListener(self)
  }

  deinit {
    NELiveStreamKit.getInstance().removeLiveStreamListener(self)
  }
}

// MARK: - NELiveStreamListener

extension NEUILiveStreamContext: NELiveStreamListener {
  public func onMemberAudioMuteChanged(_ member: NELiveStreamMember,
                                       mute: Bool,
                                       operateBy: NELiveStreamMember?) {
    if member.account == NELiveStreamKit.getInstance().localMember?.account {
      rtcConfig.micOn = !mute
    }
  }
}
