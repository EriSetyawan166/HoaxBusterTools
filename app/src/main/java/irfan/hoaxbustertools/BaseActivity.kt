package irfan.hoaxbustertools

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

open class BaseActivity: AppCompatActivity() {
    private lateinit var oldPrefLocaleCode : String
    protected val storage : Storage by lazy {
        (application as MyApp).storage
    }

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
        if(oldPrefLocaleCode != currentLocaleCode){
            recreate()
            oldPrefLocaleCode = currentLocaleCode
        }
        super.onResume()

    }
}
