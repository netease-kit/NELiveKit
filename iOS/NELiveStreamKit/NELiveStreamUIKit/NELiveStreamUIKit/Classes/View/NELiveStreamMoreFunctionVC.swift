//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NELiveStreamKit
import UIKit

protocol NELiveStreamMoreFunctionDelegate: AnyObject {
  /// 翻转摄像头
  func didSwitchCamera()
  /// 继续/暂停直播
  func didToggleLive(_ isLive: Bool) -> Bool
  /// 评论设置
  func didCommentSetting()
  /// 屏幕方向
  func didScreenOrientation()
  /// 小窗播放
  func didPictureInPicture()
  /// 质量数据
  func didQualityData()
  /// 高级设置
  func didAdvancedSetting()
}

// MARK: - Supporting Classes

class NELiveStreamMoreItem {
  var title: String
  var image: UIImage?
  let tag: Int
  var isOn: Bool

  init(title: String, image: UIImage?, tag: Int, isOn: Bool = false) {
    self.title = title
    self.image = image
    self.tag = tag
    self.isOn = isOn
  }
}

class NELiveStreamMoreCell: UICollectionViewCell {
  private lazy var imageView: UIImageView = {
    let view = UIImageView()
    view.contentMode = .scaleAspectFit
    return view
  }()

  private lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.textAlignment = .center
    label.font = .systemFont(ofSize: 12)
    label.textColor = .darkText
    return label
  }()

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func setupUI() {
    contentView.addSubview(imageView)
    contentView.addSubview(titleLabel)

    imageView.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(12)
      make.centerX.equalToSuperview()
      make.size.equalTo(CGSize(width: 40, height: 40))
    }

    titleLabel.snp.makeConstraints { make in
      make.top.equalTo(imageView.snp.bottom).offset(8)
      make.left.right.equalToSuperview()
      make.height.equalTo(20)
    }
  }

  func configure(with item: NELiveStreamMoreItem) {
    imageView.image = item.image
    titleLabel.text = item.title
  }
}

class NELiveStreamMoreFunctionVC: UIViewController {
  // MARK: - Properties

  weak var delegate: NELiveStreamMoreFunctionDelegate?

  private var items: [NELiveStreamMoreItem] = []
  private var isAnchor: Bool = false

  // MARK: - UI Components

  private lazy var flowLayout: UICollectionViewFlowLayout = {
    let layout = UICollectionViewFlowLayout()
    layout.scrollDirection = .vertical
    layout.minimumLineSpacing = 20 // 调整行间距
    layout.minimumInteritemSpacing = 0
    layout.itemSize = CGSize(width: 60, height: 84)
    // 调整内边距，确保在底部有合适的间距
    layout.sectionInset = UIEdgeInsets(top: 20, left: 30, bottom: 20, right: 30)
    return layout
  }()

  private lazy var collectionView: UICollectionView = {
    let collection = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
    collection.backgroundColor = .clear
    collection.isScrollEnabled = false
    collection.dataSource = self
    collection.delegate = self
    collection.register(NELiveStreamMoreCell.self, forCellWithReuseIdentifier: "cell")
    return collection
  }()

  // MARK: - Lifecycle

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupItems()

    // 设置首选尺寸
    preferredContentSize = calculatePreferredSize()
  }

  private func calculatePreferredSize() -> CGSize {
    let screenWidth = UIScreen.main.bounds.width
    let numberOfRows = ceil(Float(items.count) / 4.0) // 每行4个item

    // 计算内容高度
    let contentHeight = flowLayout.itemSize.height * CGFloat(numberOfRows) + // items高度
      flowLayout.minimumLineSpacing * max(0, CGFloat(numberOfRows - 1)) + // 行间距
      flowLayout.sectionInset.top + flowLayout.sectionInset.bottom // 上下内边距

    // 计算总高度（标题栏 + 内容区）
    let totalHeight = 44 + contentHeight // 44是导航栏高度

    // 添加底部安全区域高度
    let bottomSafeArea: CGFloat
    if #available(iOS 11.0, *) {
      bottomSafeArea = UIApplication.shared.windows.first?.safeAreaInsets.bottom ?? 0
    } else {
      bottomSafeArea = 0
    }

    // 限制最大高度，确保不会遮挡太多视频画面
    let maxHeight = UIScreen.main.bounds.height * 0.4 // 最多占屏幕高度的40%
    let finalHeight = min(totalHeight + bottomSafeArea, maxHeight)

    return CGSize(width: screenWidth, height: finalHeight)
  }

  // MARK: - Private Methods

  private func setupUI() {
    view.backgroundColor = .white
    title = NSLocalizedString("更多设置", comment: "")

    view.addSubview(collectionView)
    collectionView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }
  }

  public func updateRole(isAnchor: Bool) {
    self.isAnchor = isAnchor
    setupItems()
  }

  private func setupItems() {
    if isAnchor {
      items = [
        NELiveStreamMoreItem(title: "翻转镜头",
                             image: UIImage.neliveStream_imageNamed("live_more_camera_icon"),
                             tag: 0),
        NELiveStreamMoreItem(title: "暂停直播",
                             image: UIImage.neliveStream_imageNamed("live_more_cancel_icon"),
                             tag: 1),
      ]
    } else {
      items = [
        NELiveStreamMoreItem(title: "翻转镜头",
                             image: UIImage.neliveStream_imageNamed("live_more_camera_icon"),
                             tag: 0),
      ]
    }
    collectionView.reloadData()
    preferredContentSize = calculatePreferredSize()
  }
}

// MARK: - UICollectionViewDataSource

extension NELiveStreamMoreFunctionVC: UICollectionViewDataSource {
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    items.count
  }

  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! NELiveStreamMoreCell
    let item = items[indexPath.item]
    cell.configure(with: item)
    return cell
  }
}

// MARK: - UICollectionViewDelegate

extension NELiveStreamMoreFunctionVC: UICollectionViewDelegate {
  func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    let item = items[indexPath.item]
    switch item.tag {
    case 0:
      delegate?.didSwitchCamera()
    case 1:
      let ret = delegate?.didToggleLive(!item.isOn) ?? false
      guard ret else {
        return
      }

      item.isOn.toggle()
      item.title = item.isOn ? "继续直播" : "暂停直播"
      item.image = item.isOn ? UIImage.neliveStream_imageNamed("live_more_continue_icon") : UIImage.neliveStream_imageNamed("live_more_cancel_icon")
      collectionView.reloadItems(at: [indexPath])
    case 2:
      delegate?.didPictureInPicture()
    default:
      break
    }
  }
}
