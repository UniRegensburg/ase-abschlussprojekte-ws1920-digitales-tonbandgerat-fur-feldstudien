package de.ur.mi.audidroid.models

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * The Repository isolates the data layer from the rest of the app.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

class Repository(application: Application) : CoroutineScope {

    private var entryDao: EntryDao
    private var labelDao: LabelDao
    private var labelAssignmentDao: LabelAssignmentDao
    private var markerDao: MarkerDao
    private var allRecordings: LiveData<List<EntryEntity>>

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        val database: RecorderDatabase = RecorderDatabase.getInstance(
            application.applicationContext
        )
        entryDao = database.entryDao()
        labelDao = database.labelDao()
        labelAssignmentDao = database.labelAssignmentDao()
        markerDao = database.markerDao()
        allRecordings = entryDao.getAllRecordings()
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }

    fun getAllLabels(): LiveData<List<LabelEntity>> {
        return labelDao.getAllLabels()
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

    fun insert(entryEntity: EntryEntity): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = entryDao.insert(entryEntity)
            }
        }
        return temp!!
    }

    fun insertMark(marker: MarkerEntity) {
        CoroutineScope(coroutineContext).launch {
            markerDao.insertMark(marker)
        }
    }

    fun insertLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.insert(labelEntity)
        }
    }

    fun updateLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.update(labelEntity)
        }
    }

    fun getRecordingFromIdInclMarks(uid: Int): LiveData<List<RecordingAndMarker>> {
        return markerDao.getRecordingFromIdInclMarks(uid)
    }

    fun getAllMarks(uid: Int): LiveData<List<MarkerEntity>> {
        return markerDao.getAllMarks(uid)
    }

    fun getLabelById(labelEntity: LabelEntity): LiveData<LabelEntity> {
        return labelDao.getLabelById(labelEntity.uid)

    }

    fun insertRecLabels(labelAssignment: LabelAssignmentEntity) {
        CoroutineScope(coroutineContext).launch {
            labelAssignmentDao.insertRecLabels(labelAssignment)
        }
    }

    fun getRecordingFromIdInclLabels(uid: Int): List<RecordingAndLabel> {
        return labelAssignmentDao.getRecordingFromIdInclLabels(uid)
    }
}
