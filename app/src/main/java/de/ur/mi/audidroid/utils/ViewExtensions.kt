package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Extension functions for class [View].
 * @author: Jonas Puchinger
 * Adapted from: https://github.com/adrielcafe/AndroidAudioConverter
 */

fun View.showKeyboard() {
    this.post {
        kotlin.run {
            this.requestFocus()
            val inputMethodManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }
    }
}

fun View.hideKeyboard() {
    this.post {
        kotlin.run {
            val inputMethodManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}
