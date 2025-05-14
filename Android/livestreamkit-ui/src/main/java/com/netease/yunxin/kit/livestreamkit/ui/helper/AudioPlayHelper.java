// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.helper;

import static com.netease.yunxin.kit.livestreamkit.ui.helper.AudioPlayHelper.AudioMixingPlayState.STATE_STOPPED;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.utils.CommonUtil;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamListener;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.ChatRoomAudioDialog;
import com.netease.yunxin.kit.roomkit.api.model.*;
import java.io.File;

public class AudioPlayHelper extends NELiveStreamListener {

  public static final String TAG = "AudioPlayHelper";

  /** 音效文件 */
  private String[] effectPaths;

  private int effectVolume = 100;

  /** 混音音量 */
  private int audioMixingVolume = 50;

  /** 当前混音 */
  private int audioMixingIndex = 0;

  /** 混音播放状态 */
  private int audioMixingState = STATE_STOPPED;

  /** 采集音量，默认100 */
  private int audioCaptureVolume = 100;

  private IPlayCallback callBack;

  private Context context;

  private static final String MUSIC_DIR = "music";
  private static final String MUSIC1 = "music1.mp3";
  private static final String MUSIC2 = "music2.mp3";
  private static final String MUSIC3 = "music3.mp3";
  private static final String EFFECT1 = "effect1.wav";
  private static final String EFFECT2 = "effect2.wav";

  public AudioPlayHelper(Context context) {
    this.context = context;
    NELiveStreamKit.getInstance().addLiveStreamListener(this);
  }

  private String extractMusicFile(String path, String name) {
    CommonUtil.copyAssetToFile(context, MUSIC_DIR + "/" + name, path, name);
    return new File(path, name).getAbsolutePath();
  }

  private String ensureMusicDirectory() {
    File dir = context.getExternalFilesDir(MUSIC_DIR);
    if (dir == null) {
      dir = context.getDir(MUSIC_DIR, 0);
    }
    if (dir != null) {
      dir.mkdirs();
      return dir.getAbsolutePath();
    }
    return "";
  }

  public void checkMusicFiles() {
    new Thread(
            () -> {
              String root = ensureMusicDirectory();
              String[] effectPaths = new String[2];
              effectPaths[0] = extractMusicFile(root, EFFECT1);
              effectPaths[1] = extractMusicFile(root, EFFECT2);
              setEffectPaths(effectPaths);
            })
        .start();
  }

  /**
   * 获取音乐文件信息
   *
   * @param mediaUri 文件路径
   */
  private ChatRoomAudioDialog.MusicItem getMusicInfo(String order, String mediaUri) {
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    mediaMetadataRetriever.setDataSource(mediaUri);
    String name = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    String author =
        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    return new ChatRoomAudioDialog.MusicItem(order, name, author);
  }

  public void setCallBack(IPlayCallback callBack) {
    this.callBack = callBack;
  }

  public void setEffectPaths(String[] effectPaths) {
    this.effectPaths = effectPaths;
  }

  public void setEffectVolume(int effectVolume) {
    this.effectVolume = effectVolume;
    for (int index = 0; index < effectPaths.length; index++) {
      int effectId = effectIndexToEffectId(index);
      NELiveStreamKit.getInstance().setEffectVolume(effectId, effectVolume);
    }
  }

  public int getEffectVolume() {
    return effectVolume;
  }

  public int getAudioMixingVolume() {
    return audioMixingVolume;
  }

  public int getCurrentState() {
    return audioMixingState;
  }

  public int getPlayingMixIndex() {
    return audioMixingIndex;
  }

  public void setAudioCaptureVolume(int volume) {
    audioCaptureVolume = volume;
    NELiveStreamKit.getInstance().adjustRecordingSignalVolume(volume);
  }

  public int getAudioCaptureVolume() {
    return audioCaptureVolume;
  }

  // 播放音效
  public void playEffect(int index) {
    if (effectPaths == null) {
      ALog.e(TAG, "effectPaths is null");
      return;
    }
    if (index < effectPaths.length && index >= 0) {
      String path = effectPaths[index];
      int effectId = effectIndexToEffectId(index);
      NERoomCreateAudioEffectOption option =
          new NERoomCreateAudioEffectOption(
              path,
              1,
              true,
              effectVolume,
              true,
              effectVolume,
              0,
              100,
              NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain);
      NELiveStreamKit.getInstance().stopEffect(effectId);
      NELiveStreamKit.getInstance().playEffect(effectId, option);
    }
  }

  public void stopAllEffect() {
    NELiveStreamKit.getInstance().stopAllEffect();
  }

  @Override
  public void onAudioMixingStateChanged(int reason) {
    if (reason == 0) {
      audioMixingState = STATE_STOPPED;
      callBack.onAudioMixingPlayFinish();
    } else {
      callBack.onAudioMixingPlayError();
    }
  }

  public void destroy() {
    stopAllEffect();
    NELiveStreamKit.getInstance().removeLiveStreamListener(this);
  }

  private int effectIndexToEffectId(int index) {
    return index + 1; // effect id starts from one
  }

  /** 伴音播放状态 */
  public interface AudioMixingPlayState {
    /** 停止，未播放 */
    int STATE_STOPPED = 0;

    /** 播放中 */
    int STATE_PLAYING = 1;

    /** 暂停 */
    int STATE_PAUSED = 2;
  }

  public interface IPlayCallback {
    /** 伴音播放错误 */
    void onAudioMixingPlayError();

    /**
     * 伴音播放状态
     *
     * @param state {@link AudioMixingPlayState}
     * @param index 伴音文件索引
     */
    void onAudioMixingPlayState(int state, int index);

    /** 伴音播放完成 */
    void onAudioMixingPlayFinish();
  }
}
