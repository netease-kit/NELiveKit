//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers
public class NEUIBackgroundMusicModel: NSObject {
  public var musicId: String = ""
  public var musicName: String = ""
  public var musicUrl: String = ""
  public var artist: String = ""

  override public init() {
    super.init()
  }
}
