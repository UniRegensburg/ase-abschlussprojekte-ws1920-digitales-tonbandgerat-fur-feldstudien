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

    fun getAllMarkers(): LiveData<List<MarkerEntity>> {
        return markerDao.getAllMarkers()
    }

    fun getMarkerCount(): Int {
        var count: Int? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                count = markerDao.getMarkerCount()
            }
        }
        return count!!
    }

    fun deleteRecording(uid: Int) {
        CoroutineScope(coroutineContext).launch {
            entryDao.delete(uid)
        }
    }

    fun deleteLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.delete(labelEntity)
        }
    }

    fun deleteMarker(markerEntity: MarkerEntity) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteMarker(markerEntity)
        }
    }

    fun insertRecording(entryEntity: EntryEntity): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = entryDao.insert(entryEntity)
            }
        }
        return temp!!
    }

    fun insertMarker(markerEntity: MarkerEntity) {
        CoroutineScope(coroutineContext).launch {
            markerDao.insertMarker(markerEntity)
        }
    }

    fun insertMark(marker: MarkTimestamp) {
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

    fun updateMarker(markerEntity: MarkerEntity) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateMarker(markerEntity)
        }
    }

    fun getAllMarks(uid: Int): LiveData<List<MarkAndTimestamp>> {
        return markerDao.getMarksById(uid)
    }

    fun getMarkerByName(name: String): List<MarkerEntity> {
        var list: List<MarkerEntity>? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                list = markerDao.getMarkerByName(name)
            }
        }
        return list!!
    }

    fun updateMarkTimestamp(markTimestamp: MarkTimestamp) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateMarkTimestamp(markTimestamp)
        }
    }

    fun getRecLabelsById(uid: Int): LiveData<List<LabelEntity>> {
        return labelDao.getRecLabelsById(uid)

    fun getRecordingById(uid: Int): LiveData<EntryEntity> {
        return entryDao.getRecordingById(uid)
    }

    fun getLabelByName(name: String): List<LabelEntity> {
        var list: List<LabelEntity>? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                list = labelDao.getLabelByName(name)
            }
        }
        return list!!
    }

    fun getAllRecordingsWithLabels(): LiveData<List<RecordingAndLabels>> {
        return labelDao.getAllRecordingsWithLabels()
    }

    fun insertRecLabels(labelAssignment: LabelAssignmentEntity) {
        CoroutineScope(coroutineContext).launch {
            labelAssignmentDao.insertRecLabels(labelAssignment)
        }
    }

    fun deleteRecMarks(uid: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteRecMarks(uid)
        }
    }

    fun deleteMark(mid: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteMark(mid)
        }
    }

    fun deleteRecLabels(uid: Int) {
        CoroutineScope(coroutineContext).launch {
            labelAssignmentDao.deleteRecLabels(uid)
        }
    }
}
