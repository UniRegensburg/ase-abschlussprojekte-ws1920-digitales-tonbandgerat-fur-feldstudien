package de.ur.mi.audidroid.models

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

/**
 * The Repository isolates the data layer from the rest of the app.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

class Repository(application: Application) {

    private var entryDao: EntryDao
    private var labelDao: LabelDao
    private var allRecordings: LiveData<List<EntryEntity>>
    private var allLabels: LiveData<List<LabelEntity>>

    // TODO(refactor function calls into singular methods per function, and perform function based on type parameter)

    init {
        val database: RecorderDatabase = RecorderDatabase.getInstance(
            application.applicationContext
        )
        entryDao = database.entryDao()
        labelDao = database.labelDao()
        allRecordings = entryDao.getAllRecordings()
        allLabels = labelDao.getAllLabels()
    }



    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }

    fun getAllLabels(): LiveData<List<LabelEntity>> {
        return allLabels
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
        DeleteLabelAsyncTask(labelDao).execute(labelEntity)
    }

    private class DeleteLabelAsyncTask(val labelDao: LabelDao) :
        AsyncTask<LabelEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: LabelEntity?) {
            labelDao.delete(params[0]!!)
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

    fun insertLabel(labelEntity: LabelEntity) {
        InsertLabelAsyncTask(labelDao).execute(labelEntity)
    }

    private class InsertLabelAsyncTask(val labelDao: LabelDao) :
        AsyncTask<LabelEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: LabelEntity?) {
            labelDao.insert(params[0]!!)
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

    fun getLabelWithId(labelEntity: LabelEntity) {
        GetLabelWithId(labelDao).execute(labelEntity)
    }

    private class GetLabelWithId(val labelDao: LabelDao) :
        AsyncTask<LabelEntity, Unit, Unit>() {

        override fun doInBackground(vararg params: LabelEntity?) {
            labelDao.getLabelWithId(params[0]!!.uid)
        }
    }
}
