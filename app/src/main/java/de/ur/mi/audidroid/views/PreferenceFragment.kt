package de.ur.mi.audidroid.views

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.StorageHelper
import de.ur.mi.audidroid.utils.ThemeHelper
import java.util.regex.Pattern

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initLabelsPreference()
        initFileNamePreference()
        initThemePreference()
        initStoragePreference()

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

    private fun initStoragePreference(){
        val storagePreference = findPreference<Preference>(getString(R.string.storage_preference_key))!!
        storagePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivityForResult(StorageHelper.setOpenDocumentTreeIntent(), resources.getInteger(R.integer.activity_request_code_preference_storage))
            true
        }
    }
    private fun setStoragePreferenceSummary(uri: Uri){
        val storagePreference = findPreference<Preference>(getString(R.string.storage_preference_key))
        val summary = uri.pathSegments.last().split(":")[1]
        storagePreference!!.setSummary(summary)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resources.getInteger(R.integer.activity_request_code_preference_storage) &&
                resultCode == Activity.RESULT_OK){
            val pref = context!!.getSharedPreferences(getString(R.string.storage_preference_key), Context.MODE_PRIVATE)
            with (pref.edit()){
                putString(getString(R.string.storage_preference_key), data!!.dataString)
                commit()
            }
            setStoragePreferenceSummary(data!!.data!!)
        }
    }
}
