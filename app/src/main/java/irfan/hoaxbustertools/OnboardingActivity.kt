package irfan.hoaxbustertools

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)
        val onboardingCompleted = sharedPreferences.getBoolean("onboardingCompleted", false)

        val defaultLanguage = "in" // Replace with your default language code
        LocaleUtil.applyLocalizedContext(this, defaultLanguage)

        if (onboardingCompleted) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val spinner = findViewById<Spinner>(R.id.languageSpinner)
            val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            val saveButton = findViewById<Button>(R.id.saveButton)
            saveButton.setOnClickListener {
                val selectedLanguage = when (spinner.selectedItemPosition) {
                    0 -> "in"
                    1 -> "en"
                    else -> "in"
                }

                sharedPreferences.edit().putString("preferred_locale", selectedLanguage).apply()
                sharedPreferences.edit().putBoolean("onboardingCompleted", true).apply()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }
    }
}
