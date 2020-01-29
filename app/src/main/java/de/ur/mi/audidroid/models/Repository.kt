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

    fun insert(entryEntity: EntryEntity) {
        InsertAsyncTask(entryDao).execute(entryEntity)
    }

    private class InsertAsyncTask(val entryDao: EntryDao) :
        AsyncTask<EntryEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: EntryEntity?) {
            entryDao.insert(params[0]!!)
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
}
