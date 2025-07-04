// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NELiveStreamKit
import SDWebImage
import UIKit

/// PK 邀请列表 controller
class NELiveStreamPKInviteListViewController: UIViewController {
  private let kitTag = "NELiveStreamPKInviteListViewController"

  // MARK: - Properties

  private var pkInviteList: [NELiveStreamRoomInfo] = [] {
    didSet {
      tableView.reloadData()
      emptyView.isHidden = !pkInviteList.isEmpty
    }
  }

  private lazy var emptyView: NELiveStreamEmptyView = {
    let view = NELiveStreamEmptyView()
    view.setTip(tip: NELiveStreamBundle.localized("Host_Apply_List_Empty"))
    view.isHidden = false
    return view
  }()

  private lazy var pkStatusLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .medium)
    label.text = "PK中"
    label.textColor = UIColor(hexString: "#FF5496")
    label.textAlignment = .left
    return label
  }()

  private lazy var pkEndButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle("结束", for: .normal)
    button.setTitleColor(UIColor(hexString: "#FF5496"), for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
    button.layer.cornerRadius = 7
    button.layer.borderWidth = 1
    button.layer.borderColor = UIColor(hexString: "#FF5496").cgColor
    button.backgroundColor = .white
    button.contentEdgeInsets = UIEdgeInsets(top: 0, left: 12, bottom: 0, right: 12)
    button.addTarget(self, action: #selector(pkEndButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var pkHeaderView: UIView = {
    let view = UIView()
    view.backgroundColor = .clear
    view.addSubview(pkStatusLabel)
    view.addSubview(pkEndButton)
    // 布局
    pkStatusLabel.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(16)
      make.centerY.equalToSuperview()
    }
    pkEndButton.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-16)
      make.centerY.equalToSuperview()
      make.height.equalTo(32)
    }
    view.isHidden = true
    return view
  }()

  private lazy var tableView: UITableView = {
    let tableView = UITableView()
    tableView.backgroundColor = .clear
    tableView.separatorStyle = .none
    tableView.delegate = self
    tableView.dataSource = self
    tableView.register(NELiveStreamPKInviteCell.self, forCellReuseIdentifier: "NELiveStreamPKInviteCell")
    tableView.isScrollEnabled = true
    tableView.showsVerticalScrollIndicator = false
    return tableView
  }()

  private lazy var userListView: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    return view
  }()

  // MARK: - Initialization

  override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
    super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  deinit {
    removeRoomObserver()
  }

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    addRoomObserver()
    preferredContentSize = CGSize(width: UIScreen.main.bounds.width, height: 300)
  }

  override func viewDidAppear(_ animated: Bool) {
    fetchPKInviteList()
  }

  private func setupUI() {
    title = NSLocalizedString("PK邀请", comment: "")
    view.backgroundColor = .white

    view.addSubview(pkHeaderView)
    view.addSubview(userListView)
    userListView.addSubview(emptyView)
    userListView.addSubview(tableView)

    setupConstraints()
  }

  private func setupConstraints() {
    pkHeaderView.snp.makeConstraints { make in
      make.top.equalToSuperview()
      make.left.right.equalToSuperview()
      make.height.equalTo(56)
    }

    userListView.snp.remakeConstraints { make in
      if pkHeaderView.isHidden {
        make.top.equalToSuperview()
      } else {
        make.top.equalTo(pkHeaderView.snp.bottom)
      }
      make.left.right.bottom.equalToSuperview()
    }

    tableView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    emptyView.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview().offset(-100)
    }
  }

  private func fetchPKInviteList() {
    NELiveStreamKit.getInstance().fetchCoLiveRooms(1, pageSize: 20) { [weak self] code, str, list in
      DispatchQueue.main.async {
        guard let self = self else { return }
        if code == 0 {
          self.pkInviteList = list?.list ?? []
          let connectedList = NELiveStreamKit.getInstance().coHostManager.coHostUserList
          if connectedList.count == 1, connectedList.first?.userUuid != NELiveStreamKit.getInstance().localMember?.account {
            self.pkStatusLabel.text = "与主播 \(connectedList.first?.name ?? "") PK中"
          } else {
            self.pkStatusLabel.text = "PK中"
          }

          self.pkHeaderView.isHidden = connectedList.count == 0
          self.setupConstraints()

          self.tableView.reloadData()
        } else {
          NELiveStreamLog.errorLog(self.kitTag, desc: "Failed to fetch co live room info. Code: \(code)")
        }
      }
    }
  }

  // MARK: - Room Observer

  private func addRoomObserver() {
    // 添加房间结束监听
    NELiveStreamKit.getInstance().addLiveStreamListener(self)
    NELiveStreamKit.getInstance().coHostManager.addListener(self)
  }

  private func removeRoomObserver() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(self)
    NELiveStreamKit.getInstance().coHostManager.removeListener(self)
  }

  @objc private func pkEndButtonClick() {
    // 显示确认对话框
    let alert = UIAlertController(
      title: NSLocalizedString("结束PK？", comment: ""),
      message: NSLocalizedString("", comment: ""),
      preferredStyle: .alert
    )

    // 取消按钮
    alert.addAction(UIAlertAction(
      title: NSLocalizedString("取消", comment: ""),
      style: .cancel
    ))

    // 确认按钮
    alert.addAction(UIAlertAction(
      title: NSLocalizedString("确认", comment: ""),
      style: .destructive
    ) { [weak self] _ in
      self?.endPK()
    })

    present(alert, animated: true)
  }

  private func endPK() {
    // 处理结束PK事件
    NELiveStreamUILog.infoLog(kitTag, desc: "End PK action.")

    // 实现结束PK逻辑
    if let roomUuid = NELiveStreamKit.getInstance().coHostManager.coHostUserList.first?.roomUuid {
      NELiveStreamKit.getInstance().coHostManager.disconnect(roomUuid: roomUuid)
    }

    dismiss(animated: true, completion: nil)
  }
}

// MARK: - UITableViewDelegate & UITableViewDataSource

extension NELiveStreamPKInviteListViewController: UITableViewDelegate, UITableViewDataSource {
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    pkInviteList.count
  }

  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: "NELiveStreamPKInviteCell", for: indexPath) as! NELiveStreamPKInviteCell
    let item = pkInviteList[indexPath.row]
    let isInvited = item.liveModel?.connectionStatus == 1
    cell.configure(with: item, isInvited: isInvited)
    cell.delegate = self
    return cell
  }

  func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    64
  }
}

extension NELiveStreamPKInviteListViewController: NELiveStreamListener, NECoHostListener {
  func onConnectionUserListChanged(connectedList: [NEConnectionUser], joinedList: [NEConnectionUser], leavedList: [NEConnectionUser]) {
    if connectedList.count == 1, connectedList.first?.userUuid != NELiveStreamKit.getInstance().localMember?.account {
      pkStatusLabel.text = "与主播 \(connectedList.first?.name ?? "") PK中"
    } else {
      pkStatusLabel.text = "PK中"
    }

    pkHeaderView.isHidden = connectedList.count == 0
    setupConstraints()

    // Clear invitation status when connection is established or ended
    if connectedList.isEmpty {
      tableView.reloadData()
    }

    fetchPKInviteList()
  }

  func onConnectionRequestTimeout(inviter: NEConnectionUser, inviteeList: [NEConnectionUser]) {
    guard inviter.userUuid == NELiveStreamKit.getInstance().localMember?.account else {
      return
    }

    fetchPKInviteList()
  }

  func onConnectionRequestReject(invitee: NEConnectionUser) {
    fetchPKInviteList()
  }

  func onConnectionRequestCancelled(inviter: NEConnectionUser) {
    fetchPKInviteList()
  }
}

// MARK: - PK 邀请 Cell

protocol NELiveStreamPKInviteCellDelegate: AnyObject {
  func pkInviteCellDidTapInvite(_ cell: NELiveStreamPKInviteCell, isInvited: Bool)
}

class NELiveStreamPKInviteCell: UITableViewCell {
  weak var delegate: NELiveStreamPKInviteCellDelegate?
  private var isInvited: Bool = false

  private lazy var gradientLayer: CAGradientLayer = {
    let gradientLayer = CAGradientLayer()
    gradientLayer.colors = [UIColor(hexString: "#FF5496").cgColor, UIColor(hexString: "#FF6D4D").cgColor]
    gradientLayer.startPoint = CGPoint(x: 0, y: 0.5)
    gradientLayer.endPoint = CGPoint(x: 1, y: 0.5)
    return gradientLayer
  }()

  private let avatarView: UIImageView = {
    let imageView = UIImageView()
    imageView.backgroundColor = UIColor(hexString: "#F5F5F5")
    imageView.contentMode = .scaleAspectFill
    imageView.layer.cornerRadius = 20
    imageView.clipsToBounds = true
    imageView.layer.borderWidth = 1
    imageView.layer.borderColor = UIColor(hexString: "#000000").withAlphaComponent(0.08).cgColor
    return imageView
  }()

  private let nicknameLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(hexString: "#1E1F27")
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()

  private lazy var inviteButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle("邀请", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
    button.layer.cornerRadius = 8
    button.layer.masksToBounds = true
    button.contentEdgeInsets = UIEdgeInsets(top: 0, left: 12, bottom: 0, right: 12)
    button.layer.insertSublayer(gradientLayer, at: 0)
    button.addTarget(self, action: #selector(inviteButtonTapped), for: .touchUpInside)
    return button
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    setupUI()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func setupUI() {
    backgroundColor = .clear
    selectionStyle = .none

    contentView.addSubview(avatarView)
    contentView.addSubview(nicknameLabel)
    contentView.addSubview(inviteButton)

    avatarView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(16)
      make.centerY.equalToSuperview()
      make.size.equalTo(CGSize(width: 40, height: 40))
    }
    nicknameLabel.snp.makeConstraints { make in
      make.left.equalTo(avatarView.snp.right).offset(12)
      make.centerY.equalToSuperview()
      make.right.lessThanOrEqualTo(inviteButton.snp.left).offset(-8)
    }
    inviteButton.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-16)
      make.centerY.equalToSuperview()
      make.height.equalTo(32)
    }

    layoutIfNeeded()
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    gradientLayer.frame = inviteButton.bounds
  }

  func configure(with item: NELiveStreamRoomInfo, isInvited: Bool) {
    self.isInvited = isInvited
    nicknameLabel.text = item.anchor?.userName
    if let avatar = item.anchor?.icon, !avatar.isEmpty {
      avatarView.sd_setImage(with: URL(string: avatar), placeholderImage: NELiveStreamUI.ne_livestream_imageName("default_avatar"))
    } else {
      avatarView.image = NELiveStreamUI.ne_livestream_imageName("default_avatar")
    }

    inviteButton.setTitle(isInvited ? "取消邀请" : "邀请", for: .normal)
    inviteButton.layoutIfNeeded()
  }

  @objc private func inviteButtonTapped() {
    delegate?.pkInviteCellDidTapInvite(self, isInvited: isInvited)
  }
}

extension NELiveStreamPKInviteListViewController: NELiveStreamPKInviteCellDelegate {
  func pkInviteCellDidTapInvite(_ cell: NELiveStreamPKInviteCell, isInvited: Bool) {
    guard let indexPath = tableView.indexPath(for: cell) else { return }
    let item = pkInviteList[indexPath.row]

    guard let roomUuid = item.liveModel?.roomUuid else {
      NELiveStreamUILog.errorLog(kitTag, desc: "Failed invite pk: roomUuid or userUuid is nil")
      return
    }

    if isInvited {
      // Cancel invitation
      NELiveStreamKit.getInstance().coHostManager.cancelRequest(roomUuid: roomUuid) { [weak self] code, msg, _ in
        guard let self = self else { return }
        if code == 0 {
          self.fetchPKInviteList()
          NELiveStreamUILog.successLog(self.kitTag, desc: "Successfully cancel pk invitation.")
        } else {
          self.fetchPKInviteList()
          NELiveStreamUILog.errorLog(self.kitTag, desc: "Failed cancel pk invitation. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(msg ?? "")
        }
      }
    } else {
      NELiveStreamKit.getInstance().coHostManager.requestConnection(roomUuid: roomUuid, timeoutSeconds: 5) { [weak self] code, msg, _ in
        guard let self = self else { return }
        if code == 0 {
          self.fetchPKInviteList()
          NELiveStreamUILog.successLog(self.kitTag, desc: "Successfully invite pk.")
          NELiveStreamToast.show("PK邀请已发送")
        } else {
          self.fetchPKInviteList()
          NELiveStreamUILog.errorLog(self.kitTag, desc: "Failed invite pk. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(msg ?? "")
        }
      }
    }
  }
}
