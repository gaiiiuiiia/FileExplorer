package com.example.fileexplorer

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class FileOpener
{
    companion object
    {
        fun openFile(context: Context, file: File)
        {
            val selectedFile = file
            val uri = FileProvider.getUriForFile(context,
                    context.applicationContext.packageName + ".provider",
                        file)

            val intent = Intent(Intent.ACTION_VIEW)

            if (FileAllowManager.isAllowedImage(uri.toString())) {
                intent.setDataAndType(uri, "image/jpeg")
            } else {
                intent.setDataAndType(uri, "*/*")
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }
    }
}