//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NELiveStreamKit
import NIMSDK
import SDWebImage
import UIKit

// MARK: - 观众头像单元格

class NELiveStreamAudienceNumCell: UICollectionViewCell {
  // MARK: 属性

  private lazy var avatar: UIImageView = {
    let view = UIImageView()
    view.layer.cornerRadius = 14
    view.layer.masksToBounds = true
    return view
  }()

  // MARK: 初始化

  override init(frame: CGRect) {
    super.init(frame: frame)
    contentView.backgroundColor = .clear
    contentView.addSubview(avatar)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: 布局

  override func layoutSubviews() {
    super.layoutSubviews()
    let length = min(contentView.bounds.width, contentView.bounds.height)
    avatar.frame = CGRect(x: 0, y: 0, width: length, height: length)
    avatar.center = contentView.center
  }

  // MARK: 配置数据

  func configure(with model: NELiveStreamAudience?) {
    guard let urlStr = model?.icon, let url = URL(string: urlStr) else {
      avatar.image = nil
      return
    }
    avatar.sd_setImage(with: url)
  }

  // MARK: 工具方法

  static func cell(for collectionView: UICollectionView,
                   at indexPath: IndexPath,
                   with datas: [NELiveStreamAudience]) -> NELiveStreamAudienceNumCell {
    let cell = collectionView.dequeueReusableCell(
      withReuseIdentifier: String(describing: self),
      for: indexPath
    ) as! NELiveStreamAudienceNumCell

    if datas.indices.contains(indexPath.item) {
      let model = datas[indexPath.item]
      cell.configure(with: model)
    }

    return cell
  }

  // MARK: 尺寸定义

  static var cellSize: CGSize {
    CGSize(width: 31, height: 28)
  }
}

// MARK: - 顶部观众数量及头像列表 UI

class NELiveStreamAudienceNum: UIView, UICollectionViewDataSource, UICollectionViewDelegate {
  // MARK: 属性

  private var datas: [NELiveStreamAudience] = []

  // MARK: 属性

  private var originDatas: [NELiveStreamAudience] = []

  private lazy var collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    layout.itemSize = NELiveStreamAudienceNumCell.cellSize
    layout.scrollDirection = .horizontal
    layout.minimumInteritemSpacing = 0
    layout.minimumLineSpacing = 0

    let view = UICollectionView(frame: .zero, collectionViewLayout: layout)
    view.backgroundColor = .clear
    view.showsHorizontalScrollIndicator = false
    view.showsVerticalScrollIndicator = false
    view.register(
      NELiveStreamAudienceNumCell.self,
      forCellWithReuseIdentifier: String(describing: NELiveStreamAudienceNumCell.self)
    )
    return view
  }()

  private lazy var numLabel: UILabel = {
    let label = UILabel()
    label.textAlignment = .center
    label.textColor = .white
    label.font = .systemFont(ofSize: 12)
    label.layer.cornerRadius = 14
    label.layer.masksToBounds = true
    label.text = "0"
    label.backgroundColor = UIColor(white: 0, alpha: 0.6)
    return label
  }()

  // MARK: 初始化

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: 布局

  override func layoutSubviews() {
    super.layoutSubviews()
    collectionView.frame = CGRect(x: 0, y: 0, width: 93, height: 28)
    numLabel.frame = CGRect(x: bounds.width - 45, y: 0, width: 45, height: 28)
  }

  // MARK: UI 配置

  private func setupUI() {
    addSubview(collectionView)
    addSubview(numLabel)
    collectionView.dataSource = self
    collectionView.delegate = self
  }

  // MARK: 数据刷新

  func reload(with datas: [NELiveStreamAudience]) {
    originDatas = datas

    var processedDatas = datas
    // 补充空数据到3个
    let diff = 3 - datas.count
    if diff > 0 {
      let emptyMembers = (0 ..< diff).map { _ in NELiveStreamAudience() }
      processedDatas = emptyMembers + processedDatas
    } else {
      // 取前三个
      processedDatas = Array(processedDatas.prefix(3))
    }

    self.datas = processedDatas

    numLabel.text = "\(datas.count)"
    collectionView.reloadData()
  }

  // MARK: 更新观众数量

  func updateCount(_ count: Int) {
    numLabel.text = "\(count)"
  }

  // MARK: 获取观众列表

  func getAudienceList() -> [NELiveStreamAudience] {
    originDatas
  }

  // MARK: CollectionView 数据源

  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    datas.count
  }

  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    NELiveStreamAudienceNumCell.cell(for: collectionView, at: indexPath, with: datas)
  }
}
