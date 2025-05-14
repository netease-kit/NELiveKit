//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import UIKit

// MARK: - NELiveStreamCircleButton

@objcMembers
private class NELiveStreamCircleButton: UIButton {
  // MARK: - Properties

  private lazy var iconView: UIImageView = {
    let imageView = UIImageView()
    return imageView
  }()

  private lazy var titleLab: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = .white
    label.textAlignment = .center
    return label
  }()

  // MARK: - Initialization

  init(title: String, icon: String) {
    super.init(frame: .zero)
    setupViews(title: title, icon: icon)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Private Methods

  private func setupViews(title: String, icon: String) {
    layer.masksToBounds = true

    addSubview(iconView)
    addSubview(titleLab)

    if let image = UIImage.neliveStream_imageNamed(icon)?.ne_image(withTintColor: .white) {
      iconView.image = image
    }

    titleLab.text = title
  }

  // MARK: - Override Methods

  override func layoutSubviews() {
    super.layoutSubviews()

    let width = bounds.width
    iconView.frame = CGRect(x: (width - 24) / 2.0, y: 12, width: 24, height: 24)
    titleLab.frame = CGRect(x: 0, y: 37, width: width, height: 20)
  }
}

// MARK: - NELiveStreamBottomPanelDelegate

protocol NELiveStreamBottomPanelDelegate: AnyObject {
  func clickBeautyBtn()
  func clickcameraBtn()
//    func clickStartLiveBtn()
}

// MARK: - NELiveStreamBottomPanel

@objcMembers
class NELiveStreamBottomPanel: UIView {
  // MARK: - Properties

  weak var delegate: NELiveStreamBottomPanelDelegate?

  private lazy var beautyBtn: NELiveStreamCircleButton = {
    let button = NELiveStreamCircleButton(title: NSLocalizedString("美颜", comment: ""), icon: "live_beauty_icon")
    button.addTarget(self, action: #selector(clickAction(_:)), for: .touchUpInside)
    return button
  }()

  private lazy var cameraBtn: NELiveStreamCircleButton = {
    let button = NELiveStreamCircleButton(title: NSLocalizedString("摄像头", comment: ""), icon: "switch_camera")
    button.addTarget(self, action: #selector(clickAction(_:)), for: .touchUpInside)
    return button
  }()

  private lazy var liveBtn: UIButton = {
    let button = UIButton()
    button.setTitle(NSLocalizedString("开启直播间", comment: ""), for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 16)
    button.layer.cornerRadius = 22
    button.layer.masksToBounds = true
    button.addTarget(self, action: #selector(clickAction(_:)), for: .touchUpInside)
    button.isUserInteractionEnabled = true

    return button
  }()

  private lazy var tipsLabel: UILabel = {
    let label = UILabel()
    label.text = NSLocalizedString("本产品仅用于功能演示，单次直播时长不超过10min", comment: "")
    label.textColor = .white
    label.font = .systemFont(ofSize: 12)
    label.textAlignment = .center
    label.alpha = 0.6
    return label
  }()

  private lazy var shadowLayer: CAGradientLayer = {
    let layer = CAGradientLayer()
    layer.colors = [
      UIColor(hex: 0x1ED0FD).cgColor,
      UIColor(hex: 0x5561FC).cgColor,
    ]
    layer.startPoint = CGPoint(x: 0, y: 0)
    layer.endPoint = CGPoint(x: 1, y: 0)

    let length = UIScreen.main.bounds.width - 40
    layer.frame = CGRect(x: 0, y: 0, width: length, height: length)

    return layer
  }()

  // MARK: - Initialization

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Private Methods

  private func setupViews() {
    addSubview(beautyBtn)
    addSubview(cameraBtn)
    addSubview(tipsLabel)

    // 设置美颜和滤镜按钮的约束
    for button in [beautyBtn, cameraBtn] {
      button.snp.makeConstraints { make in
        make.width.equalTo(64)
        make.height.equalTo(64)
      }
    }

    beautyBtn.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(100)
      make.top.equalToSuperview()
    }

    cameraBtn.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-100)
      make.top.equalToSuperview()
    }

    tipsLabel.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.top.equalTo(cameraBtn.snp.bottom).offset(8)
      make.height.equalTo(17)
    }
  }

  @objc private func clickAction(_ sender: UIButton) {
    guard let delegate = delegate else { return }

    switch sender {
    case beautyBtn:
      delegate.clickBeautyBtn()
    case cameraBtn:
      delegate.clickcameraBtn()
    default:
      break
    }
  }
}
