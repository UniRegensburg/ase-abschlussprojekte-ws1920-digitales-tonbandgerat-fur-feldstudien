package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardHelper {

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    fun hideSoftKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        view.clearFocus()
    }

}
