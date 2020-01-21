package de.ur.mi.audidroid.views

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.ThemeHelper
import de.ur.mi.audidroid.utils.StorageHelper

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference = findPreference<ListPreference>(getString(R.string.theme_preference_key))!!
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                ThemeHelper.applyTheme(newValue as String)
                true
            }
        val storagePreference = findPreference<ListPreference>(getString(R.string.storage_preference_key))!!
        storagePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                StorageHelper.applyStorage(newValue as String)
                true
            }


    }

}
