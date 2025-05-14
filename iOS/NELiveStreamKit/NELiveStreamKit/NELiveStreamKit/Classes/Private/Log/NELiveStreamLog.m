// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamLog.h"
#import <NECoreKit/XKitLog.h>

static XKitLog *_log = nil;

@implementation NELiveStreamLog
+ (void)setUp:(NSString *)appkey {
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    XKitLogOptions *options = [[XKitLogOptions alloc] init];
    options.level = XKitLogLevelInfo;
    options.moduleName = @"LiveStream";
    options.sensitives = @[ appkey ];
    _log = [XKitLog setUp:options];
  });
}

+ (void)apiLog:(NSString *)className desc:(NSString *)desc {
  [_log apiLog:className
          desc:[NSString stringWithFormat:@"🚰 %@", [desc stringByRemovingPercentEncoding]]];
}
+ (void)infoLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className
           desc:[NSString stringWithFormat:@"⚠️ %@", [desc stringByRemovingPercentEncoding]]];
}
/// warn类型 log
+ (void)warnLog:(NSString *)className desc:(NSString *)desc {
  [_log warnLog:className
           desc:[NSString stringWithFormat:@"❗️ %@", [desc stringByRemovingPercentEncoding]]];
}
+ (void)successLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className
           desc:[NSString stringWithFormat:@"✅ %@", [desc stringByRemovingPercentEncoding]]];
}
/// error类型 log
+ (void)errorLog:(NSString *)className desc:(NSString *)desc {
  [_log errorLog:className
            desc:[NSString stringWithFormat:@"❌ %@", [desc stringByRemovingPercentEncoding]]];
}
+ (void)messageLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className
           desc:[NSString stringWithFormat:@"✉️ %@", [desc stringByRemovingPercentEncoding]]];
}
+ (void)networkLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className
           desc:[NSString stringWithFormat:@"📶 %@", [desc stringByRemovingPercentEncoding]]];
}

@end
