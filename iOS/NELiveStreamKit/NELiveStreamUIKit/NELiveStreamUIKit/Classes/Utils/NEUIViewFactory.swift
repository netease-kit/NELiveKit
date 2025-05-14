//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import UIKit

public enum NEUIViewFactory {
  /// 创建基础文本输入框
  public static func createTextField(frame: CGRect, placeholder: String) -> UITextField {
    let textField = UITextField(frame: frame)
    textField.placeholder = placeholder
    textField.borderStyle = .none
    textField.clearButtonMode = .whileEditing
    return textField
  }

  /// 创建带完整配置的标签
  public static func createLabel(frame: CGRect,
                                 title: String = "",
                                 textColor: UIColor = .black,
                                 alignment: NSTextAlignment = .left,
                                 font: UIFont = .systemFont(ofSize: 17)) -> UILabel {
    let label = UILabel(frame: frame)
    label.text = title.isEmpty ? nil : title
    label.textColor = textColor
    label.textAlignment = alignment
    label.font = font
    return label
  }

  /// 创建自定义按钮（支持多 Bundle 资源）
  public static func createButton(frame: CGRect,
                                  title: String = "",
                                  bgImage: String? = nil,
                                  selectedBgImage: String? = nil,
                                  image: String? = nil,
                                  bundle: Bundle? = nil,
                                  target: Any? = nil,
                                  action: Selector? = nil) -> UIButton {
    let button = UIButton(type: .custom)
    button.frame = frame
    button.setTitle(title, for: .normal)
    button.setTitleColor(.black, for: .normal)

    // 设置图片资源
    if let imageName = image {
      let img = loadImage(name: imageName, bundle: bundle)
      button.setImage(img, for: .normal)
    }

    // 设置背景图片
    if let bgName = bgImage {
      let bgImg = loadImage(name: bgName, bundle: bundle)
      button.setBackgroundImage(bgImg, for: .normal)
      button.setBackgroundImage(bgImg, for: .highlighted)
    }

    // 设置选中状态背景
    if let selectedBgName = selectedBgImage {
      let selectedImg = loadImage(name: selectedBgName, bundle: bundle)
      button.setBackgroundImage(selectedImg, for: .selected)
    }

    // 添加点击事件
    if let target = target, let action = action {
      button.addTarget(target, action: action, for: .touchUpInside)
    }

    return button
  }

  /// 创建系统样式按钮
  public static func createSystemButton(frame: CGRect,
                                        title: String,
                                        titleColor: UIColor = .black,
                                        backgroundColor: UIColor = .clear,
                                        target: Any? = nil,
                                        action: Selector? = nil) -> UIButton {
    let button = UIButton(type: .system)
    button.frame = frame
    button.setTitle(title, for: .normal)
    button.setTitleColor(titleColor, for: .normal)
    button.backgroundColor = backgroundColor

    if let target = target, let action = action {
      button.addTarget(target, action: action, for: .touchUpInside)
    }

    return button
  }

  /// 创建图片视图
  public static func createImageView(frame: CGRect,
                                     imageName: String? = nil,
                                     bundle: Bundle? = nil) -> UIImageView {
    let imageView = UIImageView(frame: frame)
    if let name = imageName {
      imageView.image = loadImage(name: name, bundle: bundle)
    }
    return imageView
  }

  /// 创建基础视图
  public static func createView(frame: CGRect,
                                backgroundColor: UIColor = .clear) -> UIView {
    let view = UIView(frame: frame)
    view.backgroundColor = backgroundColor
    return view
  }

  // MARK: - 私有方法 -

  private static func loadImage(name: String, bundle: Bundle?) -> UIImage? {
    if let bundle = bundle {
      return UIImage(named: name, in: bundle, compatibleWith: nil)
    }
    return UIImage.neliveStream_imageNamed(name)
  }
}
