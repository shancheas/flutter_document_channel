package id.kudzoza.document_writer

import android.content.Intent
import android.os.Build
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val documentWriter by lazy { DocumentWriter(this) }

    private lateinit var methodChannel: MethodChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(
                flutterEngine.dartExecutor.binaryMessenger,
                "$packageName/doc_writer"
        )
        methodChannel.setMethodCallHandler { call, _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                documentWriter.handleMethodCall(call, methodChannel)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            documentWriter.onActivityResult(requestCode, resultCode, data)
    }

}
