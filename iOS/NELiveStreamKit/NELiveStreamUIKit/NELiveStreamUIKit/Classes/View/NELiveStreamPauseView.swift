//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import UIKit

protocol NELiveStreamPauseViewDelegate: AnyObject {
  func onResumeLiveButtonClicked()
}

class NELiveStreamPauseView: UIView {
  // MARK: - Properties

  weak var delegate: NELiveStreamPauseViewDelegate?

  // MARK: - UI Components

  private lazy var pauseLabel: UILabel = {
    let label = UILabel()
    label.text = "你已暂停直播，观众无法看到画面"
    label.textColor = .white
    label.font = .systemFont(ofSize: 14)
    label.textAlignment = .center
    return label
  }()

  private lazy var resumeButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle("继续直播", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 16)
    button.backgroundColor = UIColor(white: 1, alpha: 0.2)
    button.layer.cornerRadius = 22
    button.addTarget(self, action: #selector(resumeButtonClicked), for: .touchUpInside)
    return button
  }()

  // MARK: - Properties

  var onResumeLiveStream: (() -> Void)?

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
    // 设置渐变背景
    let gradientLayer = CAGradientLayer()
    gradientLayer.colors = [
      UIColor(red: 0.059, green: 0.086, blue: 0.149, alpha: 1).cgColor, // #0F1626
      UIColor(red: 0.133, green: 0.075, blue: 0.149, alpha: 1).cgColor, // #221326
    ]
    gradientLayer.locations = [0.0, 1.0]
    gradientLayer.startPoint = CGPoint(x: 0.5, y: 0)
    gradientLayer.endPoint = CGPoint(x: 0.5, y: 1)
    gradientLayer.frame = bounds
    layer.insertSublayer(gradientLayer, at: 0)

    addSubview(pauseLabel)
    addSubview(resumeButton)

    setupConstraints()
  }

  private func setupConstraints() {
    pauseLabel.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview().offset(-120)
    }

    resumeButton.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.top.equalTo(pauseLabel.snp.bottom).offset(20)
      make.width.equalTo(240)
      make.height.equalTo(44)
    }
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    // 更新渐变层frame
    if let gradientLayer = layer.sublayers?.first as? CAGradientLayer {
      gradientLayer.frame = bounds
    }
  }

  // MARK: - Actions

  @objc private func resumeButtonClicked() {
    delegate?.onResumeLiveButtonClicked()
  }
}
