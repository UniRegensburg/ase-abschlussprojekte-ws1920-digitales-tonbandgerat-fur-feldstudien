package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jraska.livedata.test
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import org.junit.Rule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule



/**
* Unit tests for the room database using jraska/livedata-testing for testing the LiveData return
* @author: Sabine Roth
*/


@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var testEntity: EntryEntity
    private lateinit var testDatabase: RecorderDatabase
    private lateinit var testDao: EntryDao
    private val testUid = 1
    private val testUidForInsertTest = 2

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()



    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDatabase = Room.inMemoryDatabaseBuilder(
            context, RecorderDatabase::class.java
        ).allowMainThreadQueries().build()
        testDao = testDatabase.entryDao()
        testEntity = EntryEntity(testUid, "test", "test", "test")
        testDao.insert(testEntity)
    }

    @Test
    @Throws(Exception::class)
    fun testLoadEntryById() {
        val byIdLiveDataEntity = testDao.getRecordingWithId(testUid)
        byIdLiveDataEntity.test()
            .awaitValue()
            .assertHasValue()
            .assertValue(testEntity)
    }

    @Test
    @Throws(Exception::class)
    fun testInsert() {
        val testInsertEntity = EntryEntity(testUidForInsertTest, "test", "test", "test")

        /** checks if the number of entries in the database increments */
        val currentList = ArrayList<EntryEntity>()
        val allRecordingsLiveData = testDao.getAllRecordings()
        allRecordingsLiveData.test()
            .awaitValue()
            .map { currentList }
        testDao.insert(testInsertEntity)
        val allRecordingsLiveDataNew = testDao.getAllRecordings()
        allRecordingsLiveDataNew.test()
            .awaitValue()
            .assertHistorySize(currentList.size + 1)

        /** checks if testEntry is in the database */
        val byIdLiveDataEntity = testDao.getRecordingWithId(testUidForInsertTest)
        byIdLiveDataEntity.test()
            .awaitValue()
            .assertHasValue()
            .assertValue(testInsertEntity)
    }

    @Test
    @Throws(Exception::class)
    fun testDelete() {
        testDao.delete(testEntity)
        val byIdLiveDataEntity = testDao.getRecordingWithId(testUidForInsertTest)
        byIdLiveDataEntity.test()
            .awaitValue()
            .assertHasValue()
            .assertValue(null)
    }

    @Test
    @Throws(Exception::class)
    fun testClearTable() {
        testDao.clearTable()
        val currentList = ArrayList<EntryEntity>()
        val allRecordingsLiveDataNew = testDao.getAllRecordings()
        allRecordingsLiveDataNew.test()
            .awaitValue()
            .map { currentList }
        assertTrue(currentList.isEmpty())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        testDatabase.close()
    }
}
