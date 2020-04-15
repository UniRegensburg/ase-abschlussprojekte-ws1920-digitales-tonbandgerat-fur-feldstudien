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

    private var recordingDao: RecordingDao
    private var labelDao: LabelDao
    private var labelAssignmentDao: LabelAssignmentDao
    private var markerDao: MarkerDao
    private var allRecordings: LiveData<List<RecordingEntity>>

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        val database: RecorderDatabase = RecorderDatabase.getInstance(
            application.applicationContext
        )
        recordingDao = database.recordingDao()
        labelDao = database.labelDao()
        labelAssignmentDao = database.labelAssignmentDao()
        markerDao = database.markerDao()
        allRecordings = recordingDao.getAllRecordings()
    }

    /** Recordings */

    fun insertRecording(recordingEntity: RecordingEntity): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = recordingDao.insert(recordingEntity)
            }
        }
        return temp!!
    }

    fun deleteRecording(uid: Int) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                recordingDao.delete(uid)
            }
        }
    }

    fun getAllRecordings(): LiveData<List<RecordingEntity>> {
        return allRecordings
    }

    fun getRecordingById(uid: Int): LiveData<RecordingEntity> {
        return recordingDao.getRecordingById(uid)
    }


    /** Labels */

    fun insertLabel(labelEntity: LabelEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                labelDao.insert(labelEntity)
            }
        }
    }

    fun updateLabel(labelEntity: LabelEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                labelDao.update(labelEntity)
            }
        }
    }

    fun deleteLabel(labelEntity: LabelEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                labelDao.delete(labelEntity)
            }
        }
    }

    fun getAllLabels(): LiveData<List<LabelEntity>> {
        return labelDao.getAllLabels()
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


    /** Recordinglabels */

    fun insertRecLabels(labelAssignment: LabelAssignmentEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                labelAssignmentDao.insertRecLabels(labelAssignment)
            }
        }
    }

    fun deleteRecLabels(uid: Int) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                labelAssignmentDao.deleteRecLabels(uid)
            }
        }
    }

    fun getRecLabelsById(uid: Int): LiveData<List<LabelEntity>> {
        return labelDao.getRecLabelsById(uid)
    }

    fun getAllRecordingsWithLabels(): LiveData<List<RecordingAndLabels>> {
        return labelDao.getAllRecordingsWithLabels()
    }


    /** Markers */

    fun insertMarker(markerEntity: MarkerEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                markerDao.insertMarker(markerEntity)
            }
        }
    }

    fun updateMarker(markerEntity: MarkerEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                markerDao.updateMarker(markerEntity)
            }
        }
    }

    fun deleteMarker(markerEntity: MarkerEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                markerDao.deleteMarker(markerEntity)
            }
        }
    }

    fun getAllMarkers(): LiveData<List<MarkerEntity>> {
        return markerDao.getAllMarkers()
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

    fun getMarkerCount(): Int {
        var count: Int? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                count = markerDao.getMarkerCount()
            }
        }
        return count!!
    }


    /** MarkTimestamp */

    fun insertMarkTimestamp(marker: MarkTimestamp) {
        CoroutineScope(coroutineContext).launch {
            markerDao.insertMark(marker)
        }
    }

    fun getAllMarks(uid: Int): LiveData<List<MarkAndTimestamp>> {
        return markerDao.getMarksById(uid)
    }

    fun updateMarkTimestamp(markTimestamp: MarkTimestamp) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateMarkTimestamp(markTimestamp)
        }
    }

    fun deleteMarkTimestamp(mid: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteMark(mid)
        }
    }

    fun deleteRecMarks(uid: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteRecMarks(uid)
        }
    }
}
