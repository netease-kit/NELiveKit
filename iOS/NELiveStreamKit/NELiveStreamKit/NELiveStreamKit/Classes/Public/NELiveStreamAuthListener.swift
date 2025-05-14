// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
public enum NELiveStreamAuthEvent: Int {
  case kickOut
  case forbidden
  case accountTokenError
  case loggedIn
  case loggedOut
  case incorrectToken
  case tokenExpired
}

@objc
public protocol NELiveStreamAuthListener: AnyObject {
  func onLiveStreamAuthEvent(_ event: NELiveStreamAuthEvent)
}
