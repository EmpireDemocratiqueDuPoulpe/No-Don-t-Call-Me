package com.eddp.nodontcallme

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    private var _emergencyCallLimit: EditTextPreference? = null

    // TODO: Preference are unused for now.
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.shared_pref_filename)
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Get and init preferences
        this._emergencyCallLimit = preferenceManager.findPreference("emergency_calls")

        if (this._emergencyCallLimit != null) {
            this._emergencyCallLimit!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            }
        }
    }

    // TODO : Probably useless. Check later. See https://developer.android.com/reference/androidx/preference/Preference#setPreferenceDataStore(androidx.preference.PreferenceDataStore)
    //private fun initSettingsValue() {
    //    val emergencyCallEnabledValue: Boolean = preference?.getBoolean(PREF_EMERGENCY_CALL_ENABLED, true) ?: true
    //    val emergencyCallLimitValue: Int = preference?.getInt(PREF_EMERGENCY_CALL_LIMIT, 1) ?: 1
    //    val autoMessageEnabledValue: Boolean = preference?.getBoolean(PREF_AUTO_MESSAGE_ENABLED, true) ?: true
    //    val autoMessageValue: String = preference?.getString(PREF_AUTO_MESSAGE, getString(R.string.auto_message_default_value)) ?: getString(R.string.auto_message_default_value)
//
    //    emergencyCallEnabler?.isChecked = emergencyCallEnabledValue
    //    emergencyCallLimit?.text = emergencyCallLimitValue.toString()
    //    autoMessageEnable?.isChecked = autoMessageEnabledValue
    //    autoMessage?.text = autoMessageValue
    //}

    //companion object {
    //    const val PREF_EMERGENCY_CALL_ENABLED = "emergency_call_enabled"
    //    const val PREF_EMERGENCY_CALL_LIMIT = "emergency_call_limit"
    //    const val PREF_AUTO_MESSAGE_ENABLED = "auto_message_enabled"
    //    const val PREF_AUTO_MESSAGE = "auto_message"
    //}
}