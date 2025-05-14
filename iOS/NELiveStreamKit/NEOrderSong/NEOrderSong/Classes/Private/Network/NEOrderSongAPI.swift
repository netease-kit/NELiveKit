// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct NEAPIItem: NEAPIProtocol {
  let urlPath: String
  var url: String { NE.config.baseUrl + urlPath }
  let description: String
  let extra: String?
  var method: NEHttpMethod
  init(_ url: String,
       desc: String,
       method: NEHttpMethod = .post,
       extra: String? = nil) {
    urlPath = url
    self.method = method
    description = desc
    self.extra = extra
  }
}

enum NEAPI {
  // 音乐模块
  enum Music {
    static let pre_path =
      "/nemo/entertainmentLive/music"

    static func ready() -> NEAPIItem {
      NEAPIItem("\(pre_path)/ready", desc: "播放ready")
    }

    static func action() -> NEAPIItem {
      NEAPIItem("\(pre_path)/action", desc: "暂停/继续/结束演唱")
    }

    static func info(_ liveRecordId: String) -> NEAPIItem {
      NEAPIItem("\(pre_path)/info?liveRecordId=\(liveRecordId)", desc: "获取房间当前演唱信息", method: .get)
    }
  }

  enum PickSong {
    static let pre_path = "/nemo/entertainmentLive/live/song"
    static func getMusicToken() -> NEAPIItem {
      NEAPIItem("\(pre_path)/getMusicToken", desc: "获取实时计算Token")
    }

    static func orderSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/orderSong", desc: "点歌")
    }

    static func getOrderedSongs(_ liveRecordId: String) -> NEAPIItem {
      NEAPIItem("\(pre_path)/getOrderSongs?liveRecordId=\(liveRecordId)", desc: "获取已点列表", method: .get)
    }

    static func deleteSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/cancelOrderSong", desc: "删除已点歌曲")
    }

    static func topSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/songSetTop", desc: "置顶歌曲")
    }

    static func switchSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/switchSong", desc: "切歌")
    }
  }
}
