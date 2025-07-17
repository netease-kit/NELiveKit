//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import FaceUnity
import Foundation
import NELiveStreamKit
import NEOrderSong
import NERoomKit
import NERtcSDK
import NESocialUIKit
import NIMSDK
import UIKit

/// 歌曲播放状态
enum PlayingStatus: Int {
  case `default`
  case pause
  case playing
}

enum NELiveStreamAnchorStatus: Int {
  ///  默认
  case idle = 0
  /// 单直播
  case soloLive = 1
  /// 观众连麦
  case audienceLink = 2
  /// 主播连线
  case coHosting = 3
}

let anchorControllerTag: String = "anchorController"

@objcMembers
class NELiveAnchorViewController: UIViewController {
  // MARK: - Properties

  var applySeatItems: [NELiveStreamSeatRequestItem]?

  // 添加属性保存房间信息
  var liveInfo: NELiveStreamRoomInfo?

  private var previewRoomContext: NEPreviewRoomContext?
  private var isLiving: Bool = false

  // 当前状态
  var status: NELiveStreamAnchorStatus {
    guard isLiving else {
      return .idle
    }

    let seatTakenCount = NELiveStreamKit.getInstance().localSeats?.filter { $0.status == .taken }.count ?? 0
    let cohostCount = NELiveStreamKit.getInstance().coHostManager.coHostUserList.count
    if seatTakenCount > 1 {
      return .audienceLink
    } else if cohostCount > 0 {
      return .coHosting
    } else {
      return .soloLive
    }
  }

  // 添加定时器属性
  private var audienceListTimer: Timer?

  // 缓存 PK 邀请列表 VC
  private lazy var pkInviteListVC: NELiveStreamPKInviteListViewController = {
    let listVC = NELiveStreamPKInviteListViewController()
    return listVC
  }()

  // MARK: - Preview UI

  private lazy var backButton: UIButton = {
    let button = UIButton()
    button.setImage(NELiveStreamUI.ne_livestream_imageName("live_close_preview_icon"), for: .normal)
    button.addTarget(self, action: #selector(backButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.text = NSLocalizedString("直播房", comment: "")
    label.textColor = .white
    label.font = .systemFont(ofSize: 16)
    return label
  }()

  private lazy var coverSettingPanel: NELiveStreamCoverSetting = {
    let panel = NELiveStreamCoverSetting()
    return panel
  }()

  private lazy var bottomPanel: NELiveStreamBottomPanel = {
    let view = NELiveStreamBottomPanel()
    view.delegate = self
    view.backgroundColor = .clear
    return view
  }()

  private lazy var tipsLabel: UILabel = {
    let label = UILabel()
    label.text = NSLocalizedString("开播提醒", comment: "")
    label.textColor = .white
    label.font = .systemFont(ofSize: 14)
    return label
  }()

  private lazy var startLiveButton: UIButton = {
    let button = UIButton()
    button.setTitle(NSLocalizedString("开始直播", comment: ""), for: .normal)
    button.backgroundColor = .systemBlue
    button.layer.cornerRadius = 22
    button.addTarget(self, action: #selector(startLiveButtonClick), for: .touchUpInside)
    return button
  }()

  // MARK: - Live UI

  lazy var micVc: NETSRequestManageMainController = {
    let micVc = NETSRequestManageMainController(roomId: liveInfo?.liveModel?.roomUuid ?? "")
    micVc.delegate = self
    return micVc
  }()

  lazy var audienceNumView: NELiveStreamAudienceNum = {
    let view = NELiveStreamAudienceNum()
    view.isHidden = true
    view.reload(with: [])
    return view
  }()

  lazy var mutiConnectView: NELiveStreamMutiConnectView = {
    let view = NELiveStreamMutiConnectView(dataSource: [], frame: .zero)
    view.delegate = self
    view.isHidden = true
    return view
  }()

  lazy var pkUIView: NELiveStreamPKUIView = {
    let view = NELiveStreamPKUIView(localRenderView: localRender)
    view.delegate = self
    return view
  }()

  // 当前显示的PK邀请弹框
  var currentPKInviteView: NELiveStreamPKInviteViewController?

  private lazy var localRender: UIView = {
    let view = UIView()
    view.backgroundColor = .black
    return view
  }()

  private lazy var anchorInfoView: NELiveStreamAnchorTopInfoView = {
    let view = NELiveStreamAnchorTopInfoView()
    view.isHidden = true
    view.configure(avatar: liveInfo?.anchor?.icon, nickname: liveInfo?.liveModel?.liveTopic, roomId: liveInfo?.liveModel?.roomUuid)
    return view
  }()

  lazy var footerView: NELiveStreamFooterView = {
    let view = NELiveStreamFooterView()
    view.delegate = self
    view.isHidden = true
    return view
  }()

  private lazy var chatroomView: NESocialChatroomView = {
    let view = NESocialChatroomView()
    view.isHidden = true
    return view
  }()

  // 添加关闭直播按钮
  private lazy var closeButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setImage(NELiveStreamUI.ne_livestream_imageName("close_room"), for: .normal)
    button.backgroundColor = UIColor.clear
    button.layer.cornerRadius = 14
    button.isHidden = true
    button.addTarget(self, action: #selector(closeButtonClick), for: .touchUpInside)
    return button
  }()

  private lazy var keyboardToolbar: NELiveStreamKeyboardToolbarView = {
    let toolbar = NELiveStreamKeyboardToolbarView(frame: CGRectMake(0, UIScreen.main.bounds.size.height,
                                                                    UIScreen.main.bounds.size.width, 50))
    toolbar.delegate = self
    toolbar.isHidden = true
    return toolbar
  }()

  private lazy var pickSongView: NELiveStreamPickSongView = {
    let pickSongView = NELiveStreamPickSongView(frame: CGRectMake(0, 0,
                                                                  UIScreen.main.bounds.size.width, UIScreen.main.bounds.size.height),
                                                detail: self.liveInfo)
    pickSongView.delegate = self
//        pickSongView.isHidden = false
    return pickSongView
  }()

  private lazy var pauseView: NELiveStreamPauseView = {
    let view = NELiveStreamPauseView()
    view.delegate = self
    view.isHidden = true
    return view
  }()

  public lazy var reachability: NESocialReachability? = {
    let reachability = try? NESocialReachability(hostname: "163.com")
    return reachability
  }()

  private lazy var backgroundImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = NELiveStreamUI.ne_livestream_imageName("live_bg")
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()

  // MARK: - Lifecycle

  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    addRoomObserver()
    startPreview()
    checkOngingLive()
    observeKeyboard()
    addSystemObserver()

    var safeAreaBottom: CGFloat = 0.0
    if #available(iOS 11.0, *) {
      safeAreaBottom = view.safeAreaInsets.bottom
    }

    // 美颜
    FUDemoManager.share().show(inTargetController: self, originY: view.frame.height - safeAreaBottom - 49)
    FUDemoManager.share().defaultBarSelectedItem(0)
    FUDemoManager.share().hide()
  }

  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(true, animated: true)
    UIApplication.shared.isIdleTimerDisabled = true
  }

  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    navigationController?.setNavigationBarHidden(false, animated: true)
    stopPreview()
    // 停止定时器
    stopAudienceListTimer()
    UIApplication.shared.isIdleTimerDisabled = false
  }

  deinit {
    // 清理工作
    NotificationCenter.default.removeObserver(self)
    removeRoomObserver()
    stopAudienceListTimer()
    stopPreview()
  }

  // MARK: - Private Methods

  private func addRoomObserver() {
    // 添加房间监听
    NELiveStreamKit.getInstance().addLiveStreamListener(self)
    NELiveStreamKit.getInstance().addCoHostListener(self)
  }

  private func removeRoomObserver() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(self)
    NELiveStreamKit.getInstance().removeCoHostListener(self)
  }

  private func addSystemObserver() {
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(appWillEnterForegournd),
      name: UIApplication.willEnterForegroundNotification,
      object: nil
    )
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(appDidEnterBackground),
      name: UIApplication.didEnterBackgroundNotification,
      object: nil
    )

    // 创建与系统手势相同代理的空白滑动手势（用于覆盖系统返回手势）
    let target = navigationController?.interactivePopGestureRecognizer?.delegate
    let pan = UIPanGestureRecognizer(target: target, action: nil)
    view.addGestureRecognizer(pan)
  }

  func appWillEnterForegournd() {
    guard status == .soloLive else {
      return
    }

    resumeLiveStream()
  }

  func appDidEnterBackground() {
    guard status == .soloLive else {
      return
    }

    pauseLiveStream()
  }

  private func setupUI() {
    view.backgroundColor = .black
    // Add preview UI
    view.addSubview(backgroundImageView)
    view.addSubview(pkUIView)
    pkUIView.delegate = self

    view.addSubview(mutiConnectView)
    view.addSubview(backButton)
    view.addSubview(titleLabel)
    view.addSubview(coverSettingPanel)
    view.addSubview(bottomPanel)
    bottomPanel.addSubview(startLiveButton)

    // 添加暂停视图
    view.addSubview(pauseView)

    // Add live UI
    view.addSubview(anchorInfoView)
    view.addSubview(audienceNumView)
    view.addSubview(footerView)
    view.addSubview(chatroomView)

    // 添加关闭按钮
    view.addSubview(closeButton)

    // 添加键盘工具栏
    view.addSubview(keyboardToolbar)

    setupConstraints()
  }

  private func setupConstraints() {
    // Preview UI constraints
    backgroundImageView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }
    pkUIView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    mutiConnectView.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(20)
      make.right.equalToSuperview().offset(-10)
      make.bottom.equalToSuperview().offset(-20)
      make.width.equalTo(100)
    }

    backButton.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(20)
      make.top.equalTo(view.safeAreaLayoutGuide).offset(12)
      make.size.equalTo(CGSize(width: 24, height: 24))
    }

    pauseView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    titleLabel.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.centerY.equalTo(backButton)
    }

    coverSettingPanel.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(20)
      make.top.equalTo(titleLabel.snp.bottom).offset(20)
      make.right.equalToSuperview().offset(-20)
      make.height.equalTo(44)
    }

    bottomPanel.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.bottom.equalTo(view.safeAreaLayoutGuide)
      make.height.equalTo(180)
    }

    startLiveButton.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.bottom.equalToSuperview().offset(-20)
      make.size.equalTo(CGSize(width: 240, height: 44))
    }

    // 添加关闭按钮的约束
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

    // Live UI constraints
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
      make.height.equalTo(120)
      make.bottom.equalTo(footerView.snp.top).offset(-16)
      make.left.equalToSuperview().offset(8)
      make.width.lessThanOrEqualTo(280)
    }
  }

  func layoutSingleUI() {
    pkUIView.snp.remakeConstraints { make in
      make.edges.equalToSuperview()
    }

    pkUIView.layoutSingleUI()
  }

  func layoutPKUI() {
    pkUIView.snp.remakeConstraints { make in
      make.left.right.equalToSuperview()
      make.top.equalTo(anchorInfoView.snp.bottom).offset(22)
      make.height.equalTo(NEUIScreenAdapter.screenHeight / 2)
    }

    pkUIView.layoutPKUI()
    pkUIView.startCountdown(duration: 180)
  }

  private func startPreview() {
    NELiveStreamUtils.getMeidaPermissions(mediaType: .video) { granted in
      if granted {
        NERoomKit.shared().roomService.previewRoom { [weak self] code, msg, context in
          guard let self = self,
                let context = context else { return }

          let canvas = NERoomVideoView()
          canvas.container = self.localRender
          canvas.renderMode = .cropFill

          _ = context.previewController.setLocalVideoConfig(width: 1280, height: 720, fps: 15)
          context.previewController.startPreview(canvas: canvas)
          context.previewController.setVideoFrameDelegate(self)
          self.previewRoomContext = context
        }
      } else {
        NELiveStreamToast.show(NELiveStreamBundle.localized("Camera_Permission_Disabled"))
      }
    }
  }

  private func stopPreview() {
    previewRoomContext?.previewController.stopPreview()
  }

  private func checkOngingLive() {
    NELiveStreamKit.getInstance().getOngoingLive { [weak self] code, msg, info in
      DispatchQueue.main.async {
        guard let self = self else {
          return
        }

        if self.isLiving {
          NELiveStreamUILog.successLog(
            anchorControllerTag,
            desc: "current is living, cancel get ongoing Live"
          )
          return
        }

        if code == 0, let ongoingLive = info?.liveModel {
          self.liveInfo = info
          NELiveStreamUILog.successLog(
            anchorControllerTag,
            desc: "Successfully get ongoing Live"
          )
          self.showOngoingLiveAlertView()
        } else {
          NELiveStreamUILog.errorLog(
            anchorControllerTag,
            desc: "Failed to get ongoing list. Code: \(code). Msg: \(msg ?? "")"
          )
        }
      }
    }
  }

  private func switchToLiveMode() {
    // Hide preview UI
    backButton.isHidden = true
    titleLabel.isHidden = true
    coverSettingPanel.isHidden = true
    bottomPanel.isHidden = true

    // Show live UI
    pkUIView.isHidden = false
    anchorInfoView.isHidden = false
    audienceNumView.isHidden = false
    footerView.isHidden = false
    closeButton.isHidden = false
    chatroomView.isHidden = false

    // Set initial role
    footerView.updateRole(isAnchor: true)
    isLiving = true
  }

  func checkNetwork() -> Bool {
    if reachability?.connection == .cellular || reachability?.connection == .wifi {
      return true
    }
    return false
  }

  // MARK: - Actions

  @objc private func backButtonClick() {
    navigationController?.popViewController(animated: true)
  }

  @objc private func startLiveButtonClick() {
    var hasPermissions = false
    let semaphore = DispatchSemaphore(value: 0)
    NELiveStreamUtils.getMeidaPermissions(mediaType: .audio) { authorized in
      if authorized {
        NELiveStreamUtils.getMeidaPermissions(mediaType: .video) { authorized in
          if authorized {
            hasPermissions = true
            semaphore.signal()
          } else {
            NELiveStreamToast.show(NELiveStreamBundle.localized("Camera_Permission_Disabled"))
            semaphore.signal()
          }
        }
      } else {
        NELiveStreamUtils.getMeidaPermissions(mediaType: .video) { authorized in
          if authorized {
            NELiveStreamToast.show(NELiveStreamBundle.localized("Microphone_Permission_Disabled"))
            semaphore.signal()
          } else {
            NELiveStreamToast.show(NELiveStreamBundle.localized("Microphone_Camera_Permission_Disabled"))
            semaphore.signal()
          }
        }
      }
    }

    semaphore.wait()
    if !hasPermissions {
      return
    }

    NELiveStreamToast.showLoading()
    let createParam = NECreateLiveStreamRoomParams()
    createParam.configId = NELiveStreamUIManager.shared.configId
    createParam.liveTopic = coverSettingPanel.roomName
    createParam.seatCount = 4 // 麦位数量 默认4个

    NELiveStreamKit.getInstance().createRoom(createParam) { [weak self] code, msg, info in
      DispatchQueue.main.async {
        if code == 0, let info = info {
          NELiveStreamUILog.infoLog(anchorControllerTag, desc: "创建直播成功")
          // 保存返回的房间信息
          self?.liveInfo = info

          let joinParam = NEJoinLiveStreamRoomParams()
          joinParam.nick = NELiveStreamUIManager.shared.nickname
          joinParam.role = .host
          joinParam.roomInfo = info

          self?.anchorInfoView.configure(avatar: info.anchor?.icon, nickname: info.liveModel?.liveTopic, roomId: info.liveModel?.roomUuid)

          // 版权设置为听歌场景
          NEOrderSong.getInstance().setSongScene(TYPE_LISTENING_TO_MUSIC)
          NEOrderSong.getInstance().configRoomSetting(info.liveModel!.roomUuid ?? "", liveRecordId: UInt64(info.liveModel!.liveRecordId))

          NELiveStreamKit.getInstance().joinRoom(joinParam) { [weak self] code, msg, _ in
            NELiveStreamToast.hideLoading()

            DispatchQueue.main.async {
              if code == 0 {
                NELiveStreamUILog.infoLog(anchorControllerTag, desc: "开始直播成功")
                // 在这里添加首次观众列表更新和启动定时器
                self?.updateAudienceList()
                self?.startAudienceListTimer()

                self?.switchToLiveMode()

                NELiveStreamKit.getInstance().setLocalVideoCanvas(view: self?.localRender)
              } else {
                NELiveStreamUILog.errorLog(anchorControllerTag, desc: "开始直播失败")
              }
            }
          }
        } else {
          NELiveStreamToast.hideLoading()
          NELiveStreamToast.show("创建直播失败")
          NELiveStreamUILog.errorLog(anchorControllerTag, desc: "创建直播失败")
        }
      }
    }
  }

  @objc private func handleBackgroundTap() {
    view.endEditing(true)
  }

  // 添加关闭按钮点击事件
  @objc private func closeButtonClick() {
    // 显示确认对话框
    let alert = UIAlertController(
      title: NSLocalizedString("结束直播", comment: ""),
      message: NSLocalizedString("是否结束直播？", comment: ""),
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
      self?.endLiveStream()
    })

    present(alert, animated: true)
  }

  // 结束直播的方法
  private func endLiveStream() {
    NELiveStreamKit.getInstance().endRoom { code, msg, obj in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.infoLog(anchorControllerTag, desc: "结束直播成功")
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag, desc: "结束直播失败")
        }
      }
    }

    // 无脑退出去 清除保存的房间信息
    liveInfo = nil
    navigationController?.popViewController(animated: true)
  }

  func showChooseSingViewController() {
    guard checkNetwork() else {
      return
    }

    let screenSize = UIScreen.main.bounds.size
    let height = screenSize.height * 2 / 3
    let size = CGSize(width: screenSize.width, height: height)

    let controller = UIViewController()
    controller.preferredContentSize = size

//        pickSongView = NEVoiceRoomPickSongView(frame: CGRect(origin: .zero, size: size), detail: detail)
    pickSongView.setPlayingStatus(true) /* = (playingStatus == .playing) */

//        let effectVolume = NEVoiceRoomKit.shared.getEffectVolume()
    pickSongView.setVolume(100)
    pickSongView.delegate = self
    controller.view = pickSongView

//        let nav = NEUIActionSheetNavigationController(rootViewController: controller)
//        controller.navigationController?.navigationBar.isHidden = true
//        nav.dismissOnTouchOutside = true
//
//        present(nav, animated: true)

//        let nav = NELiveStreamActionSheetNavigationController(rootViewController: controller)
//        nav.dismissOnTouchOutside = true
//        nav.modalPresentationStyle = .overFullScreen // 修改这里
//        controller.navigationController?.navigationBar.isHidden = true

    let nav = NEUIActionSheetNavigationController(rootViewController: controller)
    nav.dismissOnTouchOutside = true
//        nav.modalPresentationStyle = .custom

    present(nav, animated: true)
  }

  // MARK: - Pause Handling

  private func pauseLiveStream() {
    // 确保有直播记录ID
    guard let liveRecordId = liveInfo?.liveModel?.liveRecordId else {
      NELiveStreamUILog.errorLog(
        anchorControllerTag,
        desc: "Failed to pause Live Stream. LiveRecordId don't exist."
      )
      return
    }

    NELiveStreamKit.getInstance().enableLocalAudio(enable: false)
    NELiveStreamKit.getInstance().enableLocalVideo(enable: false)

    NELiveStreamKit.getInstance().pauseLive(Int64(liveRecordId), notifyMessage: "") { code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(
            anchorControllerTag,
            desc: "Successfully pauseLive"
          )
        } else {
          NELiveStreamUILog.errorLog(
            anchorControllerTag,
            desc: "Failed to pause Live Stream. Code: \(code). Msg: \(msg ?? "")"
          )
        }
      }
    }

    FUDemoManager.share().hideTipsView()

    // 显示暂停视图
    UIView.animate(withDuration: 0.3) {
      self.pauseView.isHidden = false
    }
  }

  private func resumeLiveStream() {
    // 确保有直播记录ID
    guard let liveRecordId = liveInfo?.liveModel?.liveRecordId else {
      NELiveStreamUILog.errorLog(
        anchorControllerTag,
        desc: "Failed to resume Live Stream. LiveRecordId don't exist."
      )
      return
    }

    // 实现继续直播的逻辑
    NELiveStreamKit.getInstance().enableLocalAudio(enable: true)
    NELiveStreamKit.getInstance().enableLocalVideo(enable: true)
    NELiveStreamKit.getInstance().resumeLive(Int64(liveRecordId), notifyMessage: "") { code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(
            anchorControllerTag,
            desc: "Successfully resume Live Stream"
          )

        } else {
          NELiveStreamUILog.errorLog(
            anchorControllerTag,
            desc: "Failed to get resume Live Stream. Code: \(code). Msg: \(msg ?? "")"
          )
        }
      }
    }

    // 隐藏暂停视图
    UIView.animate(withDuration: 0.3) {
      self.pauseView.isHidden = true
    }
  }

  // 添加关闭按钮点击事件
  @objc private func showOngoingLiveAlertView() {
    // 显示确认对话框
    let alert = UIAlertController(
      title: NSLocalizedString("直播异常中断，恢复直播？", comment: ""),
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
      self?.resumeOngoingLive()
    })

    present(alert, animated: true)
  }

  // 恢复异常中断的直播
  @objc private func resumeOngoingLive() {
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "尝试恢复异常直播")

    guard let info = liveInfo else {
      NELiveStreamUILog.infoLog(anchorControllerTag, desc: "恢复异常直播失败, no live info")
      return
    }

    let joinParam = NEJoinLiveStreamRoomParams()
    joinParam.nick = NELiveStreamUIManager.shared.nickname
    joinParam.roomInfo = liveInfo

    anchorInfoView.configure(avatar: info.anchor?.icon, nickname: info.liveModel?.liveTopic, roomId: info.liveModel?.roomUuid)

//    // 版权设置为听歌场景
//    NEOrderSong.getInstance().setSongScene(TYPE_LISTENING_TO_MUSIC)
    NEOrderSong.getInstance().configRoomSetting(info.liveModel!.roomUuid ?? "", liveRecordId: UInt64(info.liveModel!.liveRecordId))

    NELiveStreamKit.getInstance().joinRoom(joinParam) { [weak self] code, msg, _ in
      NELiveStreamToast.hideLoading()

      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.infoLog(anchorControllerTag, desc: "恢复异常直播成功")
          // 在这里添加首次观众列表更新和启动定时器
          self?.updateAudienceList()
          self?.startAudienceListTimer()

          self?.switchToLiveMode()
          NELiveStreamKit.getInstance().setLocalVideoCanvas(view: self?.localRender)
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag, desc: "恢复异常直播失败")
        }
      }
    }
  }
}

// MARK: - NELiveStreamFooterDelegate

extension NELiveAnchorViewController: NELiveStreamFooterDelegate {
  func footerDidReceiveMicMuteAction(_ mute: Bool) {
    // 处理麦克风静音
  }

  func footerDidReceiveGiftClick() {
    // 处理礼物点击
    // 显示礼物面板等
  }

  func footerDidReceiveMusicClickAction() {
    // 处理音乐点击
    // 显示音乐面板等
    showChooseSingViewController()
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
    moreVC.updateRole(isAnchor: true)
    moreVC.delegate = self
    let nav = NEUIActionSheetNavigationController(rootViewController: moreVC)
    nav.dismissOnTouchOutside = true
    nav.modalPresentationStyle = .custom
    present(nav, animated: true)
  }

  /// 美颜点击事件
  func footerDidReceiveBeautyClickAction() {
    FUDemoManager.share().show()
  }

  func footerDidLinkMicClick() {
    let nav = NEUIActionSheetNavigationController(rootViewController: micVc)
    nav.dismissOnTouchOutside = true
    nav.modalPresentationStyle = .custom
    present(nav, animated: true)
  }

  func footerDidReceivePKClickAction() {
    // 处理PK点击
    let nav = NEUIActionSheetNavigationController(rootViewController: pkInviteListVC)
    nav.dismissOnTouchOutside = true
    nav.modalPresentationStyle = .custom
    present(nav, animated: true)
  }
}

extension NELiveAnchorViewController {
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
          guard weakSelf.isLiving else {
            return
          }
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
        weakSelf.keyboardToolbar.frame = CGRect(x: 0, y: UIScreen.main.bounds.size.height + 50, width: weakSelf.view.bounds.width, height: 50)
      }
    }
  }

  override public func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    super.touchesBegan(touches, with: event)
    keyboardToolbar.resignFirstResponder()
    view.endEditing(true)
    FUDemoManager.share().hide()

    if !isLiving {
      bottomPanel.isHidden = false
    }
  }
}

// MARK: - Audience List Methods

extension NELiveAnchorViewController {
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
    guard let liveRecordId = liveInfo?.liveModel?.liveRecordId else {
      NELiveStreamUILog.errorLog(
        anchorControllerTag,
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
            anchorControllerTag,
            desc: "Successfully get audience list. Count: \(audienceList.total)"
          )
        } else {
          NELiveStreamUILog.errorLog(
            anchorControllerTag,
            desc: "Failed to get audience list. Code: \(code). Msg: \(msg ?? "")"
          )
        }
      }
    }
  }
}

// MARK: - NELiveStreamMoreFunctionDelegate

extension NELiveAnchorViewController: NELiveStreamMoreFunctionDelegate {
  func didSwitchCamera() {
    // 实现翻转摄像头
    NELiveStreamKit.getInstance().switchCamera()
  }

  func didToggleLive(_ isLive: Bool) -> Bool {
    // 实现继续/暂停直播
    if isLive {
      guard status == .soloLive else {
        NELiveStreamToast.show("当前状态不支持暂停直播")
        return false
      }

      pauseLiveStream()
    } else {
      resumeLiveStream()
    }

    return true
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

extension NELiveAnchorViewController: NERoomVideoFrameDelegate {
  func videoFrameCaptured(_ bufferRef: CVPixelBuffer, rotation: NERoomVideoRotationType) {
    FUManager.share().renderItems(to: bufferRef)
  }
}

extension NELiveAnchorViewController: NELiveStreamBottomPanelDelegate {
  func clickBeautyBtn() {
    NELiveStreamUtils.getMeidaPermissions(mediaType: .video) { [weak self] granted in
      if granted {
        FUDemoManager.share().show()
        self?.bottomPanel.isHidden = true
      } else {
        NELiveStreamToast.show(NELiveStreamBundle.localized("Camera_Permission_Disabled"))
      }
    }
  }

  func clickcameraBtn() {
    // 实现预览翻转摄像头
    previewRoomContext?.previewController.switchCamera()
  }
}

extension NELiveAnchorViewController: NELiveStreamPickSongViewProtocol {
  func pauseSong() {}

  func resumeSong() {}

  func nextSong(_ orderSongModel: NEOrderSongResponseOrderSongModel?) {}

  func volumeChanged(_ volume: Float) {}
}

// MARK: - NELiveStreamKeyboardToolbarDelegate

extension NELiveAnchorViewController: NELiveStreamKeyboardToolbarDelegate {
  func didToolBarSendText(_ text: String) {
    // 1. 检查文本是否为空
    let set = CharacterSet.whitespacesAndNewlines
    var text = text.trimmingCharacters(in: set)
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
          message.icon = NELiveStreamUI.ne_livestream_imageName(NELiveStreamBundle.localized("Owner_Icon"))
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

extension NELiveAnchorViewController: NELiveStreamListener {
  func onRoomEnded(_ reason: NELiveStreamEndReason) {
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "onRoomEnded reason:\(reason.rawValue)")
    DispatchQueue.main.async {
      guard reason != .leaveBySelf else {
        return
      }

      self.dismiss(animated: false)
      NELiveStreamToast.show("房间关闭")
      self.navigationController?.popViewController(animated: true)
    }
  }

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
    let isOwner = message.fromUserUuid == liveInfo?.anchor?.userUuid
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
}

extension NELiveAnchorViewController: NELiveStreamPauseViewDelegate {
  func onResumeLiveButtonClicked() {
    resumeLiveStream()
  }
}
