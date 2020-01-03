package de.ur.mi.audidroid.viewmodels

import androidx.lifecycle.ViewModel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class FilesViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    //This will may help you:
      /*  private fun getAllEntries(){
            doAsync{
                val result = db.entryDao().getAllRecordings()
                uiThread{
                    result.forEach {
                        println(it)
                    }
                }
            }
        }*/
}
