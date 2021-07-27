import 'package:flutter/services.dart';

class DocumentWriter {
  static const platform = const MethodChannel('id.kudzoza.document_writer/doc_writer');

  DocumentWriter() {
    platform.setMethodCallHandler((call) => _platformCallHandler(call));
  }

  void Function() _onPermissionAlreadyGranted = () {};
  void Function() _onPermissionGranted = () {};
  void Function() _onCopySuccess = () {};
  void Function() _onCopyFailure = () {};

  void init() {
    platform.invokeMethod("init");
  }

  Future<void> copy({required String from, required String to}) {
    return platform.invokeMethod("copy", <String, String>{
      "from": from,
      "to": to,
    });
  }

  Future<dynamic> _platformCallHandler(MethodCall call) async {
    switch (call.method) {
      case "on_permission_granted":
        _onPermissionGranted.call();
        break;
      case "on_permission_already_granted":
        _onPermissionAlreadyGranted.call();
        break;
      case "on_copy_success":
        _onCopySuccess.call();
        break;
      case "on_copy_failed":
        _onCopyFailure.call();
        break;
      case "undefined_method":
        print("Undefined Method");
        break;
    }
  }

  void setOnPermissionAlreadyGranted(void Function() action) {
    _onPermissionAlreadyGranted = action;
  }

  void setOnPermissionGranted(void Function() action) {
    _onPermissionGranted = action;
  }

  void setOnCopySuccess(void Function() action) {
    _onCopySuccess = action;
  }

  void setOnCopyFailure(void Function() action) {
    _onCopyFailure = action;
  }
}
