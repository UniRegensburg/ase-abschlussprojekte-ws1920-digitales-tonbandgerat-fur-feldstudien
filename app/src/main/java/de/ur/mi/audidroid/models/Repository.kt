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

    fun delete(entryEntity: EntryEntity): Int {
        val deletedRecordings = DeleteAsyncTask(entryDao).execute(entryEntity).get()
        return deletedRecordings
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }

    private class DeleteAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Int>() {

        override fun doInBackground(vararg params: EntryEntity?): Int {
            return entryDao.delete(params[0]!!)
        }
    }

    fun insert(entryEntity: EntryEntity) {
        InsertAsyncTask(entryDao).execute(entryEntity)
    }

    private class InsertAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.insert(params[0]!!)
        }
    }

    fun getRecordingWithId(recordingId: Int): EntryEntity {
        val recording = GetRecordingWithId(entryDao).execute(recordingId).get()
        return recording
    }

    private class GetRecordingWithId(val entryDao: EntryDao) :
        AsyncTask<Int, Unit, EntryEntity>() {

        override fun doInBackground(vararg params: Int?): EntryEntity {
            return entryDao.getRecordingWithId(params[0]!!)
        }
    }
}
