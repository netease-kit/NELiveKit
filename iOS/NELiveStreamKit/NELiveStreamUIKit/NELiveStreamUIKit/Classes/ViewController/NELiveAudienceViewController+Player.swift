//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECoreKit
import NELivePlayerFramework
import UIKit

let playerTag: String = "liveStream_player"

extension NELiveAudienceViewController {
  public static func setPlayerLogDir() {
    guard let logDir = NEPathUtils.getDirectoryForDocuments(dir: "NIMSDK/Logs/extra_log") else { return }
    NELivePlayerController.setLogDir(logDir)
  }

  // MARK: - Observer

  func addPlayerObserver() {
    // 添加播放器监听
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(playerDidFinishPlaying),
      name: .NELivePlayerPlaybackFinished,
      object: player
    )

    NotificationCenter.default.addObserver(
      self,
      selector: #selector(playerDidPreparedToPlay),
      name: .NELivePlayerDidPreparedToPlay,
      object: player
    )
  }

  func startStream() {
    guard let rtmpUrl = roomInfo.liveModel?.externalLiveConfig?.pullRtmpUrl,
          !rtmpUrl.isEmpty else {
      NELiveStreamUILog.errorLog(playerTag, desc: "播放地址为空")
      showJoinFailedAlert()
      return
    }

    player?.view.removeFromSuperview()
    player?.shutdown()
    player = nil

    // 创建播放器
    player = NELivePlayerController(contentURL: URL(string: rtmpUrl)!, error: nil)

    NELiveStreamUILog.infoLog(playerTag, desc: "开始拉流")
    if let player = player {
      // 配置播放器
      player.view.frame = localPlayerRender.bounds
      player.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
      localPlayerRender.addSubview(player.view)
      player.shouldAutoplay = true
      player.setScalingMode(.aspectFit)

      // 开始播放
      player.prepareToPlay()
    }
  }

  func stopStream() {
    player?.shutdown()
    player?.view.removeFromSuperview()
    player = nil
  }

  // 重置重试次数
  func resetRetryCount() {
    retryCount = 0
  }

  @objc private func playerDidFinishPlaying(_ note: Notification) {
    guard note.object as AnyObject? === player else {
      return
    }

    guard let userInfo = note.userInfo,
          let reason = userInfo[NELivePlayerPlaybackDidFinishReasonUserInfoKey] as? Int else {
      return
    }

    switch NELPMovieFinishReason(rawValue: reason) ?? .userExited {
    case .playbackEnded:
      let msg = "播放结束"
      NELiveStreamUILog.errorLog(playerTag, desc: msg)
      resetRetryCount() // 播放正常结束，停止重试

    case .playbackError:
      let errorCode = userInfo[NELivePlayerPlaybackDidFinishErrorKey] as? Int ?? 0
      let msg = "播放失败，错误码：\(errorCode)"
      NELiveStreamUILog.errorLog(playerTag, desc: msg)

      // 检查重试次数 先设置无限重试，如果需要可以再打开下面的代码
//            if retryCount >= maxRetryCount {
//                NELiveStreamUILog.errorLog(playerTag, desc: "重试次数达到上限(\(maxRetryCount)次)，停止重试")
//                showJoinFailedAlert()
//                return
//            }

      // 增加重试次数
      retryCount += 1
      // 延迟3秒后重试
      DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) { [weak self] in
        guard let self = self else { return }

        if let isPlaying = self.player?.isPlaying() {
          if isPlaying {
            return
          }
        }

        NELiveStreamUILog.infoLog(playerTag, desc: "开始第\(self.retryCount)次重试拉流")

        self.startStream()
      }

    case .userExited:
      resetRetryCount()

    @unknown default:
      resetRetryCount()
    }
  }

  @objc private func playerDidPreparedToPlay(_ note: Notification) {
    guard note.object as AnyObject? === player else {
      return
    }

    NELiveStreamUILog.successLog(playerTag, desc: "准备开始播放")

    // 准备播放成功，可以停止重试了
    resetRetryCount()
  }
}
