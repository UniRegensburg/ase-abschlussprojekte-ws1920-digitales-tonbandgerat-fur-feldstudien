package de.ur.mi.audidroid.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import de.ur.mi.audidroid.R

/**
 * Listener for rotation changes of the device
 * @author: Sabine Roth
 */

object OrientationListener {

    private var orientationEventListener: OrientationEventListener? = null

    @SuppressLint("SourceLockedOrientationActivity")
    fun adjustRotationListener(context: Context) {
        val activity = context as Activity
        if (getRotationPreference(context)) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            if (orientationEventListener == null) {
                orientationEventListener = object : OrientationEventListener(context) {

                    override fun onOrientationChanged(orientation: Int) {
                        if (orientation in 70..290) activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        else if (orientation in 291..360 || (orientation in 0..69)) activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
            orientationEventListener?.enable()
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            orientationEventListener?.disable()
        }
    }

    private fun getRotationPreference(context: Context): Boolean {
        return context.getSharedPreferences(
            context.resources.getString(R.string.rotate_preference_key),
            Context.MODE_PRIVATE
        ).getBoolean(context.resources.getString(R.string.rotate_preference_key), true)
    }
}
