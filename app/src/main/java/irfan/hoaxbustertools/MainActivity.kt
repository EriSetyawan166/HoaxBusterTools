package irfan.hoaxbustertools

import android.content.Intent
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

data class FirebaseMenu @JvmOverloads constructor(
    val name: String = "",
    val icon: String = ""
)


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val headerView = navView.getHeaderView(0)
        val btnHome: Button = headerView.findViewById(R.id.btnHome)
        val btnAbout: Button = headerView.findViewById(R.id.btnAbout)

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



    private fun fetchMenusFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("menus")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FirebaseFetch", "Data received from Firebase: $dataSnapshot")
                val menus = dataSnapshot.children.mapNotNull { it.getValue(FirebaseMenu::class.java) }
                Log.d("FirebaseFetch", "Processed menus: $menus")
                updateNavigationMenuItems(menus)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Gagal Mengambil Data"
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()

            }
        })
    }


    private fun updateNavigationMenuItems(menus: List<FirebaseMenu>) {
        val navView: NavigationView = binding.navView
        val menu = navView.menu
        menu.clear()

        menus.forEachIndexed { index, fetchedMenu ->
            val menuItem = menu.add(Menu.NONE, index, Menu.NONE, fetchedMenu.name)

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
                supportActionBar?.title = fetchedMenu.name
                true
            }

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
