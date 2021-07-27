package id.kudzoza.document_writer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Created by Kudzoza
 * on 25/07/2021
 **/

class DocumentWriter(private val context: Activity) {

    private val uriFlag: Int =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private lateinit var channel: MethodChannel

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val androidDataUri: Uri = DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/data"
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val androidDataTreeUri: Uri = DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/data"
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val actionOpenDocumentTree: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        flags = uriFlag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, androidDataUri)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun launch(onUriGranted: () -> Unit, onUriDenied: () -> Unit) {
        context.contentResolver.persistedUriPermissions.find {
            it.uri.equals(androidDataTreeUri) && it.isReadPermission
        }?.run {
            onUriGranted.invoke()
        } ?: onUriDenied.invoke()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun write(action: (DocumentFile) -> Unit) {
        context.contentResolver.persistedUriPermissions.find {
            it.uri.equals(androidDataTreeUri) && it.isWritePermission
        }?.run {
            val documentTree = DocumentFile.fromTreeUri(context, androidDataTreeUri)
            if (documentTree != null) action.invoke(documentTree)
            else throw RuntimeException("Uri not registered")
        } ?: throw RuntimeException("Denied Permission")
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_URI_PERMISSION_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return
            context.contentResolver.takePersistableUriPermission(directoryUri, uriFlag)
            channel.invokeMethod("on_permission_granted", null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun handleMethodCall(call: MethodCall, methodChannel: MethodChannel) {
        channel = methodChannel
        when (call.method) {
            "init" -> {
                launch(onUriGranted = {
                    methodChannel.invokeMethod("on_permission_already_granted", null)
                }, onUriDenied = {
                    context.startActivityForResult(actionOpenDocumentTree, REQUEST_URI_PERMISSION_CODE)
                })
            }
            "copy" -> {
                copyDocument(
                        call.argument<String>("from").orEmpty(),
                        call.argument<String>("to").orEmpty(),
                        call.argument<String>("mime").orEmpty()
                )
            }
            else -> {
                channel.invokeMethod("undefined_method", null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun copyDocument(
            fromPath: String,
            toPath: String,
            mime: String
    ) {
        write { root ->
            val fromSource = fromPath.split("/")
            val toSource = toPath.split("/")

            var from: DocumentFile? = root
            repeat(fromSource.size) { index ->
                from = from?.findFile(fromSource[index])
            }

            var to: DocumentFile? = root
            repeat(toSource.size) { index ->
                val target = to?.findFile(toSource[index])
                to = if (target?.exists() == true) {
                    target
                } else {
                    if (index == toSource.lastIndex) {
                        to?.createFile(mime, toSource[index])
                    } else {
                        to?.createDirectory(toSource[index])
                    }
                }
            }


            try {
                val inputStream = context.contentResolver.openInputStream(from?.uri!!)!!
                val outputStream = context.contentResolver.openOutputStream(to?.uri!!)!!

                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { stream -> len = stream } != -1) {
                    outputStream.write(buffer, 0, len)
                }

                inputStream.close()
                outputStream.close()

                channel.invokeMethod("on_copy_success", null)
            } catch (e: Exception) {
                e.printStackTrace()
                channel.invokeMethod("on_copy_failed", null)
            }
        }
    }

    companion object {
        const val REQUEST_URI_PERMISSION_CODE = 100
    }
}