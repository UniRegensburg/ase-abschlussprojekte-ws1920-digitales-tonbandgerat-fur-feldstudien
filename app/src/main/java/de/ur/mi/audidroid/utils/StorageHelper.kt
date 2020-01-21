package de.ur.mi.audidroid.utils

import android.os.Environment

/**
 * Helper object to apply and change the storage location of files based on a user-set preference.
 * Supported locations: [internal] and [external].
 *
 * @author: Lisa Sanladerer
 * Adapted from: ThemeHelper
 */

object StorageHelper {

    /** This is the users preferred saving location [internal] and [external].
     * Needs to be outsourced in order to preserve state.
     */
    var preference = "internal"


    /** Checks if an external saving location exists and can be used.
     *  This should be checked before each recording and [external] is selected.
     *  The Media_Mounted_READ_ONLY-Option could be useful if we are solely playing back. (?)
     * */
    fun checkExternalStorageExistance(): Boolean{

        var state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
           return false
        }
        return false
    }

    /**
     * Applies the location change to the storage preference.
     */

    fun applyStorage(storage_location: String){
        if (storage_location != preference){
            if (storage_location == "external") {
                if (checkExternalStorageExistance()){
                    preference = storage_location
                }
            }
        }
    }

}
