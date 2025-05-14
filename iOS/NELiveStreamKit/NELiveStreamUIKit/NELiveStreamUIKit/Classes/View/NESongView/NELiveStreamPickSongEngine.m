// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamPickSongEngine.h"
#import <AVFoundation/AVFoundation.h>
#import <NELiveStreamKit/NELiveStreamKit-Swift.h>
#import <NELiveStreamUIKit/NELiveStreamUIKit-Swift.h>
#import <NEOrderSong/NEOrderSong-Swift.h>
#import "NELiveStreamLocalized.h"
#import "NELiveStreamPickSongColorDefine.h"
#import "NELiveStreamSongItem.h"

static int NEPageSize = 20;

@interface NELiveStreamPickSongEngine () <NEOrderSongCopyrightedMediaEventHandler,
                                          NELiveStreamListener,
                                          NEOrderSongListener,
                                          NEOrderSongCopyrightedMediaListener>

@property(nonatomic, strong) NSPointerArray *observeArray;

@property(nonatomic, assign) uint64_t retrtLater;

@end

@implementation NELiveStreamPickSongEngine

+ (instancetype)sharedInstance {
  static dispatch_once_t onceToken;
  static NELiveStreamPickSongEngine *pickSongEngine = nil;
  dispatch_once(&onceToken, ^{
    pickSongEngine = [[NELiveStreamPickSongEngine alloc] init];
    [pickSongEngine initData];
    [[NELiveStreamKit getInstance] addLiveStreamListener:pickSongEngine];
    [[NEOrderSong getInstance] addOrderSongListener:pickSongEngine];
    [[NEOrderSong getInstance] setCopyrightedMediaEventHandler:pickSongEngine];
  });
  return pickSongEngine;
}

- (void)initData {
  self.pickSongArray = [NSMutableArray array];
  self.pickSongDownloadingArray = [NSMutableArray array];
  self.pickedSongArray = [NSMutableArray array];
  self.pageNum = 0;
  self.observeArray = [NSPointerArray weakObjectsPointerArray];
  self.currentOrderSongArray = [NSMutableArray array];
}

- (void)addObserve:(id<NESongPointProtocol>)observe {
  bool hasAdded = NO;
  for (id<NESongPointProtocol> item in self.observeArray) {
    if (item == observe) {
      hasAdded = YES;
      break;
    }
  }
  if (!hasAdded) {
    [self.observeArray addPointer:(__bridge void *)(observe)];
  }
}

- (void)removeObserve:(id<NESongPointProtocol>)observe {
  bool hasAdded = NO;
  int observeIndex = 0;
  for (int index = 0; index < self.observeArray.count; index++) {
    id<NESongPointProtocol> item = [self.observeArray pointerAtIndex:index];
    if (item == observe) {
      hasAdded = YES;
      observeIndex = index;
      break;
    }
  }
  if (hasAdded) {
    [self.observeArray removePointerAtIndex:observeIndex];
  }
}

- (void)clearData {
  [self.pickSongArray removeAllObjects];
  [self.pickedSongArray removeAllObjects];
  [self.pickSongDownloadingArray removeAllObjects];
}
// 获取已点数据
- (void)getKaraokeSongOrderedList:(SongListBlock)callback {
  [[NEOrderSong getInstance]
      getOrderedSongsWithCallback:^(NSInteger code, NSString *_Nullable msg,
                                    NSArray<NEOrderSongResponse *> *_Nullable orderSongs) {
        if (code != 0) {
          callback([NSError errorWithDomain:@"getVoiceRoomSongOrderedList" code:code userInfo:nil]);
        } else {
          self.pickedSongArray = [orderSongs mutableCopy];
          callback(nil);
        }
      }];
}

- (void)getKaraokeSongList:(SongListBlock)callback {
  [[NEOrderSong getInstance]
      getSongList:nil
          channel:nil
          pageNum:@(self.pageNum)
         pageSize:@(NEPageSize)
         callback:^(NSArray<NECopyrightedSong *> *_Nonnull songList, NSError *_Nonnull error) {
           if (error) {
             callback(error);
           } else {
             @synchronized(self) {
               NSMutableArray *tempItems = [NSMutableArray array];
               NSMutableArray *tempLoading = [NSMutableArray array];
               NSMutableArray *tempCurrentOrderingArray = [NSMutableArray array];
               for (NELiveStreamSongItem *item in self.currentOrderSongArray) {
                 [tempCurrentOrderingArray addObject:item.songId];
               }
               for (NECopyrightedSong *songItem in songList) {
                 NELiveStreamSongItem *item = [self changeCopyrightedToKaraokeSongItem:songItem];
                 // 语聊房不需要伴奏判断
                 //                 if (item.hasAccompany) {
                 BOOL isDownloading = NO;
                 if ([tempCurrentOrderingArray containsObject:item.songId]) {
                   isDownloading = YES;
                 }

                 if (isDownloading) {
                   [tempLoading addObject:@"1"];
                 } else {
                   [tempLoading addObject:@"0"];
                 }
                 if (item) {
                   [tempItems addObject:item];
                 }
                 //                 }
               }
               dispatch_async(dispatch_get_main_queue(), ^{
                 [self.pickSongArray addObjectsFromArray:tempItems];
                 [self.pickSongDownloadingArray addObjectsFromArray:tempLoading];
                 self.noMore = songList.count <= 0;
                 callback(nil);
               });
             }
           }
         }];
}

- (void)updateSongArray {
  @synchronized(self) {
    [self.pickSongArray removeAllObjects];
    [self.pickSongDownloadingArray removeAllObjects];
  }
}

- (void)resetPageNumber {
  self.pageNum = 0;
  self.searchPageNum = 0;
}
- (void)updatePageNumber:(BOOL)isSearching {
  if (isSearching) {
    self.searchPageNum += 1;
  } else {
    self.pageNum += 1;
  }
}

// 上下滑动刷新搜索数据
- (void)getKaraokeSearchSongList:(NSString *)searchString callback:(SongListBlock)callback {
  [[NEOrderSong getInstance]
      searchSong:searchString
         channel:nil
         pageNum:@(self.searchPageNum)
        pageSize:@(NEPageSize)
        callback:^(NSArray<NECopyrightedSong *> *_Nonnull songList, NSError *_Nonnull error) {
          if (error) {
            callback(error);
          } else {
            @synchronized(self) {
              NSMutableArray *tempItems = [NSMutableArray array];
              NSMutableArray *tempLoading = [NSMutableArray array];
              NSMutableArray *tempCurrentOrderingArray = [NSMutableArray array];
              for (NELiveStreamSongItem *item in self.currentOrderSongArray) {
                [tempCurrentOrderingArray addObject:item.songId];
              }
              for (NECopyrightedSong *songItem in songList) {
                NELiveStreamSongItem *item = [self changeCopyrightedToKaraokeSongItem:songItem];
                /// 语聊房不需要伴奏判断.
                //                if (item.hasAccompany) {
                if (item) {
                  [tempItems addObject:item];
                } else {
                  continue;
                }
                BOOL isDownloading = NO;
                if ([tempCurrentOrderingArray containsObject:item.songId]) {
                  isDownloading = YES;
                }

                if (isDownloading) {
                  [tempLoading addObject:@"1"];
                } else {
                  [tempLoading addObject:@"0"];
                }
                //                }
              }
              dispatch_async(dispatch_get_main_queue(), ^{
                [self.pickSongArray addObjectsFromArray:tempItems];
                [self.pickSongDownloadingArray addObjectsFromArray:tempLoading];
                self.noMore = songList.count <= 0;
                callback(nil);
              });
            }
          }
        }];
}

- (void)onSongListChanged {
  for (id<NESongPointProtocol> obj in self.observeArray) {
    if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
        [obj respondsToSelector:@selector(onOrderSongRefresh)]) {
      [obj onOrderSongRefresh];
    }
  }
}

/**
 * 预加载 Song 数据
 *
 * @param songId 歌曲id
 * @param channel 渠道
 */
- (void)preloadSong:(NSString *)songId channel:(SongChannel)channel {
  [[NEOrderSong getInstance] preloadSong:songId channel:channel observe:self];
}
#pragma mark <NESongPreloadProtocol>

- (void)onPreloadStart:(NSString *)songId channel:(SongChannel)channel {
  [NELiveStreamUILog successLog:liveStreamUILog
                           desc:[NSString stringWithFormat:@"%@开始加载", songId]];
}

- (void)onPreloadProgress:(NSString *)songId channel:(SongChannel)channel progress:(float)progress {
  NELiveStreamSongItem *songItem;
  @synchronized(self) {
    for (NELiveStreamSongItem *item in self.pickSongArray) {
      if ([item.songId isEqualToString:songId]) {
        songItem = item;
        songItem.downloadProcess = progress;
        break;
      }
    }
  }

  //  if (progress > 0.5 && progress < 0.6) {
  //    NSString *progressLogInfo =
  //        [NSString stringWithFormat:@"下载中,songId:%@,\n progress:%.2f, \n songItem:%@, \n  "
  //                                   @"currentOrderSongArray:%@ ,\n pickSongArray:%@",
  //                                   songId, progress, songItem, self.currentOrderSongArray,
  //                                   self.pickSongArray];
  //    [NELiveStreamUILog successLog:liveStreamUILog desc:progressLogInfo];
  //  }

  if (songItem) {
    unsigned long index = [self.pickSongArray indexOfObject:songItem];
    @synchronized(self) {
      [[NELiveStreamPickSongEngine sharedInstance].pickSongDownloadingArray
          replaceObjectAtIndex:index
                    withObject:@"0"];
    }

    for (id<NESongPointProtocol> obj in self.observeArray) {
      if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
          [obj respondsToSelector:@selector(onSourceReloadIndex:process:)]) {
        [obj onSourceReloadIndex:[NSIndexPath indexPathForRow:index inSection:0] process:progress];
      }
    }
  }
}

- (void)onPreloadComplete:(NSString *)songId
                  channel:(SongChannel)channel
                    error:(NSError *_Nullable)preloadError {
  NSString *infoString =
      [NSString stringWithFormat:@"songid = %@;error = %@", songId,
                                 preloadError.description ? preloadError.description : @"scuuess"];
  [NELiveStreamUILog infoLog:liveStreamUILog desc:infoString];
  // 获取Item 刷新UI
  @synchronized(self) {
    NELiveStreamSongItem *songItem;
    for (NELiveStreamSongItem *song in self.pickSongArray) {
      if ([songId isEqualToString:song.songId]) {
        songItem = song;
        break;
      }
    }

    if (songItem) {
      long index = [self.pickSongArray indexOfObject:songItem];
      [[NELiveStreamPickSongEngine sharedInstance].pickSongDownloadingArray
          replaceObjectAtIndex:index
                    withObject:@"0"];
      // 此处添加数据回调
      // 回调抛出
      for (id<NESongPointProtocol> obj in self.observeArray) {
        if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
            [obj respondsToSelector:@selector(onSourceReloadIndex:isSonsList:)]) {
          [obj onSourceReloadIndex:[NSIndexPath indexPathForRow:index inSection:0] isSonsList:YES];
        }
      }
    }

    NSMutableArray *songItemArray = [NSMutableArray array];
    NELiveStreamSongItem *currentSongitem;
    for (NELiveStreamSongItem *song in self.currentOrderSongArray) {
      if ([songId isEqualToString:song.songId]) {
        currentSongitem = song;
        [songItemArray addObject:song];
      }
    }
    if (preloadError) {
      if (preloadError.code == ERR_CANCEL) {
        [NELiveStreamUILog successLog:liveStreamUILog desc:NELocalizedString(@"用户取消下载")];
        for (id<NESongPointProtocol> obj in self.observeArray) {
          if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
              [obj respondsToSelector:@selector(onOrderSong:error:)]) {
            [obj onOrderSong:nil error:NELocalizedString(@"用户取消下载")];
          }
        }
      } else {
        [NELiveStreamUILog successLog:liveStreamUILog desc:NELocalizedString(@"文件加载失败")];
        for (id<NESongPointProtocol> obj in self.observeArray) {
          if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
              [obj respondsToSelector:@selector(onOrderSong:error:)]) {
            [obj onOrderSong:nil error:NELocalizedString(@"文件加载失败")];
          }
        }
      }
      if (currentSongitem) {
        [NELiveStreamUILog
            successLog:liveStreamUILog
                  desc:[NSString
                           stringWithFormat:
                               @"加载中数据移除, songId:%@ ,itemArray:%@,当前下载中列表数据:%@",
                               songId, songItemArray, self.currentOrderSongArray]];
        [self.currentOrderSongArray removeObjectsInArray:songItemArray];
      }
      return;
    }
    [NELiveStreamUILog successLog:liveStreamUILog desc:NELocalizedString(@"文件加载完成")];

    if (!currentSongitem) {
      return;
    }
    NEOrderSongOrderSongParams *orderSong = [[NEOrderSongOrderSongParams alloc] init];
    orderSong.songId = songId;
    orderSong.songName = [NSString stringWithFormat:@"%@", currentSongitem.songName];
    orderSong.songCover = [NSString stringWithFormat:@"%@", currentSongitem.songCover];
    orderSong.songCover = [NSString stringWithFormat:@"%@", currentSongitem.songCover];

    if (currentSongitem.singers.count > 0) {
      NECopyrightedSinger *singer = currentSongitem.singers.firstObject;
      if (singer) {
        orderSong.singer = singer.singerName;
      }
    }

    orderSong.oc_channel = channel;

    // 获取歌曲长度
    NSString *songPath = [[NEOrderSong getInstance] getSongURI:songId
                                                       channel:(SongChannel)channel
                                                   songResType:TYPE_ACCOMP];
    NSData *data = [NSData dataWithContentsOfFile:songPath];
    if (!data.length) {
      songPath = [[NEOrderSong getInstance] getSongURI:songId
                                               channel:(SongChannel)channel
                                           songResType:TYPE_ORIGIN];
      data = [NSData dataWithContentsOfFile:songPath];
    }

    if (channel == MIGU) {
      if (songPath) {
        orderSong.oc_songTime =
            [self getAudioDurationWithAudioURL:[NSURL fileURLWithPath:songPath]] * 1000;
      }
    } else {
      AVAudioPlayer *player = [[AVAudioPlayer alloc] initWithData:data error:nil];
      orderSong.oc_songTime = player.duration * 1000;
    }

    [NELiveStreamUILog
        successLog:liveStreamUILog
              desc:[NSString stringWithFormat:
                                 @"加载中数据移除, songId:%@ ,itemArray:%@,当前下载中列表数据:%@",
                                 songId, songItemArray, self.currentOrderSongArray]];
    [self.currentOrderSongArray removeObjectsInArray:songItemArray];

    [[NEOrderSong getInstance]
        orderSong:orderSong
         callback:^(NSInteger code, NSString *_Nullable msg,
                    NEOrderSongResponse *_Nullable object) {
           if (code != 0) {
             NSString *message = nil;
             if (code == SONG_ERROR_SONG_POINTED) {
               message = NELocalizedString(@"歌曲已点");
             } else if (code == SONG_ERROR_SONG_POINTED_USER_LIMIT) {
               message = NELocalizedString(@"已达到单人点歌数上限");
             } else if (code == SONG_ERROR_SONG_POINTED_ROOM_LIMIT) {
               message = NELocalizedString(@"已达到房间点歌数上限");
             } else {
               message = NELocalizedString(@"点歌失败");
             }

             // 此处添加数据回调
             // 回调抛出
             for (id<NESongPointProtocol> obj in self.observeArray) {
               if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
                   [obj respondsToSelector:@selector(onOrderSong:error:)]) {
                 [obj onOrderSong:nil error:message];
               }
             }

           } else {
             // 此处添加数据回调
             // 回调抛出
             [NELiveStreamUILog successLog:liveStreamUILog desc:@"点歌成功"];
             for (id<NESongPointProtocol> obj in self.observeArray) {
               if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
                   [obj respondsToSelector:@selector(onOrderSong:error:)]) {
                 [obj onOrderSong:object error:nil];
               }
             }
           }
         }];
  }
}

// 上麦成功数据处理
- (void)applySuccessWithSong:(NELiveStreamSongItem *)songItem complete:(void (^)(void))complete {
  if (songItem) {
    NSNumber *index = nil;
    for (NELiveStreamSongItem *item in self.pickSongArray) {
      if ([item.songId isEqualToString:songItem.songId]) {
        index = [NSNumber numberWithLong:[self.pickSongArray indexOfObject:item]];
      }
    }

    if (index == nil) {
      return;
    }

    [[NELiveStreamPickSongEngine sharedInstance].pickSongDownloadingArray
        replaceObjectAtIndex:[index intValue]
                  withObject:@"1"];

    // 此处添加数据回调
    // 回调抛出
    for (id<NESongPointProtocol> obj in self.observeArray) {
      if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
          [obj respondsToSelector:@selector(onSourceReloadIndex:isSonsList:)]) {
        [obj onSourceReloadIndex:[NSIndexPath indexPathForRow:[index intValue] inSection:0]
                      isSonsList:YES];
      }
    }
    if (complete) {
      complete();
    }
  }
}

- (NELiveStreamSongItem *)changeCopyrightedToKaraokeSongItem:(NECopyrightedSong *)songItem {
  NELiveStreamSongItem *item = [[NELiveStreamSongItem alloc] init];
  item.songId = songItem.songId;
  item.songName = songItem.songName;
  item.songCover = songItem.songCover;
  item.singers = songItem.singers;
  item.albumName = songItem.albumName;
  item.albumCover = songItem.albumCover;
  item.originType = songItem.originType;
  item.channel = songItem.channel;
  item.hasAccompany = songItem.hasAccompany;
  item.hasOrigin = songItem.hasOrigin;
  return item;
}

- (void)voiceroom_onTokenExpired {
  [NELiveStreamUILog infoLog:liveStreamUILog desc:@"收到token过期回调"];
  for (id<NESongPointProtocol> obj in self.observeArray) {
    if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
        [obj respondsToSelector:@selector(onVoiceRoomSongTokenExpired)]) {
      [obj onVoiceRoomSongTokenExpired];
    }
  }
}

- (CGFloat)getAudioDurationWithAudioURL:(NSURL *)audioURL {
  NSDictionary *opts =
      [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES]
                                  forKey:AVURLAssetPreferPreciseDurationAndTimingKey];
  AVURLAsset *urlAsset = [AVURLAsset URLAssetWithURL:audioURL options:opts];
  CGFloat second = urlAsset.duration.value * 1.0 / urlAsset.duration.timescale;
  return second;
}

- (NEOrderSongResponse *)getNextSong {
  NSMutableArray *tempPickedSongArray = [[self pickedSongArray] mutableCopy];
  BOOL songMatched = NO;
  NEOrderSongResponse *nextSong;
  for (NEOrderSongResponse *orderSongModel in tempPickedSongArray) {
    if (songMatched) {
      nextSong = orderSongModel;
      break;
    }
    if ([orderSongModel.orderSong.songId
            isEqualToString:self.currrentSongModel.playMusicInfo.songId] &&
        orderSongModel.orderSong.oc_channel == self.currrentSongModel.playMusicInfo.oc_channel) {
      songMatched = YES;
    }
  }
  if (nextSong == nil) {
    nextSong = tempPickedSongArray.firstObject;
  }
  return nextSong;
}

@end
