//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public extension UIImage {
  class func neliveStream_imageNamed(_ name: String) -> UIImage? {
    NELiveStreamUI.ne_livestream_imageName(name)
  }
}
