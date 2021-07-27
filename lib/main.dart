import 'dart:io';

import 'package:document_writer/document_writer.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

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
  var documentWriter = DocumentWriter();

  var _copyProcess = false;

  void _setCopyProcess(bool isProcessing) {
    setState(() {
      _copyProcess = isProcessing;
    });
  }

  @override
  void initState() {
    super.initState();

    documentWriter.setOnPermissionAlreadyGranted(() {
      print("Has Granted");
    });

    documentWriter.setOnPermissionGranted(() {
      print("Granted");
    });

    documentWriter.setOnCopySuccess(() {
      _setCopyProcess(false);
      print("Success");
    });

    documentWriter.setOnCopyFailure(() {
      _setCopyProcess(false);
      print("Error");
    });
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
            MaterialButton(
              child: Text("Request Permission"),
              color: Colors.black,
              textColor: Colors.white,
              onPressed: () {
                if (Platform.isAndroid) {
                  documentWriter.init();
                }
              },
            ),
            MaterialButton(
              child: Text("Copy File"),
              color: Colors.blueAccent,
              textColor: Colors.white,
              onPressed: () {
                if (Platform.isAndroid) {
                  _setCopyProcess(true);
                  documentWriter.copy(
                    from: "/storage/emulated/0/Download/tes.png",
                    to: "com.byu.id/tes.png",
                  );
                }
              },
            ),
            Visibility(
              visible: _copyProcess,
              child: CircularProgressIndicator(),
            )
          ],
        ),
      ),
    );
  }
}
