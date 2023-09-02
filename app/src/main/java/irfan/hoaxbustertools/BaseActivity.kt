package irfan.hoaxbustertools

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.Locale

open class BaseActivity: AppCompatActivity() { //You can use your preferred activity instead of AppCompatActivity
    private lateinit var oldPrefLocaleCode : String
    protected val storage : Storage by lazy {
        (application as MyApp).storage
    }

    /**
     * updates the toolbar text locale if it set from the android:label property of Manifest
     */
    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (e: PackageManager.NameNotFoundException) {}
    }

    override fun attachBaseContext(newBase: Context) {
        oldPrefLocaleCode = Storage(newBase).getPreferredLocale()
        val context = LocaleUtil.applyLocalizedContext(newBase, oldPrefLocaleCode)
        super.attachBaseContext(context)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetTitle()
    }

    override fun onResume() {
        val currentLocaleCode = Storage(this).getPreferredLocale()
        Log.d("LocaleCode", "current: $currentLocaleCode")
        Log.d("LocaleCode", "old: $oldPrefLocaleCode")
        if(oldPrefLocaleCode != currentLocaleCode){
            Log.d("LocaleCode", "Language Changed!!!!!! recreate also!!!!!")
            recreate() //locale is changed, restart the activty to update
            oldPrefLocaleCode = currentLocaleCode
        }
        super.onResume()

    }
}
