//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import UIKit

// MARK: 主播暂时离开 UI

class NELiveStreamAnchorLeaveView: UIView {
  // MARK: - UI Components

  private lazy var coffeeImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = NELiveStreamUI.ne_livestream_imageName("anchor_leave_coffee")
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()

  private lazy var leaveLabel: UILabel = {
    let label = UILabel()
    label.text = "主播暂时离开"
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
    backgroundColor = UIColor(hex: 0x0E1224)
    addSubview(coffeeImageView)
    addSubview(leaveLabel)

    setupConstraints()
  }

  private func setupConstraints() {
    coffeeImageView.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview().offset(-40)
      make.size.equalTo(CGSize(width: 80, height: 80))
    }

    leaveLabel.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.top.equalTo(coffeeImageView.snp.bottom).offset(20)
    }
  }

  // MARK: - Public Methods

  func show() {
    isHidden = false
    alpha = 0
    UIView.animate(withDuration: 0.3) {
      self.alpha = 1
    }
  }

  func hide() {
    UIView.animate(withDuration: 0.3) {
      self.alpha = 0
    } completion: { _ in
      self.isHidden = true
    }
  }
}
