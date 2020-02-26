package de.ur.mi.audidroid.models

import android.app.Application
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
        DeleteAsyncTask(entryDao).execute(entryEntity)
    }

    private class DeleteAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.delete(params[0]!!)
        }
    }

    fun deleteLabel(labelEntity: LabelEntity) {
        CoroutineScope(coroutineContext).launch {
            labelDao.delete(labelEntity)
        }
    }

    fun insert(entryEntity: EntryEntity): Long{
        return InsertAsyncTask(entryDao).execute(entryEntity).get()
    }

    private class InsertAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Long>() {

        override fun doInBackground(vararg params: EntryEntity?): Long{
            return entryDao.insert(params[0]!!)
        }
    }

    fun insertMark(marker: MarkerTimeRelation){
        //InsertAsyncMark(entryDao).execute(marker)
        InsertAsyncMark(markerDao).execute(marker)
    }

    private class InsertAsyncMark(val markerDao: MarkerDao) :
        AsyncTask<MarkerTimeRelation, Unit, Unit>() {

        override fun doInBackground(vararg params: MarkerTimeRelation?) {
            markerDao.insertMark(params[0]!!)
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

    fun getRecordingWithId(entryEntity: EntryEntity) {
        GetRecordingWithId(entryDao).execute(entryEntity)
    }

    private class GetRecordingWithId(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.getRecordingWithId(params[0]!!.uid)
        }
    }

    fun getRecordingInclMarks(udi : Int): List<RecordingAndMarker> {
        return GetRecordingInclMarks(markerDao).execute(udi).get()
    }

    private class GetRecordingInclMarks(val markerDao: MarkerDao) :
        AsyncTask<Int, Unit, List<RecordingAndMarker>>() {

        override fun doInBackground(vararg params: Int?): List<RecordingAndMarker> {
            return markerDao.getRecordingInclMarks(params[0]!!)
        }
    }

    fun getLabelById(labelEntity: LabelEntity): LiveData<LabelEntity> {
        return labelDao.getLabelById(labelEntity.uid)
    }
}

