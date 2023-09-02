package irfan.hoaxbustertools

import DatabaseHelper
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.database.*
import irfan.hoaxbustertools.databinding.ActivityMainBinding
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import irfan.hoaxbustertools.ui.home.HomeFragment
import irfan.hoaxbustertools.ui.tools.FirebaseContent
import java.util.Locale

data class FirebaseMenu @JvmOverloads constructor(
    val name: String = "",
    val name_id: String = "",
    val name_eng: String = "",
    val icon: String = ""
)


class MainActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var currentLanguage: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val headerView = navView.getHeaderView(0)
        val btnHome: Button = headerView.findViewById(R.id.btnHome)
        val btnAbout: Button = headerView.findViewById(R.id.btnAbout)

        val currentLocale = LocaleUtil.getLocaleFromPrefCode(storage.getPreferredLocale())
        currentLanguage = currentLocale.toString()
        Log.d("LocaleCode", "language in activity: $currentLanguage")

        btnHome.setOnClickListener {
            navController.navigate(R.id.nav_home)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnAbout.setOnClickListener {
            navController.navigate(R.id.about)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.about, R.id.tools),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        fetchMenusFromFirebase()

        Log.d("AppBarConfig", appBarConfiguration.topLevelDestinations.toString())

        binding.appBarMain.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    // Launch your Intent for the SettingsActivity
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                    true
                }
                else -> false
            }
        }



    }



    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? HomeFragment
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (fragment != null && fragment.isVisible) {
            fragment.onBackPressedInFragment()
        } else {
            super.onBackPressed()
        }
    }



    private fun fetchMenusFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("menus")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FirebaseFetch", "Data received from Firebase: $dataSnapshot")
                val menus = dataSnapshot.children.mapNotNull { it.getValue(FirebaseMenu::class.java) }
                Log.d("FirebaseFetch", "Processed menus: $menus")
                updateNavigationMenuItems(menus)
                menus.forEach { menu ->
                    val menuName = menu.name
                    val menuNameId = menu.name_id
                    val menuNameEng = menu.name_eng
                    fetchContentFromFirebase(menuName)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Gagal Mengambil Data"
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()

            }
        })
    }

    private fun fetchContentFromFirebase(menuName: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("menus")
        val db = DatabaseHelper(this)
        val isFavoritesTableEmpty = db.isFavoritesTableEmpty()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val menuSnapshot = dataSnapshot.children.find { it.child("name").getValue(String::class.java) == menuName }
                menuSnapshot?.let {
                    val contentsSnapshot = it.child("contents")
                    val contents = contentsSnapshot.children.mapNotNull { contentSnapshot ->
                        val desc_id = contentSnapshot.child("desc_id").getValue(String::class.java) ?: ""
                        val name_id = contentSnapshot.child("name_id").getValue(String::class.java) ?: ""
                        val desc_eng = contentSnapshot.child("desc_eng").getValue(String::class.java) ?: ""
                        val name_eng = contentSnapshot.child("name_eng").getValue(String::class.java) ?: ""
                        val image = contentSnapshot.child("image").getValue(String::class.java) ?: ""
                        val url = contentSnapshot.child("url").getValue(String::class.java) ?: ""
                        Log.d("FirebaseData", "desc_id: $desc_id, name_id: $name_id, image: $image, url: $url")
                        FirebaseContent(name_id, image, desc_id, url, name_eng, desc_eng)
                    }


                    if (isFavoritesTableEmpty) {
                        Log.d("DatabaseInit", "Initializing favorites content")
                        initializeDatabase(contents)
                    } else {
                        Log.d("DatabaseInit", "Favorites content already initialized")
                    }



                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }




    private fun updateNavigationMenuItems(menus: List<FirebaseMenu>) {
        val navView: NavigationView = binding.navView
        val menu = navView.menu
        menu.clear()

        menus.forEachIndexed { index, fetchedMenu ->
            val menuName = when(currentLanguage) {
                "in" -> fetchedMenu.name_id
                "en" -> fetchedMenu.name_eng
                else -> fetchedMenu.name_eng // Default to English
            }
            val menuItem = menu.add(Menu.NONE, index, Menu.NONE, menuName)

            Glide.with(this)
                .load(fetchedMenu.icon)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        val bitmap = (resource as BitmapDrawable).bitmap
                        menuItem.icon = BitmapDrawable(resources, bitmap)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // You can set a placeholder icon here
                    }
                })

            menuItem.setOnMenuItemClickListener {
                val bundle = Bundle().apply {
                    putString("menuName", fetchedMenu.name)
                }
                navController.navigate(R.id.tools, bundle)
                binding.drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer
                supportActionBar?.title = menuName
                true
            }

        }
    }

    private fun initializeDatabase(contents: List<FirebaseContent>) {
        val db = DatabaseHelper(this)
        for (content in contents) {
            db.insert(content.name_id)
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
