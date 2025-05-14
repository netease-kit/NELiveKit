// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NELiveStreamKit
import SDWebImage
import UIKit

protocol NELiveStreamConnectListViewControllerDelegate: AnyObject {
  func didLeaveSeat()
}

// 观众连麦列表 controller
class NELiveStreamConnectListViewController: UIViewController {
  // MARK: - Properties

  private var currentState: ApplyState = .empty
  private var roomUuid: String

  weak var delegate: NELiveStreamConnectListViewControllerDelegate?

  enum ApplyState {
    case empty // 空状态
    case applying // 申请中状态
    case linking // 连麦中
  }

  private lazy var emptyView: NELiveStreamEmptyView = {
    let view = NELiveStreamEmptyView()
    return view
  }()

  private lazy var maskView: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor.black.withAlphaComponent(0.5) // 改用半透明黑色背景
    return view
  }()

  // MARK: - UI Components

  private lazy var applyButtonBgImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = NELiveStreamUI.ne_livestream_imageName("live_request_link_mic_bg_icon")
    imageView.contentMode = .scaleToFill
    imageView.layer.cornerRadius = 24 // 48/2
    imageView.layer.masksToBounds = true
    return imageView
  }()

  private lazy var applyButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle("可申请连麦", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
    button.backgroundColor = .clear
    button.layer.cornerRadius = 24 // 48/2
    button.layer.masksToBounds = true
    button.addTarget(self, action: #selector(applyButtonClick), for: .touchUpInside)
    return button
  }()

  // 申请中状态视图
  private lazy var applyingView: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor(hexString: "#337EFF").withAlphaComponent(0.2) // fill_YEFNP9
    view.layer.cornerRadius = 8
    view.layer.masksToBounds = true
    return view
  }()

  private lazy var applyingIconView: UIImageView = {
    let imageView = UIImageView()
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()

  private lazy var applyingLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(hexString: "#333333") // fill_PGF2TS
    label.font = .systemFont(ofSize: 14, weight: .medium) // style_AZ2MFO
    return label
  }()

  private lazy var actionButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitleColor(UIColor(hexString: "#337EFF"), for: .normal) // fill_PP89OJ
    button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium) // style_AZ2MFO
    button.semanticContentAttribute = .forceRightToLeft // 图标在右侧
    button.imageEdgeInsets = UIEdgeInsets(top: 0, left: 4, bottom: 0, right: -4)
    button.addTarget(self, action: #selector(actionButtonClick), for: .touchUpInside)
    return button
  }()

  private var linkTimer: Timer?
  private var linkDuration: TimeInterval = 0

  private lazy var queueLabel: UILabel = {
    let label = UILabel()
    label.text = "待上麦队列"
    label.textColor = UIColor(hexString: "#666666") // fill_Y6P2CL
    label.font = .systemFont(ofSize: 12) // style_9DAJ82
    label.textAlignment = .left
    return label
  }()

  private lazy var userListView: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    return view
  }()

  private lazy var tableView: UITableView = {
    let tableView = UITableView()
    tableView.backgroundColor = .clear
    tableView.separatorStyle = .none
    tableView.delegate = self
    tableView.dataSource = self
    tableView.register(NELiveStreamUserListCell.self, forCellReuseIdentifier: "NELiveStreamUserListCell")
    tableView.isScrollEnabled = true
    tableView.showsVerticalScrollIndicator = false
    return tableView
  }()

  private var userList: [NELiveStreamSeatRequestItem] = [] {
    didSet {
      updateQueueView()
    }
  }

  // MARK: - Initialization

  init(roomUuid: String) {
    self.roomUuid = roomUuid
    super.init(nibName: nil, bundle: nil)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  deinit {
    removeRoomObserver()
    NotificationCenter.default.removeObserver(self)
  }

  // MARK: - Lifecycle

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    updateState(.empty)
    updateSeatRequestList()
    addRoomObserver()

    // 添加选择视图消失的通知观察
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(handleSelectionViewDismissed),
                                           name: NSNotification.Name("NELiveStreamConnectTypeSelectionViewDismissed"),
                                           object: nil)

    preferredContentSize = CGSize(width: UIScreen.main.bounds.width, height: 300)
  }

  // MARK: - Private Methods

  private func setupUI() {
    title = NSLocalizedString("申请连麦", comment: "")
    view.backgroundColor = .white

    // 状态视图
    view.addSubview(applyingView)
    applyingView.addSubview(applyingIconView)
    applyingView.addSubview(applyingLabel)
    applyingView.addSubview(actionButton)

    // 连麦队列视图
    view.addSubview(userListView)
    userListView.addSubview(emptyView)
    userListView.addSubview(queueLabel)
    userListView.addSubview(tableView)

    //
    view.addSubview(applyButtonBgImageView)
    view.addSubview(applyButton)
    setupConstraints()
  }

  private func setupConstraints() {
    applyingView.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(16)
      make.left.equalToSuperview().offset(16)
      make.right.equalToSuperview().offset(-16)
      make.height.equalTo(48)
    }

    applyingIconView.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(24)
      make.centerX.equalToSuperview()
      make.size.equalTo(CGSize(width: 48, height: 48))
    }

    applyingLabel.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(16)
      make.centerY.equalToSuperview()
    }

    actionButton.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-16)
      make.centerY.equalToSuperview()
    }

    userListView.snp.makeConstraints { make in
      if applyingView.isHidden {
        make.top.equalToSuperview()
      } else {
        make.top.equalTo(applyingView.snp.bottom).offset(16)
      }
      make.left.right.bottom.equalToSuperview()
    }

    emptyView.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview().offset(-100)
    }

    queueLabel.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(16)
      make.left.equalToSuperview().offset(16)
      make.right.equalToSuperview()
    }

    tableView.snp.makeConstraints { make in
      make.top.equalTo(queueLabel.snp.bottom).offset(12)
      make.left.right.bottom.equalToSuperview()
    }

    // 申请连麦按钮背景
    applyButtonBgImageView.snp.makeConstraints { make in
      make.bottom.equalToSuperview().offset(-30)
      make.centerX.equalToSuperview()
      make.size.equalTo(CGSize(width: 335, height: 48))
    }

    // 申请连麦按钮
    applyButton.snp.makeConstraints { make in
      make.edges.equalTo(applyButtonBgImageView)
    }
  }

  private func updateQueueView() {
    tableView.reloadData()
  }

  private func updateState(_ state: ApplyState) {
    if currentState != state {
      NELiveStreamUILog.infoLog(audienceControllerTag, desc: "change audience apply state: \(state)")
    }

    currentState = state
    emptyView.isHidden = userList.count > 0
    updateQueueView()

    // 根据状态显示对应视图
    switch state {
    case .empty:
      applyingView.isHidden = true
      applyButton.isHidden = false
      applyButtonBgImageView.isHidden = false

    case .linking:
      applyingView.isHidden = false
      applyButton.isHidden = true
      applyButtonBgImageView.isHidden = true
      applyingIconView.image = NELiveStreamUI.ne_livestream_imageName("live_linking_icon")
      applyingLabel.text = "连麦中"
      actionButton.setTitle("结束连麦", for: .normal)
      actionButton.setImage(NELiveStreamUI.ne_livestream_imageName("live_close_red_icon"), for: .normal)

    case .applying:
      applyingView.isHidden = false
      applyButton.isHidden = true
      applyButtonBgImageView.isHidden = true
      applyingIconView.image = NELiveStreamUI.ne_livestream_imageName("live_waiting_icon")
      applyingLabel.text = "等待确认上麦中"
      actionButton.setTitle("撤销申请", for: .normal)
      actionButton.setImage(NELiveStreamUI.ne_livestream_imageName("live_arrow_right"), for: .normal)
    }

    // 移除旧的约束后重新设置
    userListView.snp.removeConstraints()
    applyingView.snp.removeConstraints()

    setupConstraints()
  }

  @objc private func endLinkButtonClick() {
    NELiveStreamKit.getInstance().leaveSeat { [weak self] code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(audienceControllerTag, desc: "Successfully leave seat.")
          self?.delegate?.didLeaveSeat()
        } else {
          NELiveStreamUILog.errorLog(audienceControllerTag,
                                     desc: "Failed to leave seat. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(String(format: "结束连麦失败: \(msg ?? "")"))
        }

        self?.updateSeatRequestList()
      }
    }
  }

  @objc private func cancelButtonClick() {
    NELiveStreamKit.getInstance().cancelSeatRequest { [weak self] code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(audienceControllerTag, desc: "Successfully cancel seat request.")
        } else {
          NELiveStreamUILog.errorLog(audienceControllerTag,
                                     desc: "Failed to cancel seat request. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(String(format: "撤销连麦失败: \(msg ?? "")"))
        }

        self?.updateSeatRequestList()
      }
    }
  }

  // MARK: - Room Observer

  private func addRoomObserver() {
    // 添加房间结束监听
    NELiveStreamKit.getInstance().addLiveStreamListener(self)
  }

  private func removeRoomObserver() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(self)
  }

  @objc private func handleSelectionViewDismissed() {
    maskView.alpha = 0
    maskView.removeFromSuperview()
  }
}

// MARK: - NELiveStreamUserListCellDelegate

protocol NELiveStreamUserListCellDelegate: AnyObject {
  func userListCellDidTapCancel(_ cell: NELiveStreamUserListCell)
}

// MARK: - NELiveStreamUserListCell

class NELiveStreamUserListCell: UITableViewCell {
  weak var delegate: NELiveStreamUserListCellDelegate?

  private let indexLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .medium) // style_3X8B12
    label.textAlignment = .center
    return label
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
    label.textColor = UIColor(hexString: "#1E1F27") // fill_ZUUXU0
    label.font = .systemFont(ofSize: 16) // style_D2HTTC
    return label
  }()

  private lazy var cancelButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle("撤销申请", for: .normal)
    button.setTitleColor(UIColor(hexString: "#333333"), for: .normal) // fill_PGF2TS
    button.titleLabel?.font = .systemFont(ofSize: 14) // style_YNJW6M
    button.backgroundColor = .white
    button.layer.cornerRadius = 8
    button.layer.borderWidth = 1
    button.layer.borderColor = UIColor(hexString: "#EBEBEB").cgColor // stroke_BGC4I2
    button.contentEdgeInsets = UIEdgeInsets(top: 5, left: 12, bottom: 5, right: 12) // layout_9YN72A
    button.addTarget(self, action: #selector(cancelButtonClick), for: .touchUpInside)
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

    contentView.addSubview(indexLabel)
    contentView.addSubview(avatarView)
    contentView.addSubview(nicknameLabel)
    contentView.addSubview(cancelButton)

    indexLabel.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(16)
      make.centerY.equalToSuperview()
      make.width.equalTo(16)
    }

    avatarView.snp.makeConstraints { make in
      make.left.equalTo(indexLabel.snp.right).offset(4)
      make.centerY.equalToSuperview()
      make.size.equalTo(CGSize(width: 40, height: 40))
    }

    nicknameLabel.snp.makeConstraints { make in
      make.left.equalTo(avatarView.snp.right).offset(12)
      make.centerY.equalToSuperview()
    }

    cancelButton.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-16)
      make.centerY.equalToSuperview()
      make.height.equalTo(32)
    }
  }

  @objc private func cancelButtonClick() {
    delegate?.userListCellDidTapCancel(self)
  }

  func configure(index: Int, userId: String, nickname: String?, icon: String?, showCancelButton: Bool = false) {
    // 设置序号颜色
    let indexColors = ["#F24957", "#FF7919", "#FFAA00", "#666666"] // 前三个特殊颜色
    let colorIndex = min(index, indexColors.count - 1)
    indexLabel.textColor = UIColor(hexString: indexColors[colorIndex])
    indexLabel.text = "\(index + 1)"

    nicknameLabel.text = nickname ?? userId
    cancelButton.isHidden = !showCancelButton

    // 设置头像
    if let iconUrl = icon, !iconUrl.isEmpty {
      avatarView.sd_setImage(with: URL(string: iconUrl),
                             placeholderImage: NELiveStreamUI.ne_livestream_imageName("default_avatar"))
    } else {
      avatarView.image = NELiveStreamUI.ne_livestream_imageName("default_avatar")
    }
  }
}

// MARK: - UITableViewDelegate & UITableViewDataSource

extension NELiveStreamConnectListViewController: UITableViewDelegate, UITableViewDataSource, NELiveStreamUserListCellDelegate {
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    userList.count
  }

  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: "NELiveStreamUserListCell", for: indexPath) as! NELiveStreamUserListCell
    let user = userList[indexPath.row]
    // 只显示当前用户的撤销申请按钮
    let showCancelButton = user.user == NELiveStreamKit.getInstance().localMember?.account
    cell.configure(index: indexPath.row, userId: user.user, nickname: user.userName, icon: user.icon, showCancelButton: showCancelButton)
    cell.delegate = self
    return cell
  }

  func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    64 // 根据设计稿调整单元格高度
  }

  // MARK: - NELiveStreamUserListCellDelegate

  func userListCellDidTapCancel(_ cell: NELiveStreamUserListCell) {
    cancelButtonClick()
  }
}

// MARK: - NELiveStreamConnectType

enum NELiveStreamConnectType: Int {
  case video = 1 // 视频连麦
  case audio = 2 // 语音连麦
}

// MARK: - Actions

extension NELiveStreamConnectListViewController {
  @objc private func applyButtonClick() {
    submitSeatRequest(type: .video)
  }

  @objc private func actionButtonClick() {
    switch currentState {
    case .linking:
      endLinkButtonClick()
    case .applying:
      cancelButtonClick()
    case .empty:
      break
    }
  }

  func submitSeatRequest(type: NELiveStreamConnectType) {
    NELiveStreamKit.getInstance().requestSeat { [weak self] code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(audienceControllerTag, desc: "Successfully submit seat request.")
          self?.updateState(.applying)
        } else {
          self?.updateState(.empty)
          NELiveStreamUILog.errorLog(audienceControllerTag,
                                     desc: "Failed to submit seat request. Code: \(code). Msg: \(msg ?? "")")
        }

        self?.updateSeatRequestList()
      }
    }
  }
}

// MARK: - Data Methods

extension NELiveStreamConnectListViewController {
  func updateSeatRequestList() {
    NELiveStreamKit.getInstance().getSeatRequestList { [weak self] code, msg, list in
      DispatchQueue.main.async { [self] in
        if code == 0 {
          self?.userList = list ?? []
          self?.tableView.reloadData()

          // 获取当前用户账号
          guard let currentUser = NELiveStreamKit.getInstance().localMember?.account else {
            return
          }

          // 在麦上
          let isOnSeat = NELiveStreamKit.getInstance().localSeats?.contains(where: { seat in
            seat.user == currentUser && seat.status == .taken
          }) ?? false

          guard !isOnSeat else {
            return
          }

          // 检查当前用户是否在申请列表中
          let isApplying = (list ?? []).contains { request in
            request.user == currentUser
          }

          if isApplying {
            self?.updateState(.applying)
          } else if self?.currentState != .linking {
            self?.updateState(.empty)
          }
        } else {
          NELiveStreamUILog.errorLog(audienceControllerTag, desc: "Failed to get seat request list. Code: \(code). Msg: \(msg ?? "")")
        }
      }
    }
  }
}

extension NELiveStreamConnectListViewController: NELiveStreamListener {
  // 麦位信息更新
  func onSeatListChanged(_ seatItems: [NELiveStreamSeatItem]) {
    // 获取当前用户账号
    guard let currentUser = NELiveStreamKit.getInstance().localMember?.account else {
      return
    }

    // 检查是否有当前用户的麦位且状态为taken
    let isLinking = seatItems.contains { seatItem in
      seatItem.user == currentUser && seatItem.status == .taken
    }

    // 更新状态
    if isLinking {
      updateState(.linking)
    } else if currentState == .linking {
      // 如果之前是连麦状态，现在不是了，则更新为空状态
      updateState(.empty)
    }

    // 刷新连麦申请列表
    updateSeatRequestList()
  }

  // 连麦申请拒绝
  func onSeatRequestRejected(_ seatIndex: Int, account: String, operateBy: String) {
    // 刷新连麦申请列表
    updateSeatRequestList()

    // 判断是否自己
    guard account == NELiveStreamKit.getInstance().localMember?.account else {
      return
    }

    NELiveStreamUILog.infoLog(audienceControllerTag, desc: "on Seat Request Rejected. account: \(account), operateBy: \(operateBy)")
    NELiveStreamToast.show("主播已拒绝你的连麦申请")
  }

  func onSeatRequestSubmitted(_ seatIndex: Int, account: String) {
    // 刷新连麦申请列表
    updateSeatRequestList()
  }

  func onSeatRequestCancelled(_ seatIndex: Int, account: String) {
    // 刷新连麦申请列表
    updateSeatRequestList()
  }

  func onSeatRequestApproved(_ seatIndex: Int, account: String, operateBy: String, isAutoAgree: Bool) {
    // 刷新连麦申请列表
    updateSeatRequestList()
  }
}
