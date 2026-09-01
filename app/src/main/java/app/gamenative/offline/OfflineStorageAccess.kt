package app.gamenative.offline

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

object OfflineStorageAccess {
    fun hasRequiredAccess(context: Context): Boolean {
        val readWriteAccess = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        val allFilesAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false
        }
        return hasRequiredAccess(Build.VERSION.SDK_INT, allFilesAccess, readWriteAccess)
    }

    fun hasRequiredAccess(sdkInt: Int, allFilesAccess: Boolean, readWriteAccess: Boolean): Boolean =
        if (sdkInt >= Build.VERSION_CODES.R) allFilesAccess else readWriteAccess
}
