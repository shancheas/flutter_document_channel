package id.kudzoza.document_writer

import android.app.Activity
import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val documentWriter by lazy { DocumentWriter(this) }

    private lateinit var methodChannel: MethodChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "init" -> initUriPermission()
                "inject" -> injectFile(call.arguments as String)
            }
        }
    }

    private fun initUriPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            documentWriter.launch(
                    onUriGranted = {
                        methodChannel.invokeMethod("on_uri_granted", null)
                    },
                    onUriDenied = {
                        startActivityForResult(
                                documentWriter.actionOpenDocumentTree,
                                REQUEST_URI_PERMISSION_CODE
                        )
                    }
            )
        }
    }

    private fun injectFile(target: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            documentWriter.write {
                it.findFile(target)
                        ?.createFile(
                                "plain/text",
                                "inject.txt"
                        )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_URI_PERMISSION_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                documentWriter.takePersistablePermission(directoryUri)
        }
    }

    companion object {
        const val CHANNEL = "id.kudzoza.document_writer/sample"
        const val REQUEST_URI_PERMISSION_CODE = 100
    }

}
