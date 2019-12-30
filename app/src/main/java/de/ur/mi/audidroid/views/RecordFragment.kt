package de.ur.mi.audidroid.views

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.ur.mi.audidroid.viewmodels.RecordViewModel
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecorderDatabase
import de.ur.mi.audidroid.models.RecorderEntity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlinx.android.synthetic.main.record_fragment.*

class RecordFragment : Fragment() {

    companion object {
        fun newInstance() = RecordFragment()
    }

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.record_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)

        insertRecordingForTesting()
    }

    fun insertRecordingForTesting(){

        val audio1 =
            RecorderEntity(2, "path2", "date2")

        val db = RecorderDatabase.getInstance(context)

        doAsync{
            val result =   db.entryDao().clearTable()
            uiThread{
                result.toString()
            }
        }


        doAsync{
            val result =   db.entryDao().insert(audio1)
            uiThread{
                result.toString()
            }
        }

        doAsync{
            val result = db.entryDao().getAllRecordings()
            uiThread{
                result.forEach {
                    println(it)
                }
            }
        }

        viewModel.initializeRecorder(context!!)

        record_pause_button.setOnClickListener{ viewModel.recordButtonClicked(record_pause_button)}
        confirm_button.setOnClickListener{viewModel.stopRecording()}
    }
}
