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
    private var folderDao: FolderDao
    private var folderAssignmentDao: FolderAssignmentDao
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
        folderDao = database.folderDao()
        folderAssignmentDao = database.folderAssignmentDao()
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

    fun getCopiedRecordingById(recordingId: Int): Long {
        var temp: Long? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = recordingDao.getCopiedRecordingById(recordingId)
            }
        }
        return temp!!
    }

    fun updateRecording(recording: RecordingEntity) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                recordingDao.updateRecording(recording)
            }
        }
    }

    fun updatePreviousRecording(copiedRecordingId: Int, name: String, path: String) {
        CoroutineScope(coroutineContext).launch {
            recordingDao.updatePreviousRecording(copiedRecordingId, name, path)
        }
    }

    fun updateNameAndPath(uid: Int, name: String, path: String, date: String) {
        CoroutineScope(coroutineContext).launch {
            recordingDao.updateNameAndPath(uid, name, path, date)
        }
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

    fun getAllRecWithLabelsOrderName(): LiveData<List<RecordingAndLabels>> {
        return labelDao.getAllRecordingsWithLabelsOrderName()
    }

    fun getAllRecWithLabelsOrderDate(): LiveData<List<RecordingAndLabels>> {
        return labelDao.getAllRecordingsWithLabelsOrderDate()
    }

    fun getAllRecWithLabelsOrderDuration(): LiveData<List<RecordingAndLabels>> {
        return labelDao.getAllRecordingsWithLabelsOrderDuration()
    }

    fun getRecordingsByFolder(folderId: Int): LiveData<List<RecordingAndLabels>> {
        var list: LiveData<List<RecordingAndLabels>>? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                list = folderAssignmentDao.getAllRecordingsOfFolder(folderId)
            }
        }
        return list!!
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

    fun getRecordingsAndMarkerType(): LiveData<List<RecordingAndMarkTuple>> {
        return markerDao.getRecordingsAndMarkerType()
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
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                markerDao.insertMark(marker)
            }
        }
    }

    fun updateMarkTimestamp(markTimestamp: MarkTimestamp) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateMarkTimestamp(markTimestamp)
        }
    }

    fun deleteMarkTimestamp(mid: Int) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                markerDao.deleteMark(mid)
            }
        }
    }

    fun deleteRecMarks(uid: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteRecMarks(uid)
        }
    }

    fun getAllMarks(uid: Int): LiveData<List<MarkAndTimestamp>> {
        var list: LiveData<List<MarkAndTimestamp>>? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                list = markerDao.getMarksById(uid)
            }
        }
        return list!!
    }

    fun copyMarks(recordingId: Int, copiedRecordingId: Int) {
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                markerDao.copyMarks(recordingId, copiedRecordingId)
            }
        }
    }

    fun deleteOuterMarks(copiedRecordingId: Int, startTimeInMilli: Int, endTimeInMilli: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteOuterMarks(copiedRecordingId, startTimeInMilli, endTimeInMilli)
        }
    }

    fun updateInnerMarks(copiedRecordingId: Int, startTimeInMilli: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateInnerMarks(copiedRecordingId, startTimeInMilli)
        }
    }

    fun deleteInnerMarks(copiedRecordingId: Int, startTimeInMilli: Int, endTimeInMilli: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.deleteInnerMarks(copiedRecordingId, startTimeInMilli, endTimeInMilli)
        }
    }

    fun updateOuterMarks(copiedRecordingId: Int, durationInMilli: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateOuterMarks(copiedRecordingId, durationInMilli)
        }
    }

    fun updateMarks(recordingId: Int, copiedRecording: Int) {
        CoroutineScope(coroutineContext).launch {
            markerDao.updateMarks(recordingId, copiedRecording)
        }
    }

    /** Folders **/

    fun insertFolder(folderEntity: FolderEntity) {
        CoroutineScope(coroutineContext).launch {
            folderDao.insertFolder(folderEntity)
        }
    }

    fun deleteFolder(folderEntity: FolderEntity) {
        CoroutineScope(coroutineContext).launch {
            folderDao.deleteFolder(folderEntity)
        }
    }

    fun updateFolder(folderEntity: FolderEntity) {
        CoroutineScope(coroutineContext).launch {
            folderDao.updateFolder(folderEntity)
        }
    }

    fun getAllFolders(): LiveData<List<FolderEntity>> {
        var temp: LiveData<List<FolderEntity>>? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = folderDao.getAllFolders()
            }
        }
        return temp!!
    }


    /** FolderAssignment **/

    fun insertFolderAssignment(folderAssignmentEntity: FolderAssignmentEntity) {
        CoroutineScope(coroutineContext).launch {
            folderAssignmentDao.insertFolderAssignment(folderAssignmentEntity)
        }
    }

    fun deleteFolderAssignment(recordingId: Int) {
        CoroutineScope(coroutineContext).launch {
            folderAssignmentDao.deleteFolderAssignment(recordingId)
        }
    }

    fun updateFolderAssignment(folderAssignmentEntity: FolderAssignmentEntity) {
        CoroutineScope(coroutineContext).launch {
            folderAssignmentDao.updateFolderAssignment(
                folderAssignmentEntity.primaryKey,
                folderAssignmentEntity.folderId
            )
        }
    }

    fun getFolderOfRecording(recordingId: Int): FolderAssignmentEntity? {
        var temp: FolderAssignmentEntity? = null
        runBlocking {
            CoroutineScope(coroutineContext).launch {
                temp = folderAssignmentDao.getFolderOfRecording(recordingId)
            }
        }
        return temp
    }
}
