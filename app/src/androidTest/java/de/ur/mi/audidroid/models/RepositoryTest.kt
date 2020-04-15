package de.ur.mi.audidroid.models

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jraska.livedata.test
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
* Unit tests for the room database using jraska/livedata-testing for testing the LiveData return
* @author: Sabine Roth
*/


@RunWith(AndroidJUnit4::class)
class RepositoryTest {


    private lateinit var testRecording: RecordingEntity
    private lateinit var testDatabase: RecorderDatabase
    private val testUid = 1
    private lateinit var repository: Repository
    private lateinit var testLabel: LabelEntity
    private lateinit var testMarker: MarkerEntity
    private val testLabelName = "testLabelName"
    private val testMarkerName = "testMarkerName"
    private lateinit var testLabelAssignmentEntity: LabelAssignmentEntity
    private lateinit var testMarkTimestamp: MarkTimestamp

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDatabase = Room.inMemoryDatabaseBuilder(
            context, RecorderDatabase::class.java
        ).allowMainThreadQueries().build()
        testRecording = RecordingEntity(testUid, "test1", "test1", "test1", "test1")
        testLabel = LabelEntity(testUid, testLabelName)
        testMarker = MarkerEntity(testUid, testMarkerName)
        testLabelAssignmentEntity = LabelAssignmentEntity(testUid, testRecording.uid, testLabel.uid)
        testMarkTimestamp = MarkTimestamp(testUid, testRecording.uid, testMarker.uid, markTime = "test")
        repository = Repository(context as Application)
        repository.insertRecording(testRecording)
        repository.insertLabel(testLabel)
        repository.insertMarker(testMarker)
        repository.insertRecLabels(testLabelAssignmentEntity)
        repository.insertMarkTimestamp(testMarkTimestamp)
    }


    /** Recordings */

    @Test
    @Throws(Exception::class)
    fun testGetRecordingById() {
        val byIdLiveDataEntity = repository.getRecordingById(testUid)
        byIdLiveDataEntity.test()
            .awaitValue()
            .assertHasValue()
            .assertValue(testRecording)
    }

    @Test
    @Throws(Exception::class)
    fun testInsertRecording() {
        val testUidForInsertTest = 2
        val testInsertEntity = RecordingEntity(testUidForInsertTest, "test2", "test2", "test2", "test2")

        /** checks if the number of entries in the database increments */
        val currentList = ArrayList<RecordingEntity>()
        val allRecordingsLiveData = repository.getAllRecordings()
        allRecordingsLiveData.test()
            .awaitValue()
            .map { currentList }
        repository.insertRecording(testInsertEntity)
        val allRecordingsLiveDataNew = repository.getAllRecordings()
        allRecordingsLiveDataNew.test()
            .awaitValue()
            .assertHistorySize(currentList.size + 1)

        /** checks if testEntry is in the database */
        val byIdLiveDataEntity = repository.getRecordingById(testUidForInsertTest)
        byIdLiveDataEntity.test()
            .awaitValue()
            .assertHasValue()
            .assertValue(testInsertEntity)
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteRecording() {
        repository.deleteRecording(testUid)
        val byIdLiveDataEntity = repository.getRecordingById(testUid)
        byIdLiveDataEntity.test()
            .awaitValue()
            .assertHasValue()
            .assertValue(null)
    }


    /** Labels */

    @Test
    @Throws(Exception::class)
    fun testInsertLabel() {
        val nameForInsertLabelTest = "testInsertLabel"
        val currentList = ArrayList<RecordingEntity>()
        val allLabelsLiveData = repository.getAllLabels()
        allLabelsLiveData.test()
            .awaitValue()
            .map { currentList }
        repository.insertLabel(LabelEntity(2, nameForInsertLabelTest))
        val allLabelsLiveDataNew = repository.getAllLabels()
        allLabelsLiveDataNew.test()
            .awaitValue()
            .assertHistorySize(currentList.size + 1)
        val byNameLiveDataLabel = repository.getLabelByName(nameForInsertLabelTest)
        assertTrue(byNameLiveDataLabel.isNotEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateLabel() {
        val updatedLabelName = "updatedLabelName"
        val updatedLabel = LabelEntity(testLabel.uid, updatedLabelName)
        repository.updateLabel(updatedLabel)
        val byNameLiveDataUpdatedLabel = repository.getLabelByName(updatedLabelName)
        assertTrue(byNameLiveDataUpdatedLabel.isNotEmpty())
    }


    @Test
    @Throws(Exception::class)
    fun testDeleteLabel() {
        repository.deleteLabel(testLabel)
        val byNameLiveDataLabel = repository.getLabelByName(testLabelName)
        assertTrue(byNameLiveDataLabel.isEmpty())
    }


    /** Recordinglabels */

    @Test
    @Throws(Exception::class)
    fun testGetRecLabel() {
        val byIdLiveDataRecLabels = repository.getRecLabelsById(testLabelAssignmentEntity.recordingId)
        byIdLiveDataRecLabels.test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    @Throws(Exception::class)
    fun testInsertRecLabel() {
        val testInsertLabelAssignmentEntity = LabelAssignmentEntity(2, testRecording.uid, testLabel.uid)
        repository.insertRecLabels(testInsertLabelAssignmentEntity)
        val byIdLiveDataRecLabels = repository.getRecLabelsById(testLabelAssignmentEntity.recordingId)
        byIdLiveDataRecLabels.test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteRecLabel() {
        repository.deleteRecLabels(testLabelAssignmentEntity.primaryKey)
        val byIdLiveDataRecLabels = repository.getRecLabelsById(testLabelAssignmentEntity.recordingId)
        assertNull(byIdLiveDataRecLabels.value)
    }


    /** Markers */

    @Test
    @Throws(Exception::class)
    fun testInsertMarker() {
        val nameForInsertMarkerTest = "testInsertMarker"
        val currentList = ArrayList<RecordingEntity>()
        val allMarkersLiveData = repository.getAllMarkers()
        allMarkersLiveData.test()
            .awaitValue()
            .map { currentList }
        repository.insertMarker(MarkerEntity(2, nameForInsertMarkerTest))
        val allMarkersLiveDataNew = repository.getAllMarkers()
        allMarkersLiveDataNew.test()
            .awaitValue()
            .assertHistorySize(currentList.size + 1)
        val byNameLiveDataMarker = repository.getMarkerByName(nameForInsertMarkerTest)
        assertTrue(byNameLiveDataMarker.isNotEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMarker() {
        val updatedMarkerName = "updatedMarkerName"
        val updatedMarker = MarkerEntity(testMarker.uid, updatedMarkerName)
        repository.updateMarker(updatedMarker)
        val byNameLiveDataUpdatedMarker = repository.getMarkerByName(updatedMarkerName)
        assertTrue(byNameLiveDataUpdatedMarker.isNotEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteMarker() {
        repository.deleteMarker(testMarker)
        val byNameLiveDataMarker = repository.getMarkerByName(testMarkerName)
        assertTrue(byNameLiveDataMarker.isEmpty())
    }


    /** MarkTimestamp */

    @Test
    @Throws(Exception::class)
    fun testInsertMarkTimestamp(){
        val testInsertMarkTimestamp = MarkTimestamp(2, testRecording.uid + 1, testMarker.uid, markTime = "test")
        repository.insertMarkTimestamp(testInsertMarkTimestamp)
        val byIdLiveDataMarks = repository.getAllMarks(testMarkTimestamp.recordingId + 1)
        byIdLiveDataMarks.test()
            .awaitValue()
            .assertHasValue()
    }

    fun testDeleteMarkTimestamp(){
        repository.deleteMarkTimestamp(testMarkTimestamp.mid)
        val byIdLiveDataMarks = repository.getAllMarks(testMarkTimestamp.recordingId)
        assertNull(byIdLiveDataMarks)
    }


    @After
    @Throws(IOException::class)
    fun closeDb() {
        testDatabase.close()
    }
}
