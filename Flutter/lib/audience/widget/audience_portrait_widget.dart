// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:livekit_sample/base/lifecycle_base_state.dart';
import 'package:livekit_sample/utils/textutil.dart';
import 'package:livekit_sample/values/asset_name.dart';

class AudiencePortraitWidget extends StatefulWidget {
  final List<String> avatarList;

  const AudiencePortraitWidget({Key? key, required this.avatarList})
      : super(key: key);

  @override
  State<AudiencePortraitWidget> createState() {
    return _AudiencePortraitWidgetState();
  }
}

class _AudiencePortraitWidgetState
    extends LifecycleBaseState<AudiencePortraitWidget> {
  @override
  Widget build(BuildContext context) {
    return Container(
        alignment: Alignment.centerRight,
        width: 140,
        child: ListView.builder(
            padding: EdgeInsets.zero,
            primary: false,
            itemCount:
                widget.avatarList.length > 5 ? 5 : widget.avatarList.length,
            scrollDirection: Axis.horizontal,
            shrinkWrap: true,
            itemBuilder: (context, index) {
              return buildHeaderWidget(widget.avatarList[index]);
            }));
  }

  buildHeaderWidget(String avatar) {
    return Container(
      alignment: Alignment.center,
      width: 30,
      height: 28,
      child: ClipOval(
        child: TextUtils.isEmpty(avatar)
            ? Image.asset(
                AssetName.audienceDefaultAvatar,
                width: 28,
                height: 28,
                fit: BoxFit.cover,
              )
            : Image.network(
                avatar,
                width: 28,
                height: 28,
                fit: BoxFit.cover,
              ),
      ),
    );
  }
}
