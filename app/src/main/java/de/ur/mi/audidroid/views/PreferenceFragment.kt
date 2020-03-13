package de.ur.mi.audidroid.views

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.Pathfinder
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
                val input = newValue.toString()
                var closeDialog = true
                if (input.length > context!!.resources.getInteger(R.integer.max_name_length)) {
                    fileNamePreference.dialogMessage =
                        resources.getString(R.string.dialog_name_length)
                    closeDialog = false
                } else if (!fileNameClean(input)) {
                    fileNamePreference.dialogMessage =
                        resources.getString(R.string.dialog_invalid_name)
                    closeDialog = false
                } else if (fileNameClean(input)) {
                    fileNamePreference.dialogMessage =
                        resources.getString(R.string.filename_preference_dialog_message)
                    closeDialog = true
                    val preferences = context!!.getSharedPreferences(
                        getString(R.string.filename_preference_key),
                        Context.MODE_PRIVATE
                    )
                    with(preferences.edit()) {
                        putString(getString(R.string.filename_preference_key), input)
                        commit()
                    }
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
        return Pattern.compile("^[a-zA-Z0-9_{}-]+$").matcher(name).matches()
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


    /*<<<<<<< HEAD
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
=======>>>>>>> master*/
    private fun initStoragePreference() {
        val storagePreference =
            findPreference<Preference>(getString(R.string.storage_preference_key))!!
        storagePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Pathfinder.openPathDialog(storagePreference, context!!)
            true
        }
        storagePreference.summary = getSummary()
    }

    fun resultPathfinder(preference: Preference, context: Context, data: Intent?, view: View) {
        val preferences = context.getSharedPreferences(
            context.resources.getString(R.string.storage_preference_key),
            Context.MODE_PRIVATE
        )
        val path = data!!.dataString!!
        val realPath =
            when (path == context.resources.getString(R.string.default_storage_location)) {
                true -> path
                false -> Pathfinder.getRealPath(context, Uri.parse(path))
            }
        if (realPath == null) {
            Snackbar.make(
                view,
                context.resources.getString(R.string.external_sd_card_error),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        preference.summary = realPath!!
        with(preferences.edit()) {
            putString(context.resources.getString(R.string.storage_preference_key), realPath)
            commit()
        }
    }

    private fun getSummary(): String {
        val preferences = context!!.getSharedPreferences(
            getString(R.string.storage_preference_key),
            Context.MODE_PRIVATE
        )
        val storedPathString = preferences.getString(
            getString(R.string.storage_preference_key),
            getString(R.string.default_storage_location)
        )!!
        with(preferences.edit()) {
            putString(getString(R.string.storage_preference_key), storedPathString)
            commit()
        }
        return storedPathString

    }
}
