package de.ur.mi.audidroid.utils

import android.view.View

object VisibilityHelper {

    fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    fun toggleExpanded(view: View, isExpanded: Boolean) {
        if (isExpanded) collapse(view, isExpanded) else expand(view, isExpanded)
    }

    private fun collapse(view: View, isExpanded: Boolean) {
        if (!isExpanded) return
        setHeightToZero(view)
    }

    private fun expand(view: View, isExpanded: Boolean) {
        if (isExpanded) return
        setHeightToContentHeight(view)
    }

    private fun setHeightToZero(view: View) {
        setContentHeight(view,0)
    }

    private fun setHeightToContentHeight(view: View) {
        val targetHeight = view.measuredHeight
        setContentHeight(view, targetHeight)
    }

    private fun setContentHeight(view: View, height: Int) {
        view.layoutParams.height = height
        view.requestLayout()
    }
}
