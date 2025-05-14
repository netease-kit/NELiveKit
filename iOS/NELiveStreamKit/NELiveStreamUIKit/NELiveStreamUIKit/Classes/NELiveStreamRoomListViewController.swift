//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import MJRefresh
import NELiveStreamKit
import NESocialUIKit
import SDWebImage // 需要添加依赖（Podfile中添加 pod 'SDWebImage'）
import UIKit

// MARK: - 直播间数据模型（示例）

struct LiveStreamRoom {
  let coverURL: String // 封面图URL
  let title: String // 直播间标题
  let anchorAvatar: String // 主播头像
  let anchorName: String // 主播昵称
  let viewers: Int // 观看人数
}

let roomlistControllerTag: String = "roomlistController"

public class NELiveStreamRoomListCell: UICollectionViewCell {
  // 添加静态属性
  static let reuseIdentifier = NSStringFromClass(NELiveStreamRoomListCell.self)

  static func cell(collectionView: UICollectionView, indexPath: IndexPath, viewModel: NELiveStreamRoomInfo) -> NELiveStreamRoomListCell {
    // 使用静态属性
    if let cell = collectionView.dequeueReusableCell(
      withReuseIdentifier: reuseIdentifier,
      for: indexPath
    ) as? NELiveStreamRoomListCell {
      cell.setupViews(viewModel: viewModel)
      return cell
    }
    return NELiveStreamRoomListCell(frame: .zero)
  }

  override public init(frame: CGRect) {
    super.init(frame: frame)

    contentView.backgroundColor = .clear

    contentView.addSubview(coverImageView)
    contentView.addSubview(roomNameLabel)
    contentView.addSubview(anchorNameLabel)
    contentView.addSubview(memberCountLabel)

    contentView.layer.masksToBounds = true
    contentView.layer.cornerRadius = 10
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func setupConstraints() {
    coverImageView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    memberCountLabel.snp.makeConstraints { make in
      make.bottom.right.equalToSuperview().offset(-8)
      make.height.equalTo(20)
      make.width.lessThanOrEqualTo(70)
    }

    anchorNameLabel.snp.makeConstraints { make in
      make.bottom.equalToSuperview().offset(-8)
      make.left.equalToSuperview().offset(8)
      make.height.equalTo(20)
      make.right.lessThanOrEqualTo(memberCountLabel.snp.left).offset(-8)
    }

    roomNameLabel.snp.makeConstraints { make in
      make.left.equalTo(anchorNameLabel)
      make.right.lessThanOrEqualToSuperview().offset(-8)
      make.height.equalTo(20)
      make.bottom.equalTo(anchorNameLabel.snp.top).offset(-8)
    }
  }

  func setupViews(viewModel: NELiveStreamRoomInfo) {
    if let str = viewModel.liveModel?.cover,
       let url = URL(string: str) {
      coverImageView.sd_setImage(with: url, completed: { image, error, type, url in
        if let error = error {
          NELiveStreamUILog.errorLog(roomlistControllerTag, desc: "[SDWebImage] 加载封面失败: \(error.localizedDescription), url: \(url?.absoluteString ?? "nil")")
        } else {
          NELiveStreamUILog.infoLog(roomlistControllerTag, desc: "[SDWebImage] 加载封面成功: url: \(url?.absoluteString ?? "nil")")
        }
      })
    }
    if let roomName = viewModel.liveModel?.liveTopic {
      roomNameLabel.isHidden = false
      roomNameLabel.text = roomName
    } else {
      roomNameLabel.isHidden = true
    }
    if let anchorName = viewModel.anchor?.userName {
      anchorNameLabel.isHidden = false
      anchorNameLabel.text = anchorName
    } else {
      anchorNameLabel.isHidden = true
    }
    if let memberCount = viewModel.liveModel?.audienceCount {
      memberCountLabel.isHidden = false
      memberCountLabel.text = String(memberCount) + " " + NELiveStreamBundle.localized("Online_Count")
    } else {
      memberCountLabel.isHidden = true
    }

    setupConstraints()
  }

  // MARK: lazy

  lazy var coverImageView: UIImageView = {
    let view = UIImageView()
    view.contentMode = .scaleAspectFill
    return view
  }()

  lazy var roomNameLabel: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 13)
    view.layer.cornerRadius = 2
    return view
  }()

  lazy var anchorNameLabel: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 12)
    view.layer.cornerRadius = 2
    return view
  }()

  lazy var memberCountLabel: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 12)
    view.layer.cornerRadius = 2
    return view
  }()
}

// MARK: - 直播间列表控制器

public class NELiveStreamRoomListViewController: UIViewController {
  // MARK: - 属性

  private var collectionView: UICollectionView!
  private var rooms: [LiveStreamRoom] = [] // 直播间数据（示例数据）

  // MARK: - 生命周期

  override public func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    getNewList()
  }

  override public func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
  }

  // MARK: - UI初始化

  private func setupUI() {
    view.backgroundColor = .white
    title = "直播房"

    view.addSubview(createBtn)
    createBtn.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(17)
      make.right.equalToSuperview().offset(-17)
      make.bottom.equalToSuperview().offset(-25)
      make.height.equalTo(48)
    }

    // 1. 创建CollectionView布局
    let layout = UICollectionViewFlowLayout()
    layout.minimumLineSpacing = 8
    layout.minimumInteritemSpacing = 8
    let length = (view.frame.width - 24) / 2.0
    layout.itemSize = CGSize(width: length, height: length)
    layout.sectionInset = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)

    // 2. 初始化CollectionView
    collectionView = UICollectionView(frame: view.bounds, collectionViewLayout: layout)
    collectionView.delegate = self
    collectionView.dataSource = self
    collectionView.alwaysBounceVertical = true
    collectionView.backgroundColor = .white

    if #available(iOS 11.0, *) {
      collectionView.contentInsetAdjustmentBehavior = .never
    }

    collectionView.register(
      NELiveStreamRoomListCell.self,
      forCellWithReuseIdentifier: NELiveStreamRoomListCell.reuseIdentifier
    )
    view.addSubview(collectionView)
    collectionView.snp.makeConstraints { make in
      if #available(iOS 11.0, *) {
        make.top.equalTo(view.safeAreaLayoutGuide).offset(10)
      } else {
        make.top.equalTo(view).offset(10)
      }
      make.left.right.equalToSuperview()
      make.bottom.equalTo(createBtn.snp.top)
    }

    collectionView.addSubview(emptyView)
    emptyView.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview().offset(-100)
    }

    // 下拉刷新
    let mjHeader = MJRefreshGifHeader { [weak self] in
      self?.getNewList()
    }
    mjHeader.setTitle(NELiveStreamBundle.localized("Room_List_Update"), for: .idle)
    mjHeader.setTitle(NELiveStreamBundle.localized("Room_List_Update"), for: .pulling)
    mjHeader.setTitle(NELiveStreamBundle.localized("Room_List_Updating"), for: .refreshing)
    mjHeader.tintColor = .white
    mjHeader.lastUpdatedTimeLabel?.isHidden = true

    collectionView.mj_header = mjHeader

    // 上拉加载
    collectionView.mj_footer = MJRefreshBackNormalFooter(refreshingBlock: {
      [weak self] in
      if let isEnd = self?.viewModel.isEnd,
         isEnd {
        NELiveStreamToast.show(NELiveStreamBundle.localized("Room_List_No_More"))
        self?.collectionView.mj_footer?.endRefreshing()
      } else {
        self?.getMoreList()
      }
    })
  }

  private func pushToRoomViewController(_ roomInfoModel: NELiveStreamRoomInfo, isHost: Bool) {}

  func getNewList() {
    viewModel.requestNewData()
  }

  func getMoreList() {
    viewModel.requestMoreData()
  }

  @objc private func createRoom() {
    let view = NELiveAnchorViewController()
    view.modalPresentationStyle = .fullScreen
    navigationController?.pushViewController(view, animated: true)
  }

  lazy var viewModel: NELiveStreamRoomListViewModel = {
    let model = NELiveStreamRoomListViewModel()
    model.datasChanged = { [weak self] datas in
      self?.collectionView.reloadData()
      self?.emptyView.isHidden = datas.count > 0
    }
    model.isLoadingChanged = { [weak self] isLoading in
      if !isLoading {
        self?.collectionView.mj_header?.endRefreshing()
        self?.collectionView.mj_footer?.endRefreshing()
      }
    }
    model.errorChanged = { [weak self] error in
      guard let error = error else {
        return
      }
      if error.code == NELiveStreamRoomListViewModel.EMPTY_LIST_ERROR {
        NELiveStreamToast.show(NELiveStreamBundle.localized("Room_List_Empty"))
      } else if error.code == NELiveStreamRoomListViewModel.NO_NETWORK_ERROR {
        NELiveStreamToast.show(NELiveStreamBundle.localized("Net_Error"))
      } else {
        if let msg = error.userInfo[NSLocalizedDescriptionKey] as? String {
          NELiveStreamToast.show(msg)
        } else {
          NELiveStreamToast.show(NELiveStreamBundle.localized("Room_List_Error"))
        }
      }
    }
    return model
  }()

  lazy var createBtn: UIButton = {
    let btn = UIButton()
    btn.setTitle(NELiveStreamBundle.localized("Create_Room_Title"), for: .normal)
    btn.backgroundColor = UIColor(red: 0.2, green: 0.494, blue: 1, alpha: 1)
    btn.layer.cornerRadius = 24
    btn.clipsToBounds = true
    btn.addTarget(self, action: #selector(createRoom), for: .touchUpInside)
    return btn
  }()

  lazy var emptyView: NESocialRoomListEmptyView = .init()
}

extension NELiveStreamRoomListViewController: UICollectionViewDelegate {
  public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    let roomInfoModel = viewModel.datas[indexPath.row]

    // 检查是否已经有小窗口在显示
    if NESocialFloatWindow.instance.hasFloatWindow {
      // 如果已经在当前房间的小窗口，则恢复该窗口
      if NESocialFloatWindow.instance.roomUuid == roomInfoModel.liveModel?.roomUuid {
        NESocialFloatWindow.instance.button.clickAction?()
        return
      }

      // 如果在其他房间的小窗口，显示确认对话框
      let alert = UIAlertController(
        title: NSLocalizedString("提示", comment: ""),
        message: NSLocalizedString("是否退出当前直播间？", comment: ""),
        preferredStyle: .alert
      )

      alert.addAction(UIAlertAction(
        title: NSLocalizedString("取消", comment: ""),
        style: .cancel
      ))

      alert.addAction(UIAlertAction(
        title: NSLocalizedString("确认", comment: ""),
        style: .default
      ) { [weak self] _ in
        NESocialFloatWindow.instance.closeAction?({ [weak self] in
          DispatchQueue.main.async {
            self?.pushToAudienceViewController(roomInfoModel)
          }
        })
      })

      present(alert, animated: true)
    } else {
      // 直接进入直播间
      pushToAudienceViewController(roomInfoModel)
    }
  }

  // 进入观众直播间
  private func pushToAudienceViewController(_ roomInfo: NELiveStreamRoomInfo) {
    // 创建并推入观众直播间控制器
    let audienceVC = NELiveAudienceViewController(roomInfo: roomInfo)
    navigationController?.pushViewController(audienceVC, animated: true)
  }
}

extension NELiveStreamRoomListViewController: UICollectionViewDataSource {
  public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    viewModel.datas.count
  }

  public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    NELiveStreamRoomListCell.cell(collectionView: collectionView, indexPath: indexPath, viewModel: viewModel.datas[indexPath.row])
  }
}
