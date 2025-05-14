//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SDWebImage
import UIKit

// MARK: - 主播信息顶部 UI

class NELiveStreamAnchorTopInfoView: UIView {
  // MARK: - 属性

  private var avatarUrl: String? {
    didSet {
      guard let urlStr = avatarUrl, let url = URL(string: urlStr) else { return }
      avatar.sd_setImage(with: url)
    }
  }

  private var nickname: String? {
    didSet {
      nickLabel.text = nickname
    }
  }

  private var roomId: String? {
    didSet {
      roomIdLabel.text = "ID: \(roomId ?? "")"
    }
  }

  private var duration: String? {
    didSet {
      durationLabel.text = duration
    }
  }

  // MARK: - 子视图

  private lazy var containerView: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor(white: 0, alpha: 0.6)
    view.layer.cornerRadius = 18
    view.clipsToBounds = true
    return view
  }()

  private lazy var avatar: UIImageView = {
    let view = UIImageView()
    view.layer.cornerRadius = 16
    view.layer.masksToBounds = true
    return view
  }()

  private lazy var nickLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14)
    label.textColor = .white
    return label
  }()

  private lazy var roomIdLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 10)
    label.textColor = .white.withAlphaComponent(0.8)
    return label
  }()

  private lazy var liveStatusView: UIView = {
    let view = UIView()
    view.backgroundColor = .green
    view.layer.cornerRadius = 2
    return view
  }()

  private lazy var durationLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = .white
    return label
  }()

  // MARK: - 初始化

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }

  required init?(coder: NSCoder) {
    super.init(coder: coder)
    setupUI()
  }

  private func setupUI() {
    addSubview(containerView)
    containerView.addSubview(avatar)
    containerView.addSubview(nickLabel)
    containerView.addSubview(roomIdLabel)
  }

  // MARK: - 公开方法

  /// 配置视图数据
  func configure(avatar: String?, nickname: String?, roomId: String?) {
    avatarUrl = avatar
    self.nickname = nickname
    self.roomId = roomId
    duration = "00:00:00"

    setNeedsLayout()
  }

  /// 更新直播时长
  func updateDuration(_ duration: String) {
    self.duration = duration
  }

  // MARK: - 布局

  override func layoutSubviews() {
    super.layoutSubviews()

    let padding: CGFloat = 8
    let containerHeight: CGFloat = 36

    containerView.frame = CGRect(x: 0, y: 0, width: bounds.width, height: containerHeight)
    avatar.frame = CGRect(x: 0, y: 2, width: 32, height: 32)

    nickLabel.frame = CGRect(x: avatar.frame.maxX + padding,
                             y: 4,
                             width: 120,
                             height: 16)

    roomIdLabel.frame = CGRect(x: nickLabel.frame.minX,
                               y: nickLabel.frame.maxY,
                               width: bounds.width - avatar.bounds.maxX - 2 * padding,
                               height: 14)
  }
}
