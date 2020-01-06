package de.ur.mi.audidroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.EntryRepository
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class FilesViewModel (application: Application) : AndroidViewModel(application) {

    private var repository: EntryRepository = EntryRepository(application)
    private var allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()

    fun delete(entryEntity: EntryEntity) {
        repository.delete(entryEntity)
    }

    fun getAllRecordings(): LiveData<List<EntryEntity>> {
        return allRecordings
    }
}
