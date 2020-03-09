package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import java.io.File
import java.io.IOException
import java.util.ArrayList


/**
 * StorageHelper provides central functions for the handling of files and folders.
 * @author: Lisa Sanladerer
 */
object StorageHelper {
    fun handleFolderReferece(path: String, allFolders: List<FolderEntity>, repository: Repository):Int{
        allFolders.forEach {
            if (it.dirPath == path){
               return it.uid
            }
        }
        return createFolderFromUri(repository, path)
    }

    fun setOpenDocumentTreeIntent():Intent{
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    fun deleteFile(context: Context, entryEntity: EntryEntity): Boolean{
        val deletedSuccessfully: Boolean
        if(entryEntity.recordingPath.startsWith(context.resources.getString(R.string.content_uri_prefix))){
            deletedSuccessfully = deleteExternalFile(context, entryEntity.recordingPath, entryEntity.recordingName)
        }else{
            deletedSuccessfully = File(entryEntity.recordingPath).delete()
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
        val newFolderEntity = FolderEntity(0, name,null,
            false, parentFolderRef , nestingDescr)
        return newFolderEntity
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
    fun getInternalFolderHierarchy(allFolders: List<FolderEntity>): List<FolderEntity>? {

        if (allFolders.isNotEmpty()) {
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

    //Checks if String is a potential ContentUri.
    fun getExternalFolderPath(context: Context,path: String, name: String): String?{
        if (path.startsWith(context.resources.getString(R.string.content_uri_prefix))) {
            return path.substringBeforeLast(name)
        }
        return null
    }

    fun getDocumentName(context: Context?, name: String): String{
        return name + context!!.resources.getString(R.string.suffix_audio_file)
    }

    //==================================================================================================
    fun moveEntryStorage(context: Context, entryEntity: EntryEntity, folderPath: String): String?{
        var newPath: String? = null
        if(folderPath.contains(context.getString(R.string.content_uri_prefix))){
            println(entryEntity.recordingPath)

            val srcFile = File(entryEntity.recordingPath)
            val uri = Uri.parse(folderPath)
            val dstFile = DocumentFile.fromTreeUri(context, uri)!!
                .createFile("aac", getDocumentName(context,entryEntity.recordingName))
            copyToExternalFile(context, srcFile , dstFile!!)
            srcFile.delete()

            newPath = dstFile.uri.toString()
        }
        return newPath
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
        var name = getFolderName(uri.lastPathSegment.toString())
        val newFolderEntity = FolderEntity(0, name,
            path, true, null, uri.lastPathSegment.toString())
        println(newFolderEntity)
        return repository.insertFolder(newFolderEntity).toInt()
    }


    //creates an external File and copies the content of a File there
    fun createExternalFile(context: Context,tempFile: File,name: String, treeUri: Uri): String{
        val res = context.resources
        val newName = getDocumentName(context, name)
        val preferredDir = DocumentFile.fromTreeUri(context, treeUri)!!
        val newExternalFile = preferredDir.createFile("aac",newName)!!
        copyToExternalFile(context, tempFile, newExternalFile)
        return newExternalFile.uri.toString()
    }

    //copies content of a File to DocumentFile
    fun copyToExternalFile (context: Context, src: File, dst: DocumentFile): Boolean{
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