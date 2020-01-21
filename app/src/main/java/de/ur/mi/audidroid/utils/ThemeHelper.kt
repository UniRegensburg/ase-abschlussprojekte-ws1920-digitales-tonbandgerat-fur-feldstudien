package de.ur.mi.audidroid.utils

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

/**
 * Helper object to apply and change the app theme based on a user-set preference.
 * Supported themes: [LIGHT_MODE] and [DARK_MODE].
 *
 * @author: Jonas Puchinger
 * Adapted from: https://github.com/YarikSOffice/Dark-Theme-Playground/blob/master/app/src/main/java/example/com/darkthemeplayground/settings/ThemeHelper.kt
 */

object ThemeHelper {

    private const val LIGHT_MODE = "light"
    private const val DARK_MODE = "dark"

    /**
     * Applies the color theme given in [theme] to the app.
     */
    fun applyTheme(theme: String) {

        val mode = when (theme) {
            LIGHT_MODE -> AppCompatDelegate.MODE_NIGHT_NO
            DARK_MODE -> AppCompatDelegate.MODE_NIGHT_YES
            else -> {
                when {
                    isAtLeastAndroidVersionP() -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    isAtLeastAndroidVersionL() -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
            }
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun isAtLeastAndroidVersionP() = Build.VERSION.SDK_INT >= 28
    private fun isAtLeastAndroidVersionL() = Build.VERSION.SDK_INT >= 23
}
