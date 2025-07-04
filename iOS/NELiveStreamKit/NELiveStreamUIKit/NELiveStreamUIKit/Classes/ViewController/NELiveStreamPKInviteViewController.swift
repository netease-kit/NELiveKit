// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NELiveStreamKit
import SDWebImage
import UIKit

protocol NELiveStreamPKInviteViewControllerDelegate: AnyObject {
  func didAcceptPKInvitation(inviter: NEConnectionUser)
  func didRejectPKInvitation(inviter: NEConnectionUser)
}

// 邀请弹窗 controller
class NELiveStreamPKInviteViewController: UIViewController {
  // MARK: - Properties

  private let inviter: NEConnectionUser
  private var countdownTimer: Timer?
  private var remainingSeconds: Int = 12
  weak var delegate: NELiveStreamPKInviteViewControllerDelegate?

  // MARK: - UI Components

  private lazy var containerView: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    view.layer.cornerRadius = 12
    view.layer.masksToBounds = true
    return view
  }()

  private lazy var messageLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(hexString: "#333333")
    label.font = .systemFont(ofSize: 16)
    label.textAlignment = .center
    return label
  }()

  private lazy var countdownButton: UIButton = {
    let button = UIButton(type: .custom)
    button.titleLabel?.font = .systemFont(ofSize: 14)
    button.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    button.backgroundColor = UIColor(hexString: "#F5F5F5")
    button.layer.cornerRadius = 22
    button.layer.masksToBounds = true
    button.addTarget(self, action: #selector(rejectButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var acceptButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle("确认", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 14)
    button.backgroundColor = UIColor(hexString: "#FF5496")
    button.layer.cornerRadius = 22
    button.layer.masksToBounds = true
    button.addTarget(self, action: #selector(acceptButtonClick), for: .touchUpInside)
    return button
  }()

  // MARK: - Initialization

  init(inviter: NEConnectionUser) {
    self.inviter = inviter
    super.init(nibName: nil, bundle: nil)
    modalPresentationStyle = .overFullScreen
    modalTransitionStyle = .crossDissolve
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Lifecycle

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupConstraints()
    configureData()
    startCountdown()
  }

  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    stopCountdown()
  }

  // MARK: - Private Methods

  private func setupUI() {
    view.backgroundColor = UIColor.black.withAlphaComponent(0.4)

    view.addSubview(containerView)
    containerView.addSubview(messageLabel)
    containerView.addSubview(countdownButton)
    containerView.addSubview(acceptButton)
  }

  private func setupConstraints() {
    containerView.snp.makeConstraints { make in
      make.center.equalToSuperview()
      make.left.equalToSuperview().offset(30)
      make.right.equalToSuperview().offset(-30)
    }

    messageLabel.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(24)
      make.left.equalToSuperview().offset(20)
      make.right.equalToSuperview().offset(-20)
    }

    countdownButton.snp.makeConstraints { make in
      make.top.equalTo(messageLabel.snp.bottom).offset(20)
      make.left.equalToSuperview().offset(20)
      make.height.equalTo(44)
      make.width.equalTo(acceptButton.snp.width)
      make.bottom.equalToSuperview().offset(-20)
    }

    acceptButton.snp.makeConstraints { make in
      make.top.equalTo(messageLabel.snp.bottom).offset(20)
      make.right.equalToSuperview().offset(-20)
      make.height.equalTo(44)
      make.width.equalTo((UIScreen.main.bounds.width - 140) / 2)
      make.left.equalTo(countdownButton.snp.right).offset(20)
      make.bottom.equalToSuperview().offset(-20)
    }
  }

  private func configureData() {
    messageLabel.text = "主播\(inviter.name ?? inviter.userUuid) 邀请你进行PK"
    updateCountdownButton()
  }

  private func startCountdown() {
    countdownTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
      self?.updateCountdown()
    }
  }

  private func stopCountdown() {
    countdownTimer?.invalidate()
    countdownTimer = nil
  }

  private func updateCountdown() {
    remainingSeconds -= 1
    if remainingSeconds <= 0 {
      stopCountdown()
      delegate?.didRejectPKInvitation(inviter: inviter)
      dismiss(animated: true)
      return
    }
    updateCountdownButton()
  }

  private func updateCountdownButton() {
    countdownButton.setTitle("拒绝(\(remainingSeconds)s)", for: .normal)
  }

  // MARK: - Actions

  @objc private func acceptButtonClick() {
    delegate?.didAcceptPKInvitation(inviter: inviter)
    dismiss(animated: true)
  }

  @objc private func rejectButtonClick() {
    delegate?.didRejectPKInvitation(inviter: inviter)
    dismiss(animated: true)
  }
}
