package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.FolderViewModel
import java.io.File

object StorageHelper {

    //checks if String is contentUri and returns dir
    fun getExternalFolderPath(context: Context,path: String, name: String): String?{
        if (path.startsWith(context.resources.getString(R.string.content_uri_prefix))) {
            return path.substringBeforeLast(name)
        }
        return null
    }


    // check if external Folder has already an entry in DB
    fun checkExternalFolderReference(allFolders: List<FolderEntity>,path: String): Int?{
        val folders = allFolders
        folders!!.forEach {
            if (it.dirPath == path){
                return it.uid
            }
        }
        return null
    }
    fun createFolderFromUri(repository: Repository ,path: String): Int{
        val uri = Uri.parse(path)
        var name = StorageHelper.getFolderName(uri.lastPathSegment.toString())
        val newFolderEntity = FolderEntity(0, name,
            path, true, null,uri.lastPathSegment.toString())
        return repository.insertFolder(newFolderEntity).toInt()
    }

    //returns a usable name from uri.lastPathSegment
    fun getFolderName(name:String):String{
        var newName = name.trim()
        newName = newName.substringAfter(":")
        if (newName.last() == '/'){
            newName = newName.dropLast(1)}
        if(newName.contains("/")){
            newName = newName.substringAfterLast("/")
        }
        return newName
    }
    //creates an external File and copies the content of a File there
    fun createExternalFile(context: Context,tempFile: File,name: String, treeUri: Uri): String{
        val res = context.resources
        val newName = name + res.getString(R.string.suffix_audio_file)
        val preferredDir = DocumentFile.fromTreeUri(context!!, treeUri)!!
        val newExternalFile = preferredDir.createFile("aac",newName)!!
        copyToExternalFile(context, tempFile, newExternalFile)
        return newExternalFile.uri!!.toString()
    }

    //copies content of a File to DocumentFile
    fun copyToExternalFile (context: Context, src: File, dst: DocumentFile){
        val inputStream = src.inputStream()
        val outputStream = context.contentResolver.openOutputStream(dst.uri)
        inputStream.copyTo(outputStream!!)
        inputStream.close()
        outputStream.close()
    }

    // deletes list of recordings in folder; if folder is empty afterwards delete it too
    fun deleteExternalFolderAndContent(context: Context, path: String, names: List<String>): Boolean{
        var sucessfull = false
        names.forEach {
            sucessfull = deleteExternalFile(context, path, it)
        }
        if (checkExternalFolderEmpty(context,path)){
            deleteExternalFolder(context,path)
        }
        return sucessfull
    }
    //deletes an external file
    fun deleteExternalFile(context: Context, path: String, name: String): Boolean{
        val treeUri = Uri.parse(path)
        val filename = name +  context.resources.getString(R.string.suffix_audio_file)
        val file = DocumentFile.fromTreeUri(context,treeUri)!!.findFile(filename)

        if (file!!.exists()){
            return file.delete()
        }
        return false
    }

    fun deleteExternalFolder(context: Context, path: String): Boolean{
        val treeUri = Uri.parse(path)
        val dir = DocumentFile.fromTreeUri(context,treeUri)

        if (dir!!.isDirectory){
            return dir.delete()
        }
        return false
    }

    //checks if external folder is empty
    fun checkExternalFolderEmpty(context: Context, path: String): Boolean{
        val treeUri = Uri.parse(path)
        val dir = DocumentFile.fromTreeUri(context,treeUri)

        if (dir!!.listFiles() == null){
            return true
        }
        return false
    }
}