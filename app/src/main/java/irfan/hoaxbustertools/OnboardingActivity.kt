package irfan.hoaxbustertools

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Menggunakan SharedPreferences untuk menyimpan status onboarding
        val sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)
        val onboardingCompleted = sharedPreferences.getBoolean("onboardingCompleted", false)

        val defaultLanguage = "in" // Replace with your default language code
        LocaleUtil.applyLocalizedContext(this, defaultLanguage)

        if (onboardingCompleted) {
            // Jika sudah selesai, arahkan ke Activity utama
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Jika belum selesai, tampilkan tampilan onboarding
            // ...

            // Inisialisasi Spinner dan adapter untuk bahasa
            val spinner = findViewById<Spinner>(R.id.languageSpinner)
            val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Tombol Simpan
            // Tombol Simpan
            val saveButton = findViewById<Button>(R.id.saveButton)
            saveButton.setOnClickListener {
                // Mengambil bahasa terpilih dari Spinner
                val selectedLanguage = when (spinner.selectedItemPosition) {
                    0 -> "in" // Bahasa Indonesia
                    1 -> "en" // Bahasa Inggris
                    else -> "in" // Default ke Bahasa Indonesia
                }

                // Log untuk mengecek bahasa yang terpilih
                Log.d("language", "Selected Language in onboarding: $selectedLanguage")

                // Simpan bahasa terpilih ke SharedPreferences
                sharedPreferences.edit().putString("preferred_locale", selectedLanguage).apply()

                // Tandai onboarding telah selesai
                sharedPreferences.edit().putBoolean("onboardingCompleted", true).apply()

                // Beralih ke Activity utama
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }
    }
}
