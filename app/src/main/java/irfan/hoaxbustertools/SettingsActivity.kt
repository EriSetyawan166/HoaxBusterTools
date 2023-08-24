package irfan.hoaxbustertools

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import irfan.hoaxbustertools.ui.settings.CreditFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.title = "Pengaturan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back when back button is pressed
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

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