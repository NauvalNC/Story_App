package com.nauval.storyapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nauval.storyapp.helper.IGeneralSetup
import com.nauval.storyapp.helper.StoryApiSession

class SettingFragment : PreferenceFragmentCompat(), IGeneralSetup {

    private val logOutDialogueListener =
        DialogInterface.OnClickListener { _, option ->
            when (option) {
                DialogInterface.BUTTON_POSITIVE -> {
                    StoryApiSession(requireContext()).clearToken()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    requireActivity().finish()
                }
                DialogInterface.BUTTON_NEGATIVE -> {}
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preference, rootKey);
        setup()
    }

    override fun setup() {
        val langPref = findPreference<Preference> (resources.getString(R.string.language)) as Preference
        val logOutPref = findPreference<Preference> (resources.getString(R.string.logout)) as Preference

        langPref.setOnPreferenceClickListener {
            changeLanguage()
            true
        }

        logOutPref.setOnPreferenceClickListener {
            logOutAccount()
            true
        }
    }

    private fun changeLanguage() = startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))

    private fun logOutAccount() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(resources.getString(R.string.logout))
            setMessage(resources.getString(R.string.are_you_sure))
                .setPositiveButton(resources.getString(R.string.yes), logOutDialogueListener)
                .setNegativeButton(resources.getString(R.string.no), logOutDialogueListener)
        }.show()
    }
}