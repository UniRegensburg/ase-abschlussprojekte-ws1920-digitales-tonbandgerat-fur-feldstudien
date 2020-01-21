package de.ur.mi.audidroid.models

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

/**
 *
 * @author
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

        override fun doInBackground(vararg p0: EntryEntity?) {
            entryDao.delete(p0[0]!!)
        }
    }
}