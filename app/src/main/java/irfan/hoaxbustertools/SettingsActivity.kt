package irfan.hoaxbustertools

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import irfan.hoaxbustertools.ui.settings.CreditFragment
import java.util.Locale

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?)   {
        LocaleUtil.applyLocalizedContext(this, storage.getPreferredLocale())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_settings, SettingsFragment())
                .commit()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var storage: Storage
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            storage = Storage(requireContext())
            val languagePreference = findPreference<ListPreference>("pref_language")

            if (languagePreference != null) {
                val selectedLocale = storage.getPreferredLocale()
                languagePreference?.value = selectedLocale
                languagePreference?.summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
                    val selectedLocale = preference.value
                    val currentLocale = storage.getPreferredLocale()

                    if (selectedLocale == currentLocale) {
                        return@SummaryProvider Locale(currentLocale).displayLanguage
                    } else {
                        return@SummaryProvider Locale(selectedLocale).displayLanguage
                    }
                }

                languagePreference?.setOnPreferenceChangeListener { _, newValue ->
                    val newLocaleCode = newValue.toString()
                    storage.setPreferredLocale(newLocaleCode)
                    LocaleUtil.applyLocalizedContext(requireContext(), newLocaleCode)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
            }

            languagePreference?.setOnPreferenceChangeListener { preference, newValue ->
                val newLocaleCode = newValue.toString()
                storage.setPreferredLocale(newLocaleCode)
                LocaleUtil.applyLocalizedContext(requireContext(), newLocaleCode)

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }

            val creditPreference = findPreference<Preference>("credit_preference_key")
            creditPreference?.setOnPreferenceClickListener {
                val creditFragment = CreditFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_settings, creditFragment)
                    .addToBackStack(null)
                    .commit()
                true
            }
        }
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }
}