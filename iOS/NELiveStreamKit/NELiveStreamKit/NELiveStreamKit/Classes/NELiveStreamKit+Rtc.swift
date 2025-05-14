// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import AVFAudio
import Foundation
import NERoomKit

/// rtc 扩展
public extension NELiveStreamKit {
  /// 关闭自己麦克风
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
  /// - Parameter callback: 回调
  func muteMyAudio(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    internalMute(callback: callback)
  }

  /// 关闭自己的麦克风
  /// - Parameters:
  ///   - bySelf: 是否是主观操作，为了区分ban之后的关闭操作
  ///   - callback: 回调
  internal func internalMute(bySelf: Bool = true, callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Mute mu audio.")
    Judge.preCondition({
      guard let local = self.localMember?.account else {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to mute my audio. Msg: Can't find LocalMember."
        )
        callback?(NELiveStreamErrorCode.failed, "Can't find LocalMember", nil)
        return
      }

      self.roomContext?.rtcController.muteMyAudio(enableMediaPub: true, callback: { code, msg, obj in
        var res = code
        if res != 0 {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to mute mu audio. Code: \(res). Msg: \(msg ?? "")"
          )
        }
        callback?(res, msg, nil)
      })
    }, failure: callback)
  }

  /// 打开自己麦克风
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
  /// - Parameter callback: 回调
  func unmuteMyAudio(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Unmute my audio.")
    Judge.preCondition({
      guard let local = self.localMember?.account else {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to unmute my audio. Msg: Can't find LocalMember."
        )
        callback?(NELiveStreamErrorCode.failed, "Can't find LocalMember", nil)
        return
      }
      if let banned = self.localMember?.isAudioBanned,
         banned {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to unmute my audio. Audio banned"
        )
        callback?(NELiveStreamErrorCode.failed, "Audio banned", nil)
        return
      }

      self.roomContext?.rtcController.unmuteMyAudio(enableMediaPub: true, callback: { code, msg, obj in
        var res = code
        if res != 0 {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to unmute my audio. Code: \(res). Msg: \(msg ?? "")"
          )
        }
        callback?(res, msg, nil)
      })
    }, failure: callback)
  }

  /// 开启耳返功能
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
  /// - Parameter volume: 设置耳返音量
  /// - Returns: 0: 代表成功，否则失败
  @discardableResult
  func enableEarBack(_ volume: UInt32) -> Int {
    NELiveStreamLog.apiLog(kitTag, desc: "Enable earback. Volume: \(volume)")
    return Judge.syncCondition {
      let code = self.roomContext?.rtcController.enableEarback(volume: volume)
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully enable earback.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to enable earback. Code: \(String(describing: code))")
      }
      return code ?? -1
    }
  }

  // ... 继续其他方法的转换，保持相同的模式替换 NEVoiceRoom 为 NELiveStream ...

  /// 启用说话者音量提示
  ///
  /// 该方法允许 SDK 定期向 App 反馈本地发流用户和瞬时音量最高的远端用户（最多 3 位）的音量相关信息，
  /// 即当前谁在说话以及说话者的音量。启用该方法后，只要房间内有发流用户，无论是否有人说话，
  /// SDK 都会在加入房间后根据预设的时间间隔触发 onRemoteAudioVolumeIndication 回调
  /// - Parameters:
  ///   - enable: 是否启用说话者音量提示
  ///   - interval: 指定音量提示的时间间隔。单位为毫秒。必须设置为 100 毫秒的整数倍值，建议设置为 200 毫秒以上
  /// - Returns: 0: 代表成功 否则成功
  @discardableResult
  func enableAudioVolumeIndication(enable: Bool,
                                   interval: Int) -> Int {
    roomContext?.rtcController.enableAudioVolumeIndication(enable: enable, interval: interval) ?? -1
  }

  func switchCamera() {
    roomContext?.rtcController.switchCamera()
  }

  @discardableResult
  func enableLocalAudio(enable: Bool) -> Int {
    NELiveStreamLog.apiLog(kitTag, desc: "enable my local audio.")
    return Judge.syncCondition {
      guard let roomUuid = self.roomContext?.roomUuid else {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to enable my local audio. Msg: Can't find roomUuid."
        )
        return -1
      }

      let code = self.roomContext?.rtcController.enableLocalAudio(channelName: roomUuid, enable: enable)
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully enable local audio \(enable)")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to enable local audio. Code: \(String(describing: code))")
      }
      return code ?? -1
    }
  }

  @discardableResult
  func enableLocalVideo(enable: Bool) -> Int {
    NELiveStreamLog.apiLog(kitTag, desc: "enable my local video.")
    return Judge.syncCondition {
      let code = self.roomContext?.rtcController.enableLocalVideo(enable: enable)
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully enable local video.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to enable local video. Code: \(String(describing: code))")
      }
      return code ?? -1
    }
  }

  @discardableResult
  func setLocalVideoView(view: UIView?) -> Int {
    NELiveStreamLog.apiLog(kitTag, desc: "set local video view: \(view).")
    return Judge.syncCondition {
      let videoView = NERoomVideoView()
      videoView.container = view
      videoView.renderMode = .cropFill
      let code = self.roomContext?.rtcController.setupLocalVideoCanvas(videoView: videoView)
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully set local video view.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to set local video view. Code: \(String(describing: code))")
      }
      return code ?? -1
    }
  }

  @discardableResult
  func setRemoteVideoView(view: UIView?, userUuid: String) -> Int {
    NELiveStreamLog.apiLog(kitTag, desc: "set remote video view: \(view). userUuid:\(userUuid)")
    return Judge.syncCondition {
      let videoView = NERoomVideoView()
      videoView.container = view
      videoView.renderMode = .cropFill
      let code = self.roomContext?.rtcController.setupRemoteVideoCanvas(videoView: videoView, userUuid: userUuid)
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully set remote video view.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to set remote video view. Code: \(String(describing: code))")
      }
      return code ?? -1
    }
  }

  @discardableResult
  func subscribeRemoteVideoStream(userUuid: String) -> Int {
    NELiveStreamLog.apiLog(kitTag, desc: "subscribe remote video: \(userUuid).")
    return Judge.syncCondition {
      let code = self.roomContext?.rtcController.subscribeRemoteVideoStream(userUuid: userUuid, streamType: .high)
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully subscribe remote video.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to subscribe remote video. Code: \(String(describing: code))")
      }
      return code ?? -1
    }
  }
}
