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
    get { textField.text ?? "" }
    set { textField.text = newValue }
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
      attributes: [.foregroundColor: UIColor(white: 1, alpha: 0.5)]
    )
    field.delegate = self

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

  // MARK: - Initialization

  override public init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Private Methods

  private func setupUI() {
    addSubview(containerView)
    containerView.addSubview(textField)
    containerView.addSubview(editImageView)

    containerView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    editImageView.snp.makeConstraints { make in
      make.centerY.equalToSuperview()
      make.right.equalToSuperview().offset(-12)
      make.size.equalTo(CGSize(width: 20, height: 20))
    }

    textField.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(12)
      make.right.equalTo(editImageView.snp.left).offset(-8)
      make.height.equalToSuperview()
    }
  }
}

extension NELiveStreamCoverSetting: UITextFieldDelegate {
  public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    textField.resignFirstResponder()
    return true
  }

  public func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    if string == "\n" {
      textField.resignFirstResponder()
      return false
    }
    return (textField.text?.count ?? 0) + (string.count - range.length) <= 20
  }
}
