
package de.ur.mi.audidroid.models

import android.app.Application
import android.icu.text.CaseMap
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * The Repository isolates the data layer from the rest of the app.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

class Repository(application: Application): CoroutineScope {

    private var entryDao: EntryDao
    private var labelDao: LabelDao
    private var folderDao: FolderDao
    private var markerDao: MarkerDao
    private var allRecordings: LiveData<List<EntryEntity>>
    private var allFolders: LiveData<List<FolderEntity>>

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        val database: RecorderDatabase = RecorderDatabase.getInstance(
            application.applicationContext
        )
        entryDao = database.entryDao()
        labelDao = database.labelDao()
        folderDao = database.folderDao()
        markerDao = database.markerDao()
        allRecordings = entryDao.getAllRecordings()
        allFolders = folderDao.getAllFolders()
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }


    fun getAllLabels(): LiveData<List<LabelEntity>> {
        return labelDao.getAllLabels()
    }

    fun getAllFolders(): LiveData<List<FolderEntity>>{
        return allFolders
    }
    fun delete(entryEntity: EntryEntity) {
        CoroutineScope(coroutineContext).launch {
            entryDao.delete(entryEntity)
        }
    }

    fun deleteLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.delete(labelEntity)
        }
    }

    fun deleteFolder(folderEntity: FolderEntity) {
        CoroutineScope(coroutineContext).launch {
            folderDao.delete(folderEntity)
        }
    }

    fun insert(entryEntity: EntryEntity): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = entryDao.insert(entryEntity)
            }
        }
        return temp!!
    }

    fun insertMark(marker: MarkerTimeRelation){
        CoroutineScope(coroutineContext).launch {
            markerDao.insertMark(marker)
        }
    }

    fun insertLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.insert(labelEntity)
        }
    }

    fun insertFolder(folderEntity: FolderEntity): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = folderDao.insert(folderEntity)
            }
        }
        return temp!!
    }

    fun updateEntry(entryEntity: EntryEntity) {
        CoroutineScope(coroutineContext).launch {
            entryDao.update(entryEntity)
        }
    }

    fun updateLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.update(labelEntity)
        }
    }

    fun getRecordingFromIdInclMarks(uid : Int): List<RecordingAndMarker> {
        return markerDao.getRecordingFromIdInclMarks(uid)
    }

    fun getLabelById(labelEntity: LabelEntity): LiveData<LabelEntity> {
        return labelDao.getLabelById(labelEntity.uid)
    }
    fun getFolderById(uid: Int): LiveData<FolderEntity> {
        return folderDao.getFolderById(uid)
    }

    fun getRecordingByFolder(uid: Int?): LiveData<List<EntryEntity>>{
        return entryDao.getRecordingByFolder(uid)
    }

    fun getRecordingWithNoFolder(): LiveData<List<EntryEntity>>{
        return entryDao.getRecordingWithNoFolder()
    }

    fun getFolderByPath(path: String): LiveData<FolderEntity>{
        return folderDao.getFolderByPath(path)
    }

    fun getFolderByStorage(isExternal: Boolean): LiveData<List<FolderEntity>>{
        return folderDao.getFolderByStorage(isExternal)
    }
}
