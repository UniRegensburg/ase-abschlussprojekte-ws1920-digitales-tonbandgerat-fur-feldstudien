package de.ur.mi.audidroid.models

import org.junit.Test
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.After
import java.io.IOException
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

  /*  @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var database: RoomDatabase*/

    private lateinit var testEntity: EntryEntity

    private lateinit var db: RecorderDatabase

    private lateinit var testDao: EntryDao

    private val testUid = 12

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, RecorderDatabase::class.java
        ).build()
        testDao = db.entryDao()
        testEntity = EntryEntity(testUid, "test", "test", "test")
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testImport() {
        val currentNumberEntries = testDao.getAllRecordings().size
        testDao.insert(testEntity)
        assertEquals(currentNumberEntries + 1, testDao.getAllRecordings().size)
        val byId = testDao.loadEntryById(testUid)
        assertThat(byId, equalTo(testEntity))
    }

    @Test
    @Throws(Exception::class)
    fun testClearTable() {
        testDao.clearTable()
        assertTrue(testDao.getAllRecordings().isEmpty())
    }
}
