// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NESocialUIKit
import UIKit

/// 主播视频渲染视图，包含单直播视图，与 PK UI 视图（包含左侧自己、右侧对方主播、底部倒计时）
class NELiveStreamPKUIView: UIView {
  // 左侧自己的渲染视图
  var localRenderView: UIView?
  // 右侧对方主播的渲染视图
  let remoteRenderView = UIView()
  // 顶部 PK 倒计时 label
  let countdownLabel: NESocialPaddingLabel = {
    let label = NESocialPaddingLabel()
    label.textColor = .white
    label.font = UIFont(name: "PingFangSC-Semibold", size: 12) ?? .boldSystemFont(ofSize: 12)
    label.textAlignment = .center
    label.backgroundColor = UIColor.black.withAlphaComponent(0.4)
    label.layer.cornerRadius = 12
    label.layer.masksToBounds = true
    label.layer.borderWidth = 1
    label.layer.borderColor = UIColor(hexString: "#0095FF").cgColor
    label.text = "PK 03:00"
    label.edgeInsets = UIEdgeInsets(top: 0, left: 8, bottom: 0, right: 8)
    return label
  }()

  weak var delegate: NELiveStreamPKUIViewDelegate?
  private var countdownTimer: Timer?
  private var remainingSeconds: Int = 0

  init(localRenderView: UIView) {
    super.init(frame: .zero)
    self.localRenderView = localRenderView
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  deinit {
    stopCountdown()
  }

  private func setupUI() {
    guard let localRenderView = localRenderView else {
      return
    }
    backgroundColor = .clear
    addSubview(localRenderView)
    addSubview(remoteRenderView)
    addSubview(countdownLabel)

    // 左右渲染视图分屏
    localRenderView.backgroundColor = UIColor.black
    remoteRenderView.backgroundColor = UIColor.black

    layoutSingleUI()
  }

  func layoutSingleUI() {
    guard let localRenderView = localRenderView else {
      return
    }

    remoteRenderView.isHidden = true
    countdownLabel.isHidden = true
    localRenderView.snp.remakeConstraints { make in
      make.edges.equalToSuperview()
    }
  }

  func layoutPKUI() {
    guard let localRenderView = localRenderView else {
      return
    }

    remoteRenderView.isHidden = false
    countdownLabel.isHidden = false

    localRenderView.snp.remakeConstraints { make in
      make.left.top.bottom.equalToSuperview()
      make.right.equalTo(self.snp.centerX).offset(-2)
    }
    remoteRenderView.snp.makeConstraints { make in
      make.right.top.bottom.equalToSuperview()
      make.left.equalTo(self.snp.centerX).offset(2)
    }
    countdownLabel.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.top.equalTo(localRenderView.snp.bottom).offset(16)
      make.height.equalTo(24)
    }
  }

  func startCountdown(duration: Int = 180) {
    countdownTimer?.invalidate()
    remainingSeconds = duration
    updateCountdownLabel()
    countdownLabel.isHidden = false
    countdownTimer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(updateCountdown), userInfo: nil, repeats: true)
  }

  func stopCountdown() {
    countdownTimer?.invalidate()
    countdownTimer = nil
  }

  @objc private func updateCountdown() {
    remainingSeconds -= 1
    updateCountdownLabel()
    if remainingSeconds <= 0 {
      stopCountdown()
      delegate?.pkCountdownDidFinish(self)
    }
  }

  private func updateCountdownLabel() {
    let minutes = remainingSeconds / 60
    let seconds = remainingSeconds % 60
    countdownLabel.text = String(format: "PK %02d:%02d", minutes, seconds)
  }
}

protocol NELiveStreamPKUIViewDelegate: AnyObject {
  func pkCountdownDidFinish(_ pkUIView: NELiveStreamPKUIView)
}
