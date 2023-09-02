package irfan.hoaxbustertools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
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
        onBackPressed() // Navigate back when back button is pressed
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var storage: Storage
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            storage = Storage(requireContext())
            val languagePreference = findPreference<ListPreference>("pref_language")

            languagePreference?.summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
                val selectedLocale = preference.value
                val currentLocale = storage.getPreferredLocale()
                Log.d("LanguagePreference", "Selected Locale: $selectedLocale")
                Log.d("LanguagePreference", "Current Locale: $currentLocale")

                if (selectedLocale == currentLocale) {
                    // Display the current language/locale
                    return@SummaryProvider Locale(currentLocale).displayLanguage
                } else {
                    // Display the selected language/locale
                    return@SummaryProvider Locale(selectedLocale).displayLanguage
                }
            }

            languagePreference?.setOnPreferenceChangeListener { preference, newValue ->
                val newLocaleCode = newValue.toString()
                storage.setPreferredLocale(newLocaleCode)
                LocaleUtil.applyLocalizedContext(requireContext(), newLocaleCode)
                Log.d("LanguagePreference", "Changing into: $newLocaleCode")

                // Clear all activities on the stack and launch the main activity
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }



            // Find the preference for "Kredit"
            val creditPreference = findPreference<Preference>("credit_preference_key")
            creditPreference?.setOnPreferenceClickListener {
                // Handle when "Kredit" preference is clicked
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