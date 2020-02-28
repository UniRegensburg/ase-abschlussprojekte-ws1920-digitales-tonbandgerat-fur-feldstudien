package de.ur.mi.audidroid.views

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class PreferenceDialogFragment: PreferenceDialogFragmentCompat() {

    lateinit var positiveResult: () -> Any

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            positiveResult()
        }
    }

    companion object {
        fun newInstance(key: String): PreferenceDialogFragment {
            val fragment = PreferenceDialogFragment()
            val bundle = Bundle(1)
            bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }

}
