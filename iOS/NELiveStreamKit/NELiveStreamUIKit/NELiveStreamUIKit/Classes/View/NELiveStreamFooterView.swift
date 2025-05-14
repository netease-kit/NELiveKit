// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import Masonry
import NEUIKit
import NIMSDK
import SDWebImage
import UIKit

@objcMembers
public class NELiveStreamFooterView: UIView, UITextFieldDelegate {
  // MARK: - Public Properties

  /// 输入框点击回调
  public var onInputClick: (() -> Void)?
  /// PK按钮点击回调
  public var onPKClick: (() -> Void)?
  /// 连麦按钮点击回调
  public var onLinkMicClick: (() -> Void)?
  /// 美颜按钮点击回调
  public var onBeautyClick: (() -> Void)?
  /// 音乐按钮点击回调
  public var onMusicClick: (() -> Void)?
  /// 礼物按钮点击回调
  public var onGiftClick: (() -> Void)?
  /// 更多按钮点击回调
  public var onMoreClick: (() -> Void)?

  /// 代理
  public weak var delegate: NELiveStreamFooterDelegate?

  // MARK: - Private Properties

  private lazy var searchBarBgView: UIView = NEUIViewFactory.createView(
    frame: .zero,
    backgroundColor: UIColor(red: 0, green: 0, blue: 0, alpha: 0.5)
  )

  private lazy var searchImageView: UIImageView = {
    let imageView = NEUIViewFactory.createImageView(frame: .zero)
    imageView.image = UIImage.neliveStream_imageNamed("chatroom_titleIcon")?.ne_image(withTintColor: UIColor(hex: 0xAAACB7))
    return imageView
  }()

  private lazy var inputTextField: UITextField = {
    let textField = NEUIViewFactory.createTextField(
      frame: .zero,
      placeholder: ""
    )
    let attributes: [NSAttributedString.Key: Any] = [
      .foregroundColor: UIColor(hex: 0xAAACB7),
    ]
    textField.attributedPlaceholder = NSAttributedString(
      string: "一起聊聊吧~",
      attributes: attributes
    )

    textField.font = .systemFont(ofSize: 13)
    textField.textColor = .white
    return textField
  }()

  private lazy var pkButton: UIButton = NEUIViewFactory.createButton(
    frame: .zero,
    image: "live_pk_icon",
    target: self,
    action: #selector(pkButtonClick)
  )

  private lazy var linkMicButton: UIButton = NEUIViewFactory.createButton(
    frame: .zero,
    image: "live_link_mic_icon",
    target: self,
    action: #selector(linkMicButtonClick)
  )

  private lazy var beautyButton: UIButton = NEUIViewFactory.createButton(
    frame: .zero,
    image: "live_beauty_icon",
    target: self,
    action: #selector(beautyButtonClick)
  )

  private lazy var musicButton: UIButton = NEUIViewFactory.createButton(
    frame: .zero,
    image: "live_music_icon",
    target: self,
    action: #selector(musicButtonClick)
  )

  private lazy var giftButton: UIButton = NEUIViewFactory.createButton(
    frame: .zero,
    image: "live_gift_icon",
    target: self,
    action: #selector(giftButtonClick)
  )

  private lazy var moreButton: UIButton = NEUIViewFactory.createButton(
    frame: .zero,
    image: "live_more_icon",
    target: self,
    action: #selector(moreButtonClick)
  )

  // 添加红点视图
  private lazy var linkMicRedDotView: UIView = {
    let view = UIView()
    view.backgroundColor = .red
    view.isHidden = true
    view.layer.cornerRadius = 5
    return view
  }()

  private var isAnchor: Bool = false {
    didSet {
      updateUI()
    }
  }

  // MARK: - Init

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()

    // 设置所有按钮的通用样式
    for button in [pkButton, linkMicButton, beautyButton, musicButton, giftButton, moreButton] {
      button.backgroundColor = .black.withAlphaComponent(0.3)
      button.layer.cornerRadius = 18
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Public Methods

  /// 更新角色
  /// - Parameter isAnchor: 是否是主播
  public func updateRole(isAnchor: Bool) {
    self.isAnchor = isAnchor
  }

  /// 设置禁言状态
  public func setMuteWithType(_ type: NEUIMuteType) {
    var msg = ""
    switch type {
    case .self:
      msg = "您已被禁言"
    case .all:
      msg = "聊天室被禁言"
    }

    let attributes: [NSAttributedString.Key: Any] = [
      .foregroundColor: UIColor(hex: 0x4B6677),
    ]
    inputTextField.attributedPlaceholder = NSAttributedString(
      string: msg,
      attributes: attributes
    )
    searchBarBgView.isUserInteractionEnabled = false
    delegate?.footerDidReceiveNoSpeakingAction?()
  }

  /// 取消禁言
  public func cancelMute() {
    searchBarBgView.isUserInteractionEnabled = true
    let attributes: [NSAttributedString.Key: Any] = [
      .foregroundColor: UIColor(hex: 0xAAACB7),
    ]
    inputTextField.attributedPlaceholder = NSAttributedString(
      string: "一起聊聊吧~",
      attributes: attributes
    )
  }

  // MARK: - Private Methods

  private func setupUI() {
    addSubview(searchBarBgView)
    searchBarBgView.addSubview(searchImageView)
    searchBarBgView.addSubview(inputTextField)

    // 添加红点到连麦按钮上
    linkMicButton.addSubview(linkMicRedDotView)
    linkMicRedDotView.snp.makeConstraints { make in
      make.size.equalTo(CGSize(width: 10, height: 10))
      make.top.equalTo(linkMicButton)
      make.right.equalTo(linkMicButton)
    }

    // 设置约束
    searchBarBgView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(8)
      make.centerY.equalToSuperview()
      make.size.equalTo(CGSize(width: NEUIScreenAdapter.width(130), height: 36))
    }

    searchImageView.snp.makeConstraints { make in
      make.left.equalTo(searchBarBgView).offset(12)
      make.centerY.equalTo(searchBarBgView)
      make.size.equalTo(CGSize(width: 14, height: 14))
    }

    inputTextField.snp.makeConstraints { make in
      make.centerY.equalTo(searchBarBgView)
      make.left.equalTo(searchImageView.snp.right).offset(10)
      make.right.equalTo(searchBarBgView)
    }

    // 添加所有按钮并设置共同属性
    for button in [pkButton, linkMicButton, beautyButton, musicButton, giftButton, moreButton] {
      addSubview(button)
      button.backgroundColor = .black.withAlphaComponent(0.3)
      button.layer.cornerRadius = 18
      button.isHidden = true
    }

    // 设置输入框代理
    inputTextField.delegate = self
  }

  override public func layoutSubviews() {
    super.layoutSubviews()
    // 设置圆角
    searchBarBgView.layer.cornerRadius = 18
    searchBarBgView.layer.masksToBounds = true
  }

  private func updateUI() {
    // 重置所有按钮状态
    for item in [pkButton, linkMicButton, beautyButton, musicButton, giftButton, moreButton] {
      item.isHidden = true
      item.snp.removeConstraints()
    }

    if isAnchor {
      // 主播UI
      for item in [linkMicButton, beautyButton, moreButton] {
        item.isHidden = false
      }

      linkMicButton.snp.makeConstraints { make in
        make.left.equalTo(inputTextField.snp.right).offset(8)
        make.centerY.equalToSuperview()
        make.size.equalTo(CGSize(width: 36, height: 36))
      }

      beautyButton.snp.makeConstraints { make in
        make.left.equalTo(linkMicButton.snp.right).offset(8)
        make.centerY.equalToSuperview()
        make.size.equalTo(CGSize(width: 36, height: 36))
      }

      moreButton.snp.makeConstraints { make in
        make.left.equalTo(beautyButton.snp.right).offset(8)
        make.right.equalToSuperview().offset(-12)
        make.centerY.equalToSuperview()
        make.size.equalTo(CGSize(width: 36, height: 36))
      }
    } else {
      // 观众UI
      for item in [giftButton, linkMicButton, moreButton] {
        item.isHidden = false
      }

      giftButton.snp.makeConstraints { make in
        make.left.equalTo(inputTextField.snp.right).offset(12)
        make.centerY.equalToSuperview()
        make.size.equalTo(CGSize(width: 36, height: 36))
      }

      linkMicButton.snp.makeConstraints { make in
        make.left.equalTo(giftButton.snp.right).offset(8)
        make.centerY.equalToSuperview()
        make.size.equalTo(CGSize(width: 36, height: 36))
      }

      moreButton.snp.makeConstraints { make in
        make.left.equalTo(linkMicButton.snp.right).offset(8)
        make.right.equalToSuperview().offset(-12)
        make.centerY.equalToSuperview()
        make.size.equalTo(CGSize(width: 36, height: 36))
      }
    }

    layoutIfNeeded()
  }

  // MARK: - Actions

  @objc private func inputTextFieldClick() {
    delegate?.footerDidReceiveInputViewClickAction?()
  }

  @objc private func pkButtonClick() {
    onPKClick?()
  }

  @objc private func linkMicButtonClick() {
    delegate?.footerDidLinkMicClick?()
  }

  @objc private func beautyButtonClick() {
    delegate?.footerDidReceiveBeautyClickAction?()
  }

  @objc private func musicButtonClick() {
    delegate?.footerDidReceiveMusicClickAction?()
  }

  @objc private func giftButtonClick() {
    delegate?.footerDidReceiveGiftClick?()
  }

  @objc private func moreButtonClick() {
    delegate?.footerDidReceiveMoreClickAction?()
  }

  // MARK: - UITextFieldDelegate

  public func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
    delegate?.footerDidReceiveInputViewClickAction?()
    return false
  }

  // 添加显示/隐藏红点的方法
  public func showLinkMicRedDot(_ show: Bool) {
    linkMicRedDotView.isHidden = !show
  }
}

// MARK: - 协议定义

@objc public protocol NELiveStreamFooterDelegate: NSObjectProtocol {
  /// 礼物点击事件
  @objc optional func footerDidReceiveGiftClick()

  /// 连麦点击事件
  @objc optional func footerDidLinkMicClick()

  /// 麦克风静音事件
  @objc optional func footerDidReceiveMicMuteAction(_ mute: Bool)

  /// 禁言事件
  @objc optional func footerDidReceiveNoSpeakingAction()

  /// 菜单点击事件
  @objc optional func footerDidReceiveMenuClickAction()

  /// 输入框点击事件
  @objc optional func footerDidReceiveInputViewClickAction()

  /// 音乐点击事件
  @objc optional func footerDidReceiveMusicClickAction()

  /// 美颜点击事件
  @objc optional func footerDidReceiveBeautyClickAction()

  /// 更多按钮点击
  @objc optional func footerDidReceiveMoreClickAction()
}

// MARK: - 枚举定义

enum NEUIFunctionArea: Int {
  case requestMic, beauty, more, gift, music
}

public enum NELiveStreamRole {
  case host, audience
}

// MARK: - NEUIMuteType

@objc public enum NEUIMuteType: Int {
  /// 全部禁言
  case all = 0
  /// 自己禁言
  case `self` = 1
}
