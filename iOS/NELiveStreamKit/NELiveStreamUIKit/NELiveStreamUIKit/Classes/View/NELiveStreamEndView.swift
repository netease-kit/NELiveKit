//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SDWebImage
import SnapKit
import UIKit

class NELiveStreamEndView: UIView {
  // MARK: - UI Components

  private lazy var effectView: UIVisualEffectView = {
    let blurEffect = UIBlurEffect(style: .light)
    let view = UIVisualEffectView(effect: blurEffect)
    return view
  }()

  private lazy var anchorAvatarView: UIImageView = {
    let imageView = UIImageView()
    imageView.contentMode = .scaleAspectFill
    imageView.layer.cornerRadius = 40
    imageView.layer.masksToBounds = true
    return imageView
  }()

  private lazy var liveTopicLabel: UILabel = {
    let label = UILabel()
    label.textColor = .white
    label.font = .systemFont(ofSize: 16)
    label.textAlignment = .center
    return label
  }()

  private lazy var dividerLine: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    view.alpha = 0.7
    return view
  }()

  private lazy var liveEndLabel: UILabel = {
    let label = UILabel()
    label.text = NSLocalizedString("直播已结束", comment: "")
    label.textColor = .white
    label.font = .systemFont(ofSize: 16)
    label.textAlignment = .center
    return label
  }()

  // MARK: - Initialization

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Private Methods

  private func setupUI() {
    backgroundColor = UIColor(white: 0, alpha: 0.5)

    addSubview(effectView)
    addSubview(anchorAvatarView)
    addSubview(liveTopicLabel)
    addSubview(dividerLine)
    addSubview(liveEndLabel)

    setupConstraints()
  }

  private func setupConstraints() {
    effectView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    anchorAvatarView.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview().offset(-100)
      make.size.equalTo(CGSize(width: 80, height: 80))
    }

    liveTopicLabel.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.top.equalTo(anchorAvatarView.snp.bottom).offset(12)
      make.left.right.equalToSuperview().inset(20)
    }

    dividerLine.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.left.equalToSuperview().offset(68)
      make.right.equalToSuperview().offset(-68)
      make.top.equalTo(liveTopicLabel.snp.bottom).offset(12)
      make.height.equalTo(0.5)
    }

    liveEndLabel.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.top.equalTo(dividerLine.snp.bottom).offset(12)
    }
  }

  // MARK: - Public Methods

  func configure(avatarUrl: String?, liveTopic: String?) {
    if let avatarUrl = avatarUrl, let url = URL(string: avatarUrl) {
      anchorAvatarView.sd_setImage(with: url)
    }
    liveTopicLabel.text = liveTopic ?? "..."
  }
}
