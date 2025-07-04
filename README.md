<div align="vertical-center">
  <a href="https://deepwiki.com/netease-kit/NELiveKit">
    <img src="https://devin.ai/assets/deepwiki-badge.png" alt="Ask the Deepwiki" height="20"/>
  </a>
  <p>单击跳转查看 <a href="https://deepwiki.com/netease-kit/NELiveKit">DeepWiki</a> 源码解读。</p>
</div>

-------------------------------

# 简介

网易云信直播房场景方案为您提供 iOS 和 Android 端的 Demo App 与示例项目源码。您可以安装对应 App，快速体验网易云信直播房。

# 功能列表
标准直播房的主要功能如下表所示。

|  主要功能   | 功能描述                                                                                                                                             |
|  ----  |--------------------------------------------------------------------------------------------------------------------------------------------------|
| 音频通话  | RTC 方案：超低延时下，观众可以实时接收房主或连麦者的音频流，保证直播房互动顺畅。CDN 方案：观众可以通过 <br> CDN 拉流接收主播或连麦者的音频流。主播和连麦者之间可做到实时音视频通话。                                              |
| 连麦互动  | 房主邀请或观众申请上麦。观众上麦后成为连麦主播。房间内所有用户都可以实时观看房主和连麦主播互动。                                                                                                 |
| 主播PK  | 主播邀请另外一个主播进行PK互动。                                                                                          |
| 麦位管理 | 麦位即直播房的座位。房主或管理员也可以对麦位进行管理，包括对麦位上的连麦主播进行下麦等操作。<br> 上麦：邀请观众上麦。观众上麦后成为连麦主播，可以和房主实时互动。<br> 下麦：将连麦主播恢复为普通观众。<br> 抱麦：主动把用户抱到指定麦位上。<br> 踢人：踢掉对应麦位上的用户。 |
| 消息互动  | 直播房内的所有角色都可以发送和接收文字消息，实时文字互动。                                                                                                                    |
| 进出房间通知 | 聊天室内的所有角色都可以实时了解当前直播房的人员增减信息，即谁进入了聊天室，或谁离开了聊天室。                                                                                                  |
| 房间信息查询  | 聊天室内的所有角色都可以实时查看聊天室在线人数和互动成员列表。                                                                                                                  |



# 效果演示

> 您可以扫描下方二维码，下载并体验demo。

| iOS    | Android  |
|  ----  | ----  |
| ![](pic/download_ios.png)  |  ![](pic/download_android.png) | 

标准直播房 的界面效果如下图所示：

![](pic/effect_picture_1.png)


# 注意
- 该源码仅供开发者接入时参考，网易云信不负责源码的后续维护。若开发者计划将该源码用于生产环境，请确保发布前进行充分测试，避免发生潜在问题造成损失。
- 该源码中提供的业务后台地址仅用于跑通示例源码，如果您需要上线正式产品，请自行编写、搭建自己的业务后台。


# 联系我们

- 如果想要了解该场景的更多信息，请参见[直播房场景方案文档](https://doc.yunxin.163.com/pk/concept?platform=client)
- 完整的API文档请参见[API参考](https://doc.yunxin.163.com/group-voice-room/api-refer)
- 如果您遇到的问题，可以先查阅[知识库](https://faq.yunxin.163.com/kb/main/#/)
- 如果需要售后技术支持，请[提交工单](https://app.yunxin.163.com/index#/issue/submit)


# 更多场景方案

网易云信针对1V1娱乐社交、语聊房、直播连麦房、在线教育等业务场景，推出了一体式、可扩展、功能业务融合的全链路解决方案，帮助客户快速接入、业务及时上线，提高营收增长。

- [1对1 娱乐社交](https://github.com/netease-kit/1V1)
- [PK连麦](https://github.com/netease-kit/OnlinePK)
- [在线教育](https://github.com/netease-kit/WisdomEducation)
- [多人视频通话](https://github.com/netease-kit/NEGroupCall)
- [一起听](https://github.com/netease-kit/NEListenTogether)
- [在线K歌](https://github.com/netease-kit/NEKaraoke)
- [云信娱乐社交服务端 Nemo](https://github.com/netease-kit/nemo)
