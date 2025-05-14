// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit

/// 键盘工具条代理
@objc public protocol NELiveStreamKeyboardToolbarDelegate: NSObjectProtocol {
  /// 点击工具条发送文字
  /// - Parameter text: 文本内容
  func didToolBarSendText(_ text: String)
}

/// 键盘工具条视图
@objcMembers
public class NELiveStreamKeyboardToolbarView: UIView {
  // MARK: - Public Properties

  public weak var delegate: NELiveStreamKeyboardToolbarDelegate?

  // MARK: - Private Properties

  private lazy var textField: UITextField = {
    let textField = UITextField()
    textField.font = .systemFont(ofSize: 14)
    textField.backgroundColor = UIColor(hex: 0xF0F0F2)
    textField.textColor = .black
    textField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 8, height: 0))
    textField.leftViewMode = .always
    textField.layer.cornerRadius = 16
    textField.layer.masksToBounds = true
    return textField
  }()

  private lazy var sendButton: UIButton = {
    let button = UIButton()
    button.setTitle("发送", for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 14)
    button.layer.cornerRadius = 16
    button.layer.masksToBounds = true
    button.backgroundColor = UIColor(hex: 0x337EFF)
    button.addTarget(self, action: #selector(sendButtonClick), for: .touchUpInside)
    return button
  }()

  // MARK: - Init

  override public init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Public Methods

  public func becomeFirstResponse() {
    textField.becomeFirstResponder()
  }

  public func setUpInputContent(_ content: String) {
    textField.text = content
  }

  // MARK: - Private Methods

  private func setupUI() {
    backgroundColor = .white

    addSubview(textField)
    addSubview(sendButton)

    sendButton.snp.makeConstraints { make in
      make.centerY.equalToSuperview()
      make.right.equalToSuperview().offset(-8)
      make.size.equalTo(CGSize(width: 60, height: 32))
    }

    textField.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(8)
      make.centerY.equalToSuperview()
      make.right.equalTo(sendButton.snp.left).offset(-12)
      make.height.equalTo(32)
    }
  }

  @objc private func sendButtonClick() {
    delegate?.didToolBarSendText(textField.text ?? "")
    textField.text = ""
    textField.resignFirstResponder()
  }
}
