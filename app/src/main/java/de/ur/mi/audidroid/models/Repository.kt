package de.ur.mi.audidroid.models

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

/**
 * The Repository isolates the data layer from the rest of the app.
 * @author: Theresa Strohmeier
 */
class Repository(application: Application) {

    private var entryDao: EntryDao
    private var allRecordings: LiveData<List<EntryEntity>>

    init {
        val database: RecorderDatabase = RecorderDatabase.getInstance(
            application.applicationContext
        )
        entryDao = database.entryDao()
        allRecordings = entryDao.getAllRecordings()
    }

    fun delete(entryEntity: EntryEntity) {
        DeleteAsyncTask(entryDao).execute(entryEntity)
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }

    private class DeleteAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.delete(params[0]!!)
        }
    }

    fun insert(entryEntity: EntryEntity): Long{
        val id = InsertAsyncTask(entryDao).execute(entryEntity).get()
        return id
    }

    private class InsertAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Long>() {

        override fun doInBackground(vararg params: EntryEntity?):Long {
            return entryDao.insert(params[0]!!)
        }
    }

    fun insertMark(marker: MarkerEntity){
        InsertAsyncMark(entryDao).execute(marker)
    }

    private class InsertAsyncMark(val entryDao: EntryDao) :
        AsyncTask<MarkerEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: MarkerEntity?) {
            entryDao.insertMark(params[0]!!)
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
        return GetRecordingInclMarks(entryDao).execute(udi).get()
    }

    private class GetRecordingInclMarks(val entryDao: EntryDao) :
        AsyncTask<Int, Unit, List<RecordingAndMarker>>() {

        override fun doInBackground(vararg params: Int?): List<RecordingAndMarker> {
            return entryDao.getRecordingInclMarks(params[0]!!)
        }
    }
}
