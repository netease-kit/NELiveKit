// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:livekit_sample/values/colors.dart';
import 'package:livekit_sample/values/strings.dart';

class InputDialog {
  static Future<String?> show(BuildContext context) async {
    return Navigator.of(context).push(InputOverlay());
  }
}

class InputOverlay extends ModalRoute<String> {
  @override
  Duration get transitionDuration => const Duration(milliseconds: 100);

  @override
  bool get opaque => false;

  @override
  bool get barrierDismissible => true;

  @override
  Color get barrierColor => const Color(0x01000000);

  @override
  bool get maintainState => true;

  @override
  Widget buildPage(
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
  ) {
    return const InputWidget();
  }

  @override
  Widget buildTransitions(BuildContext context, Animation<double> animation,
      Animation<double> secondaryAnimation, Widget child) {
    return FadeTransition(
      opacity: CurvedAnimation(
        parent: animation,
        curve: Curves.easeOut,
      ),
      child: child,
    );
  }

  @override
  // TODO: implement barrierLabel
  String? get barrierLabel => null;
}

class InputWidget extends StatefulWidget {
  const InputWidget({Key? key}) : super(key: key);

  // InputWidget({Key key}) : super(key: key);

  @override
  _InputWidgetState createState() => _InputWidgetState();
}

class _InputWidgetState extends State<InputWidget> {
  @override
  Widget build(BuildContext context) {
    TextEditingController editingController = TextEditingController();

    return Scaffold(
      backgroundColor: Colors.transparent,
//      resizeToAvoidBottomInset: false,
      body: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Expanded(
            child: GestureDetector(
                onTapDown: (_) => Navigator.of(context).pop(),
                child: Container(
                  color: Colors.transparent,
                )),
          ),
          SafeArea(
            child: Container(
              color: Colors.white,
              child: Row(
                children: <Widget>[
                  Container(
                    width: 20,
                  ),
                  Expanded(
                    child: Container(
                      margin: const EdgeInsets.only(top: 10, bottom: 10),
                      decoration: const BoxDecoration(
                          color: Color(0xfff6f8fb),
                          borderRadius: BorderRadius.all(Radius.circular(20))),
                      alignment: Alignment.center,
                      child: TextField(
                        style: const TextStyle(color: AppColors.black),
                        onEditingComplete: () {
                          var text = editingController.text.trim();
                          Navigator.pop(context, text);
                        },
                        autofocus: true,
                        maxLengthEnforcement: MaxLengthEnforcement.enforced,
                        inputFormatters: <TextInputFormatter>[
                          LengthLimitingTextInputFormatter(200)
                        ],
                        controller: editingController,
                        decoration: const InputDecoration(
                            fillColor: AppColors.black,
                            isDense: true,
                            contentPadding: EdgeInsets.only(
                                left: 10, right: 10, top: 5, bottom: 5),
                            border: InputBorder.none,
                            hintStyle: TextStyle(color: Color(0xffcccccc)),
                            hintText: Strings.saySomething),
                      ),
                    ),
                  ),
                  GestureDetector(
                    onTap: (() {
                      var text = editingController.text.trim();
                      Navigator.pop(context, text);
                    }),
                    child: Container(
                      decoration: const BoxDecoration(
                          color: AppColors.accentElement,
                          borderRadius: BorderRadius.all(Radius.circular(5))),
                      padding: const EdgeInsets.only(
                        left: 10,
                        right: 10,
                      ),
                      alignment: Alignment.center,
                      height: 33,
                      margin: const EdgeInsets.symmetric(horizontal: 5),
                      child: const Text(
                        Strings.send,
                        style: TextStyle(color: AppColors.white),
                      ),
                    ),
                  )
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
