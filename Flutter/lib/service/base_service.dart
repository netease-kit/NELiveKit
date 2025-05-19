// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import 'package:livekit_sample/service/auth/auth_manager.dart';
import 'package:livekit_sample/service/client/http_code.dart';
import 'package:livekit_sample/service/proto/base_proto.dart';
import 'package:livekit_sample/service/response/result.dart';

/// base service
class BaseService {
  /// execute method
  Future<Result<T>> execute<T>(BaseProto proto) {
    return proto.execute().then((result) {
      if (proto.checkLoginState() &&
          (result.code == HttpCode.verifyError ||
              result.code == HttpCode.tokenError)) {
        AuthManager()
            .tokenIllegal(HttpCode.getMsg(result.msg, 'Token invalid!'));
      }
      return result as Result<T>;
    });
  }
}
