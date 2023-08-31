package irfan.hoaxbustertools.ui.home

import DatabaseHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), OnFavoriteStatusChangedListener {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerView: RecyclerView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val favoriteChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "favorite_changed") {
                val toolId = intent.getStringExtra("name_id")
                val isFavorite = intent.getBooleanExtra("is_favorite", false)
                if (toolId != null) {
                    handleFavoriteChange(toolId, isFavorite)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val filter = IntentFilter("favorite_changed")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(favoriteChangeReceiver, filter)


        val imageView = view.findViewById<ImageView>(R.id.imageView2)
        val textHome = view.findViewById<TextView>(R.id.text_home)
        val textHome2 = view.findViewById<TextView>(R.id.text_home2)
        val buttonCobaTool = view.findViewById<Button>(R.id.buttonCobaTool)

        val dbHelper = DatabaseHelper(requireContext())
        val favoritedTools = dbHelper.getFavoriteNameIds()


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewToolsFavorite)



        fetchToolDetailsFromFirebase(favoritedTools) { fetchedTools ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            if (fetchedTools.isEmpty()) {
                showEmptyStateView()
            } else {
                showRecyclerViewWithTools(fetchedTools, dbHelper)
            }
        }

        buttonCobaTool.setOnClickListener {
            openSidebarMenu()
        }



        return view
    }
    override fun onFavoriteStatusChanged(tool: ToolItem) {
        showEmptyStateView()
    }

    private fun handleFavoriteChange(toolId: String, isFavorite: Boolean) {
        val adapter = _binding?.recyclerViewToolsFavorite?.adapter as? FavoriteToolsAdapter

        if (isFavorite) {
            adapter?.updateFavoriteStatus(toolId, true)
        } else {
            adapter?.removeTool(toolId)
            if (adapter?.itemCount == 0) {
                showEmptyStateView()
            }
        }
    }


    private fun openSidebarMenu() {
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }

    fun onBackPressedInFragment() {
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            requireActivity().onBackPressed()
        }
    }


    private fun showEmptyStateView() {
        _binding?.imageView2?.visibility = View.VISIBLE
        _binding?.textHome?.text = getString(R.string.empty_favorites_text)
        _binding?.textHome2?.text = getString(R.string.add_favorites_text)
        _binding?.textHome?.visibility = View.VISIBLE
        _binding?.textHome2?.visibility = View.VISIBLE
        _binding?.buttonCobaTool?.visibility = View.VISIBLE
        _binding?.recyclerViewToolsFavorite?.visibility = View.GONE
    }

    private fun showRecyclerViewWithTools(fetchedTools: MutableList<ToolItem>, dbHelper: DatabaseHelper) {
        _binding?.imageView2?.visibility = View.GONE
        _binding?.textHome?.visibility = View.GONE
        _binding?.textHome2?.visibility = View.GONE
        _binding?.buttonCobaTool?.visibility = View.GONE

        // Update UI with fetchedTools
        val adapter = FavoriteToolsAdapter(fetchedTools, dbHelper, this)

        _binding?.recyclerViewToolsFavorite?.adapter = adapter
        _binding?.recyclerViewToolsFavorite?.visibility = View.VISIBLE
    }


    private fun fetchToolDetailsFromFirebase(nameIds: List<String>, callback: (MutableList<ToolItem>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("menus")
        val toolDetailsList = mutableListOf<ToolItem>()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (menuSnapshot in dataSnapshot.children) {
                    val contentsSnapshot = menuSnapshot.child("contents")

                    for (contentSnapshot in contentsSnapshot.children) {
                        val nameId = contentSnapshot.child("name_id").getValue(String::class.java)
                        if (nameId in nameIds) {
                            val descId = contentSnapshot.child("desc_id").getValue(String::class.java)
                            val image = contentSnapshot.child("image").getValue(String::class.java)
                            val url = contentSnapshot.child("url").getValue(String::class.java)

                            if (descId != null && image != null && url != null) {
                                val toolItem = ToolItem(nameId!!, descId, image, url)
                                toolDetailsList.add(toolItem)
                            }
                        }
                    }
                }

                // Notify your adapter here, if needed

                // Call the callback with the populated list
                callback(toolDetailsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }








    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}