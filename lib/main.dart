import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: Page(),
    );
  }
}

class Page extends StatefulWidget {
  @override
  State createState() => _Page();
}

class _Page extends State<Page> {
  static const platform = const MethodChannel('id.kudzoza.document_writer/sample');

  @override
  void initState() {
    super.initState();
    platform.setMethodCallHandler(_platformCallHandler);
    WidgetsBinding.instance?.addPostFrameCallback((timeStamp) {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Document Writer"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("URI Permission : "),
            MaterialButton(
              child: Text("Request Permission"),
              color: Colors.blueAccent,
              textColor: Colors.white,
              onPressed: () {
                if (Platform.isAndroid) {
                  platform.invokeMethod("init");
                }
              },
            ),
            MaterialButton(
              child: Text("Create File"),
              color: Colors.black,
              textColor: Colors.white,
              onPressed: () {
                if (Platform.isAndroid) {
                  platform.invokeMethod("inject", "id.co.pqm.improve");
                }
              },
            )
          ],
        ),
      ),
    );
  }

  Future<dynamic> _platformCallHandler(MethodCall call) async {
    switch (call.method) {
      case "on_uri_granted":
        showDialog(
            context: context,
            builder: (context) {
              return AlertDialog(content: Text("Uri permission has been granted"));
            });
        break;
      case "on_document_created":
        break;
    }
  }
}
