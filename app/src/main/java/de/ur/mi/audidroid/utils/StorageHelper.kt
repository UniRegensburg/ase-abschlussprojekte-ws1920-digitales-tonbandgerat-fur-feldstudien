package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import java.io.File
import java.io.IOException

/**
 * StorageHelper provides central functions for the handling of files and folders.
 * @author: Lisa Sanladerer
 */

object StorageHelper {

    fun setOpenDocumentTreeIntent():Intent{
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    fun checkFileExistence(context: Context, recodingPath: String, recodingName: String):Boolean{
        var fileExists = false
        if (recodingPath.startsWith(context.resources.getString(R.string.content_uri_prefix))){
            val treeUri = Uri.parse(recodingPath)
            val filename = recodingName +  context.resources.getString(R.string.suffix_audio_file)
            val file = DocumentFile.fromTreeUri(context,treeUri)!!.findFile(filename)
            if (file != null){fileExists = true }
        }else{
            val file = File(recodingPath)
            if (file.exists()){fileExists = true}
        }
        return fileExists
    }

    fun handleFolderReference(path: String, allFolders: List<FolderEntity>, repository: Repository):Int{
        allFolders.forEach {
            if (it.dirPath == path){return it.uid }
        }
        return createFolderFromUri(repository, path)
    }

    fun deleteFile(context: Context, recordingPath: String, recodingName: String): Boolean{
        val deletedSuccessfully: Boolean
        if(recordingPath.startsWith(context.resources.getString(R.string.content_uri_prefix))){
            deletedSuccessfully = deleteExternalFile(context, recordingPath, recodingName)
        }else{
            deletedSuccessfully = File(recordingPath).delete()
        }
        return deletedSuccessfully
    }

    private fun deleteExternalFile(context: Context, path: String, name: String): Boolean{
        val treeUri = Uri.parse(path)
        val filename = name +  context.resources.getString(R.string.suffix_audio_file)
        val file = DocumentFile.fromTreeUri(context,treeUri)!!.findFile(filename)
        if (file!!.exists()){
            return file.delete()
        }
        return false
    }

    private fun internalFolderDescr(name: String, descr: String?):String{
        var folderDescr = name
        if (descr != null){
            folderDescr = descr + "/" + name
        }
        return folderDescr
    }

    fun createInternalFolderEntity(name: String, parentFolder: FolderEntity?): FolderEntity{
        var nestingDescr: String? = null
        var parentFolderRef: Int? = null

        if (parentFolder != null){
            nestingDescr = internalFolderDescr(parentFolder.folderName, parentFolder.nestingDescr)
            parentFolderRef = parentFolder.uid
        }

        return  FolderEntity(0, name,null,
            false, parentFolderRef , nestingDescr)
    }

    //Returns the last component of the external folder (to be used as name).
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

    //Returns a sorted list of FolderEntries, derived from the parentDir reference.
    fun getInternalFolderHierarchy(allFolders: List<FolderEntity>?): List<FolderEntity>? {
        if (allFolders != null && allFolders.isNotEmpty()) {
            val foldersSorted: MutableList<FolderEntity> = mutableListOf()
            allFolders.forEach {
                if (it.parentDir == null) {
                    foldersSorted.add(it)
                }
            }
            while (foldersSorted.size != allFolders.size){
                for (i in 0 until foldersSorted.size){
                    for(x in 0 until allFolders.size){
                        if (!foldersSorted.contains(allFolders[x])){
                            if (allFolders[x].parentDir == foldersSorted[i].uid){
                                foldersSorted.add(i+1, allFolders[x])
                            }
                        }
                    }
                }
            }
            return foldersSorted
        }
        return null
    }


    fun getExternalFolderPath(context: Context,path: String, name: String): String?{
        if (path.startsWith(context.resources.getString(R.string.content_uri_prefix))) {
            return path.substringBeforeLast(name)
        }
        return null
    }

    private fun getDocumentName(context: Context?, name: String): String{
        return name + context!!.resources.getString(R.string.suffix_audio_file)
    }

    fun moveRecordingExternally(context: Context, recording: RecordingAndLabels, folderPath: String): String?{
        var newPath: String? = null
        if(folderPath.contains(context.getString(R.string.content_uri_prefix))){
            println(folderPath)
            val dstfolderUri = Uri.parse(folderPath)
            val dstFile = DocumentFile.fromTreeUri(context, dstfolderUri)!!
                .createFile("aac", getDocumentName(context, recording.recordingName))
            if (recording.recordingPath.contains(context.getString(R.string.content_uri_prefix))){
                val srcFolderUri = Uri.parse(recording.recordingPath)
                val filename = recording.recordingName +  context.resources.getString(R.string.suffix_audio_file)
                val srcTree = DocumentFile.fromTreeUri(context, srcFolderUri)
                val srcFile = srcTree!!.findFile(filename)

                if (copyExternalFile(context, srcFile!!.uri, dstFile!!.uri )){
                    srcFile.delete()
                    newPath = dstFile.uri.toString()
                }
            }else {
                val srcFile = File(recording.recordingPath)
                if (copyToExternalFile(context, srcFile, dstFile!!)){
                    srcFile.delete()
                    newPath = dstFile.uri.toString()
                }
            }
        }
        return newPath
    }

    private fun copyExternalFile(context: Context, src: Uri, dst: Uri): Boolean{
        try {
            val inputStream = context.contentResolver.openInputStream(src)
            val outputStream = context.contentResolver.openOutputStream(dst)
            inputStream!!.copyTo(outputStream!!)
            inputStream.close()
            outputStream.close()
            return true
        }catch (e: IOException){
            return false
        }
    }

    private fun copyToExternalFile (context: Context, src: File, dst: DocumentFile): Boolean{
        try {
            val inputStream = src.inputStream()
            val outputStream = context.contentResolver.openOutputStream(dst.uri)
            inputStream.copyTo(outputStream!!)
            inputStream.close()
            outputStream.close()
            return true
        }catch (e: IOException){
            return false
        }
    }

    // Checks if an external path already has a reference in Database.
    fun checkExternalFolderReference(allFolderPaths: List<String>,path: String): Int?{
        var index: Int? = null
        for (i in allFolderPaths.indices){
            if (allFolderPaths[i] == path){
                index = i
                break
            }
        }
        return index
    }

    fun createFolderFromUri(repository: Repository ,path: String): Int{
        val uri = Uri.parse(path)
        val name = getFolderName(uri.lastPathSegment.toString())
        val newFolderEntity = FolderEntity(0, name,
            path, true, null, uri.lastPathSegment.toString())
        return repository.insertFolder(newFolderEntity).toInt()
    }

    //Creates an external File and copies the content of a File there.
    fun createExternalFile(context: Context,tempFile: File,name: String, treeUri: Uri): String{
        val newName = getDocumentName(context, name)
        val preferredDir = DocumentFile.fromTreeUri(context, treeUri)!!
        val newExternalFile = preferredDir.createFile("aac",newName)!!
        copyToExternalFile(context, tempFile, newExternalFile)
        return newExternalFile.uri.toString()
    }

    fun handleExternalFolderDeletion(context: Context, path: String): Boolean{
        var sucessfull = false
        if (checkExternalFolderEmpty(context,path)){
           sucessfull = deleteExternalFolder(context,path)
        }
        return sucessfull
    }

    private fun deleteExternalFolder(context: Context, path: String): Boolean{
        val treeUri = Uri.parse(path)
        val dir = DocumentFile.fromTreeUri(context,treeUri)

        if (dir!!.isDirectory){
            return dir.delete()
        }
        return false
    }

    //checks if external folder is empty
    private fun checkExternalFolderEmpty(context: Context, path: String): Boolean{
        val treeUri = Uri.parse(path)
        val dir = DocumentFile.fromTreeUri(context,treeUri)
        if (dir!!.listFiles().isEmpty()){
            return true
        }
        return false
    }

    //Returns all subfolders of a folder, derived from the parentDir reference.
    fun getAllInternalSubFolders(allFolders: List<FolderEntity>, folderList: MutableList<FolderEntity>): MutableList<FolderEntity>{
        val foldersToBeDeleted = folderList
        foldersToBeDeleted.forEach { parent ->
            allFolders.forEach {
                if (!foldersToBeDeleted.contains(it)){
                    if (parent.uid == it.parentDir){
                        foldersToBeDeleted.add(it)
                        return getAllInternalSubFolders(allFolders, foldersToBeDeleted)
                    }
                }
            }
        }
        return foldersToBeDeleted
    }
}