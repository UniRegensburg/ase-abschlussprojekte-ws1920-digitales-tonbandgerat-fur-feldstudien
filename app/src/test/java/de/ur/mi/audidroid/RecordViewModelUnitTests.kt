package de.ur.mi.audidroid

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import de.ur.mi.audidroid.models.EntryDao
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.RecorderDatabase
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import androidx.test.platform.app.InstrumentationRegistry


@RunWith(MockitoJUnitRunner::class)
class RecordViewModelUnitTests {


    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var database: RoomDatabase

    private lateinit var testEntity: EntryEntity

    private lateinit var db: RecorderDatabase

    private lateinit var testDao: EntryDao

    private val testUid = 12


    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, RecorderDatabase::class.java
        ).build()
        testDao = db.entryDao()
        testEntity = EntryEntity(testUid, "test", "test")
    }

    @Test
    @Throws(Exception::class)
    fun testImport() {
        val currentNumberEntries = testDao.getAllRecordings()
        testDao.insert(testEntity)
        assertEquals(currentNumberEntries.size + 1, testDao.getAllRecordings())
        val byId = testDao.loadEntryById(testUid)
        assertThat(byId, equalTo(testEntity))
    }

    @Test
    @Throws(Exception::class)
    fun testClearTable() {
        testDao.clearTable()
        assertTrue(testDao.getAllRecordings().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testRowCount() {
        testClearTable()
        testImport()
        assertTrue(testDao.getRowCount() == 1)
    }
}
