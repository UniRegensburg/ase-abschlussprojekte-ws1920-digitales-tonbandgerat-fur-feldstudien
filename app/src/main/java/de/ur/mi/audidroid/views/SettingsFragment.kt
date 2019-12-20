package de.ur.mi.audidroid.views

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

import de.ur.mi.audidroid.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}