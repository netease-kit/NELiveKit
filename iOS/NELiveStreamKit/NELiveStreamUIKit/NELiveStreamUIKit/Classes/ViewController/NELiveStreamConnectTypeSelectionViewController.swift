// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit

// 观众连麦类型选择 controller
class NELiveStreamConnectTypeSelectionViewController: UIViewController {
  // MARK: - Properties

  private var roomUuid: String
  weak var delegate: NELiveStreamConnectListViewController?

  // MARK: - UI Components

  private lazy var titleView: UIView = {
    let view = UIView()
    view.backgroundColor = .clear
    return view
  }()

  private lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.text = "申请连麦"
    label.textColor = UIColor(hexString: "#333333")
    label.font = .systemFont(ofSize: 16, weight: .medium)
    label.textAlignment = .center
    return label
  }()

  private lazy var subtitleLabel: UILabel = {
    let label = UILabel()
    label.text = "选择连麦方式，主播同意后接通"
    label.textColor = UIColor(hexString: "#666666")
    label.font = .systemFont(ofSize: 12)
    label.textAlignment = .center
    return label
  }()

  private lazy var videoIconImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = NELiveStreamUI.ne_livestream_imageName("live_video_link_icon")
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()

  private lazy var audioIconImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = NELiveStreamUI.ne_livestream_imageName("live_audio_link_icon")
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()

  private lazy var videoTitleLabel: UILabel = {
    let label = UILabel()
    label.text = "申请视频连麦"
    label.textColor = UIColor(hexString: "#333333")
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()

  private lazy var audioTitleLabel: UILabel = {
    let label = UILabel()
    label.text = "申请语音连麦"
    label.textColor = UIColor(hexString: "#333333")
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()

  private lazy var videoButton: UIButton = {
    let button = UIButton(type: .custom)
    button.backgroundColor = .white
    button.layer.cornerRadius = 8
    button.addTarget(self, action: #selector(videoButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var videoSettingButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setImage(NELiveStreamUI.ne_livestream_imageName("setting"), for: .normal)
    button.addTarget(self, action: #selector(videoSettingButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var audioButton: UIButton = {
    let button = UIButton(type: .custom)
    button.backgroundColor = .white
    button.layer.cornerRadius = 8
    button.addTarget(self, action: #selector(audioButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var videoSeparatorLine: UIView = {
    let line = UIView()
    line.backgroundColor = UIColor(hexString: "#F5F5F5")
    return line
  }()

  private lazy var audioSeparatorLine: UIView = {
    let line = UIView()
    line.backgroundColor = UIColor(hexString: "#F5F5F5")
    return line
  }()

  // MARK: - Initialization

  init(roomUuid: String) {
    self.roomUuid = roomUuid
    super.init(nibName: nil, bundle: nil)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Lifecycle

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupNavigationBar()
    preferredContentSize = CGSize(width: UIScreen.main.bounds.width, height: 200)
  }

  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    NotificationCenter.default.post(name: NSNotification.Name("NELiveStreamConnectTypeSelectionViewDismissed"), object: nil)
  }

  // MARK: - Private Methods

  private func setupNavigationBar() {
    // Setup custom title view
    titleView.addSubview(titleLabel)
    titleView.addSubview(subtitleLabel)

    titleLabel.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(5)
      make.centerX.equalToSuperview()
    }

    subtitleLabel.snp.makeConstraints { make in
      make.top.equalTo(titleLabel.snp.bottom).offset(2)
      make.centerX.equalToSuperview()
      make.bottom.equalToSuperview().offset(-5)
    }

    titleView.snp.makeConstraints { make in
      make.width.equalTo(200) // 设置一个合适的宽度
      make.height.equalTo(66) // 设置一个更高的高度
    }

    navigationItem.titleView = titleView
  }

  private func setupUI() {
    view.backgroundColor = .white

    // 视频按钮
    view.addSubview(videoButton)
    videoButton.addSubview(videoIconImageView)
    videoButton.addSubview(videoTitleLabel)
    view.addSubview(videoSettingButton)
    view.addSubview(videoSeparatorLine)

    // 音频按钮
    view.addSubview(audioButton)
    audioButton.addSubview(audioIconImageView)
    audioButton.addSubview(audioTitleLabel)
    view.addSubview(audioSeparatorLine)

    setupConstraints()
  }

  private func setupConstraints() {
    videoButton.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(20)
      make.left.equalToSuperview().offset(16)
      make.right.equalToSuperview().offset(-16)
      make.height.equalTo(44)
    }

    videoIconImageView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(16)
      make.centerY.equalToSuperview()
      make.size.equalTo(CGSize(width: 24, height: 24))
    }

    videoTitleLabel.snp.makeConstraints { make in
      make.left.equalTo(videoIconImageView.snp.right).offset(8)
      make.centerY.equalToSuperview()
    }

    videoSettingButton.snp.makeConstraints { make in
      make.centerY.equalTo(videoButton)
      make.right.equalTo(videoButton).offset(-16)
      make.size.equalTo(CGSize(width: 24, height: 24))
    }

    videoSeparatorLine.snp.makeConstraints { make in
      make.top.equalTo(videoButton.snp.bottom)
      make.left.equalTo(videoButton).offset(16)
      make.right.equalTo(videoButton).offset(-16)
      make.height.equalTo(0.5)
    }

    audioButton.snp.makeConstraints { make in
      make.top.equalTo(videoButton.snp.bottom).offset(16)
      make.left.right.equalTo(videoButton)
      make.height.equalTo(44)
    }

    audioIconImageView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(16)
      make.centerY.equalToSuperview()
      make.size.equalTo(CGSize(width: 24, height: 24))
    }

    audioTitleLabel.snp.makeConstraints { make in
      make.left.equalTo(audioIconImageView.snp.right).offset(8)
      make.centerY.equalToSuperview()
    }

    audioSeparatorLine.snp.makeConstraints { make in
      make.top.equalTo(audioButton.snp.bottom)
      make.left.equalTo(audioButton).offset(16)
      make.right.equalTo(audioButton).offset(-16)
      make.height.equalTo(0.5)
    }
  }

  // MARK: - Actions

  @objc private func videoButtonClick() {
    delegate?.submitSeatRequest(type: .video)
    dismiss(animated: true)
  }

  @objc private func audioButtonClick() {
    delegate?.submitSeatRequest(type: .audio)
    dismiss(animated: true)
  }

  @objc private func videoSettingButtonClick() {
    // TODO: 处理视频设置按钮点击
  }
}
