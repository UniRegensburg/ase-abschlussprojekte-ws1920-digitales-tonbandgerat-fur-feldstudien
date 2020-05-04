package de.ur.mi.audidroid.utils

import androidx.appcompat.app.AppCompatDelegate
import de.ur.mi.audidroid.R

/**
 * Helper object to get a styled color resource.
 * @author: Jonas Puchinger
 */

object ColorHelper {

    fun getThemedIconColor(): Int {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.color.color_on_surface
            AppCompatDelegate.MODE_NIGHT_YES -> R.color.color_on_secondary
            else -> R.color.color_on_surface
        }
    }
}
