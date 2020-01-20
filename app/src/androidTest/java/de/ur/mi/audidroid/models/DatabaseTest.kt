package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var testEntity: EntryEntity
    private lateinit var db: RecorderDatabase
    private lateinit var testDao: EntryDao
    private val testUid = 1
    private val testUidForInsertTest = 2


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, RecorderDatabase::class.java
        ).build()
        testDao = db.entryDao()
        testEntity = EntryEntity(testUid, "test", "test", "test")
        testDao.insert(testEntity)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testLoadEntryById() {
        val byIdEntity = testDao.loadEntryById(testUid)
        assertEquals(byIdEntity.uid, testUid)
    }

    @Test
    @Throws(Exception::class)
    fun testInsert() {
        val testInsertEntity = EntryEntity(testUidForInsertTest, "test", "test", "test")
        /** checks if the number of entries in the database increments */
        val currentNumberEntries = testDao.getAllRecordings().size
        testDao.insert(testInsertEntity)
        assertEquals(currentNumberEntries + 1, testDao.getAllRecordings().size)
        /** checks if testEntry is in the database */
        val byId = testDao.loadEntryById(testUidForInsertTest)
        assertThat(byId, equalTo(testInsertEntity))
    }

    @Test
    @Throws(Exception::class)
    fun testDelete() {
        testDao.delete(testEntity)
        val deletedEntity = testDao.loadEntryById(testUid)
        assertEquals(deletedEntity, null)
    }

    @Test
    @Throws(Exception::class)
    fun testClearTable() {
        testDao.clearTable()
        assertTrue(testDao.getAllRecordings().isEmpty())
    }
}
