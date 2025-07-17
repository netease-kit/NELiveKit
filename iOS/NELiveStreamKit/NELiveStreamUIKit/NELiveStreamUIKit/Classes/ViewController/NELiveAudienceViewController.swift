//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NELivePlayerFramework
import NELiveStreamKit
import NERoomKit
import NESocialUIKit
import NIMSDK
import SDWebImage
import UIKit

let audienceControllerTag: String = "audienceController"

class NELiveAudienceViewController: UIViewController {
  // MARK: - Properties

  var roomInfo: NELiveStreamRoomInfo
  var roomContext: NEPreviewRoomContext?
  var player: NELivePlayerController?
  private var isRoomEnded: Bool = false // 添加房间结束状态标记

  // 观众在房间中的角色
  var role: NELiveStreamRoomRole = .audience

  // 添加重试相关属性
  var retryCount: Int = 0
  let maxRetryCount: Int = 10
  var retryTimer: Timer?

  // 添加 seatItems 属性
  var seatItems: [NELiveStreamSeatItem]?

  // 添加定时器属性
  private var audienceListTimer: Timer?

  // 添加 mutiConnectView 属性
  lazy var mutiConnectView: NELiveStreamMutiConnectView = {
    let view = NELiveStreamMutiConnectView(dataSource: [], frame: .zero)
    view.isHidden = true
    view.delegate = self
    return view
  }()

  // 当前显示的邀请弹框
  var currentInviteAlert: NELiveStreamConnectInviteViewController?

  private lazy var applyConnectVC: NELiveStreamConnectListViewController = {
    let vc = NELiveStreamConnectListViewController(roomUuid: "")
    vc.delegate = self
    return vc
  }()

  // MARK: - UI Components

  lazy var localPlayerRender: UIView = {
    let view = UIView()
    view.backgroundColor = .black
    view.contentMode = .scaleAspectFill
    return view
  }()

  lazy var localRender: UIView = {
    let view = UIView()
    view.backgroundColor = .clear
    view.contentMode = .scaleAspectFill
    view.isHidden = true
    return view
  }()

  private lazy var anchorInfoView: NELiveStreamAnchorTopInfoView = {
    let view = NELiveStreamAnchorTopInfoView()
    view.configure(avatar: roomInfo.anchor?.icon, nickname: roomInfo.liveModel?.liveTopic, roomId: roomInfo.liveModel?.roomUuid)
    return view
  }()

  private lazy var audienceNumView: NELiveStreamAudienceNum = {
    let view = NELiveStreamAudienceNum()
    view.reload(with: [])
    return view
  }()

  private lazy var chatroomView: NESocialChatroomView = .init()

  private lazy var closeButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setImage(NELiveStreamUI.ne_livestream_imageName("close_room"), for: .normal)
    button.backgroundColor = UIColor.clear
    button.layer.cornerRadius = 14
    button.addTarget(self, action: #selector(closeButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var footerView: NELiveStreamFooterView = {
    let view = NELiveStreamFooterView()
    view.delegate = self // 设置代理
    view.updateRole(isAnchor: false) // 设置为观众角色
    return view
  }()

  private lazy var liveEndView: NELiveStreamEndView = {
    let view = NELiveStreamEndView()
    view.isHidden = true
    return view
  }()

  private lazy var keyboardToolbar: NELiveStreamKeyboardToolbarView = {
    let toolbar = NELiveStreamKeyboardToolbarView(frame: CGRectMake(0, UIScreen.main.bounds.size.height,
                                                                    UIScreen.main.bounds.size.width, 50))
    toolbar.delegate = self
    toolbar.isHidden = true
    return toolbar
  }()

  private lazy var anchorLeaveView: NELiveStreamAnchorLeaveView = {
    let view = NELiveStreamAnchorLeaveView()
    view.isHidden = true
    return view
  }()

  public lazy var reachability: NESocialReachability? = {
    let reachability = try? NESocialReachability(hostname: "163.com")
    return reachability
  }()

  // MARK: - Initialization

  init(roomInfo: NELiveStreamRoomInfo) {
    self.roomInfo = roomInfo
    super.init(nibName: nil, bundle: nil)
    modalPresentationStyle = .fullScreen
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Lifecycle

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    addRoomObserver()
    joinRoom()
    observeKeyboard() // 添加键盘观察
    addPlayerObserver()
  }

  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(true, animated: true)
    UIApplication.shared.isIdleTimerDisabled = true
  }

  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    navigationController?.setNavigationBarHidden(false, animated: true)
    UIApplication.shared.isIdleTimerDisabled = false
  }

  deinit {
    leaveRoom()
    cleanupResources()
  }

  // 添加资源清理方法
  private func cleanupResources() {
    player?.shutdown()
    player = nil

    // 停止定时器
    stopAudienceListTimer()
    NotificationCenter.default.removeObserver(self)
  }

  // MARK: - Private Methods

  private func setupUI() {
    view.backgroundColor = .black
    view.addSubview(localPlayerRender)
    view.addSubview(localRender)
    view.addSubview(mutiConnectView)
    // 添加暂停视图
    view.addSubview(anchorLeaveView)
    view.addSubview(anchorInfoView)
    view.addSubview(audienceNumView)
    view.addSubview(footerView)
    view.addSubview(chatroomView)

    // 添加直播结束视图
    view.addSubview(liveEndView)
    liveEndView.configure(
      avatarUrl: roomInfo.anchor?.icon,
      liveTopic: roomInfo.liveModel?.liveTopic
    )
    // 添加键盘工具栏
    view.addSubview(keyboardToolbar)
    view.addSubview(closeButton)

    setupConstraints()
  }

  private func setupConstraints() {
    localPlayerRender.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    localRender.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    mutiConnectView.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(20)
      make.right.equalToSuperview().offset(-10)
      make.bottom.equalToSuperview().offset(-20)
      make.width.equalTo(100)
    }

    closeButton.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-8)
      make.centerY.equalTo(anchorInfoView)
      make.size.equalTo(CGSize(width: 28, height: 28))
    }

    audienceNumView.snp.makeConstraints { make in
      make.right.equalTo(closeButton.snp.left).offset(-8)
      make.centerY.equalTo(anchorInfoView)
      make.size.equalTo(CGSize(width: 140, height: 28))
    }

    anchorInfoView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(8)
      make.top.equalTo(view.safeAreaLayoutGuide).offset(4)
      make.height.equalTo(36)
      make.right.equalTo(audienceNumView.snp.left).offset(-8)
    }

    footerView.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.bottom.equalTo(view.safeAreaLayoutGuide)
      make.height.equalTo(50)
    }

    chatroomView.snp.makeConstraints { make in
      make.bottom.greaterThanOrEqualTo(footerView.snp.top).offset(-16)
      make.height.greaterThanOrEqualTo(100)
      make.bottom.equalTo(footerView.snp.top).offset(-16)
      make.left.equalToSuperview().offset(8)
      make.width.lessThanOrEqualTo(280)
    }

    liveEndView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    anchorLeaveView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }
  }

  private func joinRoom() {
    let joinParam = NEJoinLiveStreamRoomParams()
    joinParam.nick = NELiveStreamUIManager.shared.nickname
    joinParam.role = .audience
    joinParam.roomInfo = roomInfo
    NELiveStreamInnerSingleton.sharedInstance().roomInfo = roomInfo

    NELiveStreamKit.getInstance().joinRoom(joinParam) { [weak self] code, msg, info in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.infoLog(audienceControllerTag, desc: "加入直播间成功")
          self?.startStream()
          // 在加入房间成功后开始获取观众列表
          self?.updateAudienceList()
          self?.startAudienceListTimer()
        } else {
          NELiveStreamUILog.errorLog(audienceControllerTag, desc: "加入直播间失败")
          self?.showJoinFailedAlert()
        }
      }
    }
  }

  private func leaveRoom() {
    // 只有在非房间结束的情况下才调用离开房间接口
    if !isRoomEnded {
      NELiveStreamKit.getInstance().leaveRoom { code, msg, obj in
        DispatchQueue.main.async {
          if code == 0 {
            NELiveStreamUILog.infoLog(audienceControllerTag, desc: "离开直播间成功")
          } else {
            NELiveStreamUILog.errorLog(audienceControllerTag, desc: "离开直播间失败")
          }
        }
      }
    }
  }

  func showJoinFailedAlert() {
    let alert = UIAlertController(
      title: NSLocalizedString("提示", comment: ""),
      message: NSLocalizedString("加入直播间失败", comment: ""),
      preferredStyle: .alert
    )

    alert.addAction(UIAlertAction(
      title: NSLocalizedString("确定", comment: ""),
      style: .default
    ) { [weak self] _ in
      self?.navigationController?.popViewController(animated: true)
    })

    present(alert, animated: true)
  }

  func checkNetwork() -> Bool {
    if reachability?.connection == .cellular || reachability?.connection == .wifi {
      return true
    }
    return false
  }

  // MARK: - Actions

  @objc private func closeButtonClick() {
    leaveRoom()
    navigationController?.popViewController(animated: true)
  }

  private func showPlayEndAlert() {
    // 不再显示 Alert，而是显示直播结束视图
    UIView.animate(withDuration: 0.3) {
      self.liveEndView.isHidden = true
      self.anchorInfoView.isHidden = true
      self.audienceNumView.isHidden = true
      self.footerView.isHidden = true
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

  // MARK: - Audience List Methods

  private func startAudienceListTimer() {
    // 确保先停止已存在的定时器
    stopAudienceListTimer()

    // 创建新的定时器
    audienceListTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
      self?.updateAudienceList()
    }
  }

  private func stopAudienceListTimer() {
    audienceListTimer?.invalidate()
    audienceListTimer = nil
  }

  private func updateAudienceList() {
    // 确保有直播记录ID
    guard let liveRecordId = roomInfo.liveModel?.liveRecordId else {
      NELiveStreamUILog.errorLog(
        audienceControllerTag,
        desc: "Failed to get audience list. LiveRecordId don't exist."
      )
      return
    }

    // 调用获取观众列表接口
    NELiveStreamKit.getInstance().getAudienceList(Int64(liveRecordId), pageNum: 1, pageSize: 50) { [weak self] code, msg, audienceList in
      guard let self = self else { return }

      DispatchQueue.main.async {
        if code == 0, let audienceList = audienceList {
          // 更新观众人数显示
          self.audienceNumView.updateCount(audienceList.total)

          // 更新观众头像列表
          self.audienceNumView.reload(with: audienceList.list)

          NELiveStreamUILog.successLog(
            audienceControllerTag,
            desc: "Successfully get audience list. Count: \(audienceList.total)"
          )
        } else {
          NELiveStreamUILog.errorLog(
            audienceControllerTag,
            desc: "Failed to get audience list. Code: \(code). Msg: \(msg ?? "")"
          )
        }
      }
    }
  }

  // 修改 showLiveEndView 方法，添加停止定时器
  private func showLiveEndView() {
    isRoomEnded = true

    // 停止播放器
    player?.shutdown()
    player = nil

    // 停止观众列表定时器
    stopAudienceListTimer()

    dismiss(animated: false)

    // 显示结束页面
    UIView.animate(withDuration: 0.3) {
      self.liveEndView.isHidden = false
      self.anchorInfoView.isHidden = true
      self.audienceNumView.isHidden = true
      self.footerView.isHidden = true
    }
  }

  @objc private func handleBackgroundTap() {
    view.endEditing(true)
  }

  func showAnchorLeaveView() {
    anchorLeaveView.show()
  }

  func hideAnchorLeaveView() {
    anchorLeaveView.hide()
  }
}

// MARK: - NELiveStreamFooterDelegate

extension NELiveAudienceViewController: NELiveStreamFooterDelegate {
  func footerDidReceiveMicMuteAction(_ mute: Bool) {
    // 观众端通常不需要处理麦克风事件
  }

  func footerDidReceiveGiftClick() {
    // 处理礼物点击
    // 显示礼物面板等
    NELiveStreamSendGiftViewController.show(withTarget: self, viewController: self)
  }

  func footerDidReceiveMusicClickAction() {
    // 观众端通常不需要处理音乐事件
  }

  func footerDidReceiveMenuClickAction() {
    // 处理菜单点击
    // 显示更多菜单等
  }

  func footerDidReceiveInputViewClickAction() {
    // 处理输入框点击
    keyboardToolbar.isHidden = false
    keyboardToolbar.becomeFirstResponse()
  }

  func footerDidReceiveNoSpeakingAction() {
    // 处理禁言事件
  }

  func footerDidReceiveMoreClickAction() {
    let moreVC = NELiveStreamMoreFunctionVC()
    moreVC.delegate = self
    moreVC.updateRole(isAnchor: false)
    let nav = NEUIActionSheetNavigationController(rootViewController: moreVC)
    nav.dismissOnTouchOutside = true
    present(nav, animated: true)
  }

  func footerDidLinkMicClick() {
    let nav = NEUIActionSheetNavigationController(rootViewController: applyConnectVC)
    nav.dismissOnTouchOutside = true
    present(nav, animated: true)
  }
}

// MARK: - Keyboard Handling

extension NELiveAudienceViewController {
  func observeKeyboard() {
    let center = NotificationCenter.default
    center.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIResponder.keyboardWillShowNotification, object: nil)
    center.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIResponder.keyboardWillHideNotification, object: nil)
  }

  @objc func keyboardWillShow(notification: Notification) {
    if let userInfo = notification.userInfo,
       let rect = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect,
       let duration = userInfo[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double {
      let keyboardHeight = rect.size.height
      UIView.animate(withDuration: duration) { [weak self] in
        if let weakSelf = self {
          weakSelf.keyboardToolbar.isHidden = false
          weakSelf.keyboardToolbar.frame = CGRectMake(0, UIScreen.main.bounds.size.height - keyboardHeight - 50,
                                                      UIScreen.main.bounds.size.width, 50)
        }
      }
    }
  }

  @objc func keyboardWillHide(notification: Notification) {
    UIView.animate(withDuration: 0.1) { [weak self] in
      if let weakSelf = self {
        weakSelf.keyboardToolbar.isHidden = true
        weakSelf.keyboardToolbar.frame = CGRect(x: 0, y: UIScreen.main.bounds.size.height + 50,
                                                width: weakSelf.view.bounds.width, height: 50)
      }
    }
  }

  override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    super.touchesBegan(touches, with: event)
    keyboardToolbar.resignFirstResponder()
    view.endEditing(true)
  }
}

// MARK: - NELiveStreamKeyboardToolbarDelegate

extension NELiveAudienceViewController: NELiveStreamKeyboardToolbarDelegate {
  func didToolBarSendText(_ text: String) {
    // 1. 检查文本是否为空
    let set = CharacterSet.whitespacesAndNewlines
    let text = text.trimmingCharacters(in: set)
    guard !text.isEmpty else {
      NELiveStreamToast.show(NELiveStreamBundle.localized("Message_Empty"))
      return
    }
    // 2. 发送文本消息
    NELiveStreamKit.getInstance().sendTextMessage(text) { [weak self] code, msg, _ in
      guard let self = self else { return }

      DispatchQueue.main.async {
        if code == 0 {
          // 发送成功
          NELiveStreamUILog.successLog("SendMessage", desc: "Send message success")

          let message = NESocialChatroomTextMessage()
          message.sender = NELiveStreamUIManager.shared.nickname
          message.text = text
          message.iconSize = CGSize(width: 32, height: 16)
          message.icon = nil
          self.chatroomView.addMessage(message)

        } else {
          // 发送失败
          NELiveStreamUILog.errorLog(
            "SendMessage",
            desc: "Failed to send message. Code: \(code). Msg: \(msg ?? "")"
          )
        }
      }
    }
  }
}

// MARK: - NELiveStreamMoreFunctionDelegate

extension NELiveAudienceViewController: NELiveStreamMoreFunctionDelegate {
  func didSwitchCamera() {
    // 如果是观众角色，直接返回
    guard role != NELiveStreamRoomRole.audience else {
      NELiveStreamToast.show("未上麦观众不支持操作摄像头")
      return
    }
    // 实现翻转摄像头
    NELiveStreamKit.getInstance().switchCamera()
  }

  func didToggleLive(_ isLive: Bool) -> Bool {
    false
  }

  func didCommentSetting() {
    // 实现评论设置
  }

  func didScreenOrientation() {
    // 实现屏幕方向切换
  }

  func didPictureInPicture() {
    // 实现小窗播放
  }

  func didQualityData() {
    // 实现质量数据显示
  }

  func didAdvancedSetting() {
    // 实现高级设置
  }
}

// MARK: - NELiveStreamRoomListener

extension NELiveAudienceViewController: NELiveStreamListener {
  func onMemberJoinChatroom(_ members: [NELiveStreamMember]) {
    let messages = members.map { member in
      let message = NESocialChatroomNotiMessage()
      message.notification = String(format: "%@ %@", member.name, NELiveStreamBundle.localized("Join_Room"))
      return message
    }
    chatroomView.addMessages(messages)
  }

  func onMemberLeaveChatroom(_ members: [NELiveStreamMember]) {
    let messages = members.map { member in
      let message = NESocialChatroomNotiMessage()
      message.notification = String(format: "%@ %@", member.name, NELiveStreamBundle.localized("Leave_Room"))
      return message
    }
    chatroomView.addMessages(messages)
  }

  func onReceiveTextMessage(_ message: NELiveStreamChatTextMessage) {
    let isOwner = message.fromUserUuid == roomInfo.anchor?.userUuid
    let text = NESocialChatroomTextMessage()
    text.sender = message.fromNick ?? message.fromUserUuid
    text.text = message.text
    text.iconSize = CGSize(width: 32, height: 16)
    text.icon = isOwner ? NELiveStreamUI.ne_livestream_imageName(NELiveStreamBundle.localized("Owner_Icon")) : nil
    chatroomView.addMessage(text)
  }

  func onReceiveBatchGift(giftModel: NELiveStreamBatchGiftModel) {
    if let model = NESocialGiftModel.getGift(giftId: giftModel.giftId) {
      let messages: [NESocialChatroomMessage] = giftModel.rewardeeUsers.map { userRewardee in
        let message = NESocialChatroomRewardMessage()
        message.giftImage = model.icon
        message.giftImageSize = CGSize(width: 20, height: 20)
        message.sender = giftModel.rewarderUserName
        message.receiver = userRewardee.userName
        message.rewardText = NELiveStreamBundle.localized("Send_Gift_To")
        message.rewardColor = UIColor(white: 1, alpha: 0.6)
        message.giftColor = UIColor(hexString: "#FFD966")
        message.giftCount = giftModel.giftCount
        message.giftName = model.displayName
        return message
      }
      chatroomView.addMessages(messages)
    }
  }

  func onRoomEnded(_ reason: NELiveStreamEndReason) {
    showLiveEndView()
  }

  func onLivePause() {
    stopStream()
    showAnchorLeaveView()
  }

  func onLiveResume() {
    startStream()
    hideAnchorLeaveView()
  }
}

extension NELiveAudienceViewController: NELiveStreamSendGiftViewtDelegate {
  func didSendGift(_ gift: NELiveStreamUIGiftModel, giftCount: Int32, userUuids: [Any]) {
    guard checkNetwork() else {
      return
    }

    dismiss(animated: true) {
      // 强制转换为 [String] 类型
      guard let stringUuids = userUuids as? [String] else {
        NELiveStreamUILog.errorLog(audienceControllerTag, desc: "sendGift userUuids 包含非字符串类型")
        return
      }

      NELiveStreamKit.getInstance().sendBatchGift(
        Int(gift.giftId),
        giftCount: Int(giftCount),
        userUuids: stringUuids, // 使用转换后的安全值
        callback: { code, msg, obj in
          if code != 0 {
            let message = String(format: "%@ %d %@", NELiveStreamBundle.localized("发送礼物失败"), code, msg ?? "")
            NELiveStreamToast.show(message)
          }
        }
      )
    }
  }
}
