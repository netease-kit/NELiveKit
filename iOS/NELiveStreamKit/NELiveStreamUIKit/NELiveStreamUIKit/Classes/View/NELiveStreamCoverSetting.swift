//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECommonUIKit
import SDWebImage
import UIKit

// MARK: - NELiveStreamCoverSetting

@objcMembers
public class NELiveStreamCoverSetting: UIView {
  // MARK: - Public Properties

  public var roomName: String {
    get { textView.text ?? "" }
    set { textView.text = newValue }
  }

  public var roomId: String {
    get { idLabel.text?.replacingOccurrences(of: "ID ", with: "") ?? "" }
    set { idLabel.text = "ID \(newValue)" }
  }

  // MARK: - Private Properties

  private lazy var containerView: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor(white: 0, alpha: 0.3)
    view.layer.cornerRadius = 8
    return view
  }()

  private lazy var textField: UITextField = {
    let field = UITextField()
    field.placeholder = "请输入房间名称"
    field.textColor = .white
    field.font = .systemFont(ofSize: 16)
    field.tintColor = .white
    field.attributedPlaceholder = NSAttributedString(
      string: "请输入房间名称",
      attributes: [.foregroundColor: UIColor(white: 0, alpha: 0.3)]
    )

    return field
  }()

  private lazy var idLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(white: 1, alpha: 0.5)
    label.font = .systemFont(ofSize: 14)
    return label
  }()

  private lazy var editImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = UIImage.neliveStream_imageNamed("live_edit_icon")
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()

  /// 签名输入框
  private lazy var textView: NETextView = {
    let textView = NETextView()
    textView.font = .systemFont(ofSize: 16)
    textView.textColor = .white
    textView.placeholderLabel.numberOfLines = 1
    textView.placeholder = "请输入房间名称"
    textView.backgroundColor = .clear
    textView.delegate = self
    textView.layer.cornerRadius = 8
    textView.clipsToBounds = true
    textView.translatesAutoresizingMaskIntoConstraints = false
    return textView
  }()

  // MARK: - Initialization

  override public init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupNotifications()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Private Methods

  private func setupUI() {
    addSubview(containerView)
    containerView.addSubview(textView)
    containerView.addSubview(editImageView)

    containerView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
      make.height.equalTo(44)
    }

    editImageView.snp.makeConstraints { make in
      make.centerY.equalToSuperview()
      make.right.equalToSuperview().offset(-12)
      make.size.equalTo(CGSize(width: 20, height: 20))
    }

    textView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(12)
      make.centerY.equalToSuperview()
      make.right.equalTo(editImageView.snp.left).offset(-8)
      make.height.equalTo(44)
    }
  }

  private func setupNotifications() {
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(textViewDidChangeText(_:)),
                                           name: UITextView.textDidChangeNotification,
                                           object: textView)
  }

  @objc private func textViewDidChangeText(_ notification: Notification) {
    let kMaxLength = 20
    guard let textView = notification.object as? UITextView else { return }
    let toBeString = textView.text ?? ""

    let lang = textView.textInputMode?.primaryLanguage
    if lang == "zh-Hans" {
      if let selectedRange = textView.markedTextRange {
        let position = textView.position(from: selectedRange.start, offset: 0)
        if position == nil, toBeString.count > kMaxLength {
          textView.text = String(toBeString.prefix(kMaxLength))
        }
      } else if toBeString.count > kMaxLength {
        textView.text = String(toBeString.prefix(kMaxLength))
      }
    } else if toBeString.count > kMaxLength {
      textView.text = String(toBeString.prefix(kMaxLength))
    }
  }

  // MARK: - Public Methods

  func getTopic() -> String {
    textView.text ?? ""
  }

  // MARK: - Deinit

  deinit {
    NotificationCenter.default.removeObserver(self)
  }

  /// 设置编辑图标
  public func setEditIcon(_ image: UIImage?) {
    editImageView.image = image
  }

  /// 设置文本框代理
  public func setTextFieldDelegate(_ delegate: UITextFieldDelegate) {
    textField.delegate = delegate
  }

  /// 设置占位文字
  public func setPlaceholder(_ text: String, color: UIColor = UIColor(white: 1, alpha: 0.5)) {
    textField.attributedPlaceholder = NSAttributedString(
      string: text,
      attributes: [.foregroundColor: color]
    )
  }

  // 添加回调闭包
  public var onTextFieldDidEndEditing: ((String) -> Void)?

  // 在相应的位置调用回调
  @objc private func textFieldDidEndOnExit(_ textField: UITextField) {
    textField.resignFirstResponder()
    onTextFieldDidEndEditing?(textField.text ?? "")
  }
}

extension NELiveStreamCoverSetting: UITextViewDelegate {
  public func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
    if text == "\n" {
      textView.resignFirstResponder()
      return false
    }
    return textView.text.count + (text.count - range.length) <= 20
  }
}
