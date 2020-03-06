package de.ur.mi.audidroid.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.ThemeHelper
import java.util.regex.Pattern

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initLabelsPreference()
        initMarkersPreference()
        initFileNamePreference()
        initThemePreference()
    }

    private fun initLabelsPreference() {
        val labelsPreference =
            findPreference<Preference>(getString(R.string.labels_preference_key))!!
        labelsPreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                view!!.findNavController().navigate(R.id.action_global_editLabelsFragment)
                true
            }
    }

    private fun initMarkersPreference() {
        val markersPreference =
            findPreference<Preference>(getString(R.string.markers_preference_key))!!
        markersPreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                view!!.findNavController().navigate(R.id.action_global_editMarkersFragment)
                true
            }
    }

    private fun initFileNamePreference() {
        val fileNamePreference =
            findPreference<EditTextPreference>(getString(R.string.filename_preference_key))!!
        fileNamePreference.text = fileNamePreference.text
        fileNamePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                var closeDialog = true
                if (!fileNameClean(newValue.toString())) {
                    fileNamePreference.dialogMessage =
                        resources.getString(R.string.no_special_chars_allowed)
                    closeDialog = false
                } else if (fileNameClean(newValue.toString())) {
                    fileNamePreference.dialogMessage =
                        resources.getString(R.string.filename_preference_dialog_message)
                    closeDialog = true
                }
                if (!closeDialog) {
                    Snackbar.make(
                        view!!,
                        resources.getString(R.string.filename_not_saved),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                closeDialog
            }
    }

    private fun fileNameClean(name: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_{}]+$").matcher(name).matches()
    }

    private fun initThemePreference() {
        val themePreference =
            findPreference<ListPreference>(getString(R.string.theme_preference_key))!!
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                ThemeHelper.applyTheme(newValue as String)
                true
            }
    }

}
