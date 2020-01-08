package de.ur.mi.audidroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.EntryRepository

class FilesViewModel (dataSource: EntryRepository, application: Application) : AndroidViewModel(application) {

    private val repository = dataSource

    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()

    fun delete(entryEntity: EntryEntity) {
        repository.delete(entryEntity)
    }
}
