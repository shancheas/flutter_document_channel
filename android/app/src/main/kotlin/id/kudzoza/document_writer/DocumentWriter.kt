package id.kudzoza.document_writer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile

/**
 * Created by Kudzoza
 * on 25/07/2021
 **/

class DocumentWriter(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val androidDataUri: Uri = DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/data"
    )

    private val uriFlag: Int =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val androidDataTreeUri: Uri = DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/data"
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val actionOpenDocumentTree: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        Log.d("document uri :: ", androidDataUri.path.toString())
        flags = uriFlag
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, androidDataUri)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun launch(onUriGranted: () -> Unit, onUriDenied: () -> Unit) {
        context.contentResolver.persistedUriPermissions.find {
            it.uri.equals(androidDataTreeUri) && it.isReadPermission
        }?.run {
            onUriGranted.invoke()
        } ?: onUriDenied.invoke()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun write(action: (DocumentFile) -> Unit) {
        context.contentResolver.persistedUriPermissions.find {
            it.uri.equals(androidDataTreeUri) && it.isWritePermission
        }?.run {
            val documentTree = DocumentFile.fromTreeUri(context, androidDataTreeUri)
            if (documentTree != null) action.invoke(documentTree)
            else throw RuntimeException("Uri not registered")
        } ?: throw RuntimeException("Denied Permission")
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun takePersistablePermission(directoryUri: Uri) {
        context.contentResolver.takePersistableUriPermission(directoryUri, uriFlag)
    }
}