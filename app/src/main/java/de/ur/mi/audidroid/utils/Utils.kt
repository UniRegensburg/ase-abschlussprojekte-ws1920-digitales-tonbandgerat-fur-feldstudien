package de.ur.mi.audidroid.utils

import android.os.Environment
import java.io.File

object Utils {

    val outputPath: String
        get() {
            val path =
                Environment.getExternalStorageDirectory().toString() + File.separator + "example" + File.separator

            val folder = File(path)
            if (!folder.exists())
                folder.mkdirs()
            return path
        }

    fun getConvertedFile(folder: String, fileName: String): File {
        val f = File(folder)

        if (!f.exists())
            f.mkdir()

        return File(f.path + File.separator + fileName)
    }
}
