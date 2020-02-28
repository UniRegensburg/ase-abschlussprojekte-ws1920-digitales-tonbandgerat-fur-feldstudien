package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.SystemClock
import android.text.format.DateUtils
import android.widget.Chronometer
import android.widget.FrameLayout
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * The ViewModel handles the changes to the view's data and the event logic for the user interaction referring to the RecordFragment
 * @author: Sabine Roth
 */

class RecordViewModel(private val dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var tempFile = ""
    private lateinit var timer: Chronometer
    private var currentRecordTime: String = ""
    private lateinit var frameLayout: FrameLayout
    private var recorderInitialized = false
    private val context = getApplication<Application>().applicationContext
    var isRecording = MutableLiveData<Boolean>()
    var buttonsVisible = MutableLiveData<Boolean>()
    val res: Resources = context.resources
    private val _createDialog = MutableLiveData<Boolean>()
    var errorMessage: String? = null

    init {
        isRecording.value = false
        buttonsVisible.value = false
    }

    val createDialog: MutableLiveData<Boolean>
        get() = _createDialog

    fun initializeTimer(chronometer: Chronometer) {
        timer = chronometer
    }

    fun initializeLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

    private fun getStoragePreference(): Uri{
        val preferences = context!!.getSharedPreferences(res.getString(R.string.storage_preference_key), Context.MODE_PRIVATE)
        return Uri.parse(preferences.getString(res.getString(R.string.storage_preference_key),"default"))
    }

    /**Initializing the recorder and cache the recording in the internal memory till the user decides the save location in the dialog afterwards */
    private fun initializeRecorder() {
        //val fileDescriptor = initializeTmpFile()
        tempFile = context.filesDir.absolutePath + res.getString(R.string.suffix_temp_file)
        with(mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(tempFile)
            //setOutputFile(fileDescriptor)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
        try {
            mediaRecorder.prepare()
            recorderInitialized = true
        } catch (e: IllegalStateException) {
            showSnackBar(R.string.error_message_recorder_initialization)
        } catch (e: IOException) {
            showSnackBar(R.string.error_message_recorder_file)
        }
    }

    fun recordPauseButtonClicked() {
        when (isRecording.value) {
            false -> {
                recordButtonClicked()
                buttonsVisible.value = true
                isRecording.value = true
            }
            true -> {
                pauseButtonClicked()
                isRecording.value = false
            }
        }
    }

    private fun recordButtonClicked() {
        when (resumeRecord) {
            true -> {
                timer.base = SystemClock.elapsedRealtime() - getStoppedTime()
                timer.start()
                resumeRecording()
            }
            false -> {
                resetTimer()
                timer.start()
                initializeRecorder()
                startRecording()
            }
        }
    }

    private fun pauseButtonClicked() {
        resumeRecord = true
        pauseRecording()
        timer.stop()
        currentRecordTime = timer.text.toString()
    }

    fun cancelRecord() {
        if (recorderInitialized) {
            showSnackBar(R.string.record_removed)
            File(tempFile).delete()
            endRecordSession()
            resetView()
        }
    }

    fun cancelSaving() {
        errorMessage = null
        _createDialog.value = false
        buttonsVisible.value = true
        isRecording.value = false
        resumeRecord = true
    }

    fun confirmRecord() {
        prepareForPossResume()
        _createDialog.value = true
    }

    private fun prepareForPossResume() {
        if (isRecording.value!!) {
            mediaRecorder.pause()
        }
        timer.stop()
        currentRecordTime = timer.text.toString()
    }

    private fun endRecordSession() {
        recorderInitialized = false
        mediaRecorder.stop()
        mediaRecorder.reset()
    }

    private fun resetView() {
        buttonsVisible.value = false
        isRecording.value = false
        resumeRecord = false
        resetTimer()
    }

    private fun startRecording() {
        mediaRecorder.start()
    }

    private fun pauseRecording() {
        mediaRecorder.pause()
    }

    private fun resumeRecording() {
        mediaRecorder.resume()
    }

    private fun copyToExternalFile (src: File, dst: DocumentFile){
        val inputStream = src.inputStream()
        val outputStream = context.contentResolver.openOutputStream(dst.uri)
        inputStream.copyTo(outputStream!!)
        inputStream.close()
        outputStream.close()
    }

    private fun initExternalFile(tempFile: File,name: String): String{
        val preferredDir = DocumentFile.fromTreeUri(context!!, getStoragePreference())!!
        val newExternalFile = preferredDir.createFile("aac",name)!!
        copyToExternalFile(tempFile,newExternalFile)
        return newExternalFile.uri!!.toString()
    }

    private fun checkExternalNameUniqueness(targetDir: Uri, name: String): Boolean{
        val f = DocumentFile.fromTreeUri(context!!, targetDir)!!
        val parentDirContent = f.listFiles()
        parentDirContent.forEach {
            if (it.name.equals(name)) {
                errorMessage = res.getString(R.string.dialog_already_exist)
                _createDialog.value = true
                return false
            }
        }
        return true
    }

    private fun  saveFileAtLocation(newFile: File, name: String): String{
        val storagePref  = getStoragePreference()

        if (storagePref.toString().equals("default")){
            if (newFile.exists()) {
                errorMessage = res.getString(R.string.dialog_already_exist)
                _createDialog.value = true
                return "fail"
            }
            File(tempFile).copyTo(newFile)
            return ""
        }else{
            val newName = name + ".aac"
            if(!checkExternalNameUniqueness(storagePref, newName)){
                return "fail"
            }
            val path = initExternalFile(File(tempFile),newName)
            newFile.delete()
            return path
        }
    }
    fun getNewFileFromUserInput(nameInput: String?, pathInput: String?) {
        _createDialog.value = false
        var name = nameInput ?: java.lang.String.format(
            "%s_%s",
            res.getString(R.string.standard_name_recording),
            android.text.format.DateFormat.format(
                "yyyy-MM-dd_hh-mm",
                Calendar.getInstance(Locale.getDefault())
            )
        )
        if (!validNameInput(name)) {
            errorMessage = res.getString(R.string.dialog_invalid_name)
            _createDialog.value = true
            return
        }
        var path = java.lang.String.format(
            "%s/$name%s",
            (pathInput ?: context.filesDir.absolutePath),
            res.getString(R.string.suffix_audio_file)
        )
        val newFile = File(path)


        val result = saveFileAtLocation(newFile,name)
        if (result.equals("fail")){
            return
        } else if (result.isNotEmpty()){
            path = result
        }
        println(path)
        endRecordSession()
        val recordingDuration = getRecordingDuration() ?: currentRecordTime
        val audio =
            EntryEntity(0, name, path, getDate(), recordingDuration)
        saveRecordInDB(audio)
        File(tempFile).delete()
        resetView()
        errorMessage = null
    }

    private fun saveRecordInDB(audio: EntryEntity) {
        dataSource.insert(audio)
        showSnackBar(R.string.record_saved)
    }

    private fun validNameInput(name: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_-]+$").matcher(name).matches()
    }

    private fun getRecordingDuration(): String? {
        val metaRetriever = MediaMetadataRetriever()
        //metaRetriever.setDataSource(context, tmpFile.uri)
        metaRetriever.setDataSource(tempFile)
        return DateUtils.formatElapsedTime(
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong() / (res.getInteger(
                R.integer.one_second
            ).toLong())
        )
    }

    /**
     * Returns the current date
     * Adapted from: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */

    private fun getDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    /** Sends a snackbar for user information with the given [text] */
    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

    /** Returns the last stopped time as an Integer value */
    private fun getStoppedTime(): Int {
        val timeArray = currentRecordTime.split(":")
        return if (timeArray.size == 2) {
            (Integer.parseInt(timeArray[0]) * res.getInteger(R.integer.counter_divider_minutes_hours) * res.getInteger(
                R.integer.counter_multiplier
            )) + (Integer.parseInt(timeArray[1]) * res.getInteger(R.integer.counter_multiplier))
        } else {
            (Integer.parseInt(timeArray[0]) * res.getInteger(R.integer.counter_divider_minutes_hours) * res.getInteger(
                R.integer.counter_divider_minutes_hours
            ) * res.getInteger(R.integer.counter_multiplier)) + (Integer.parseInt(timeArray[1]) * res.getInteger(
                R.integer.counter_divider_minutes_hours
            ) * res.getInteger(R.integer.counter_multiplier)) + (Integer.parseInt(
                timeArray[2]
            ) * res.getInteger(R.integer.counter_multiplier))
        }
    }

    /** Resets timer to 00:00 */
    private fun resetTimer() {
        timer.stop()
        timer.base = SystemClock.elapsedRealtime()
    }
}
