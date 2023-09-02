package irfan.hoaxbustertools.ui.tools

import DatabaseHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.ui.tools.FirebaseContent
import irfan.hoaxbustertools.ui.tools.ToolsAdapter

class ToolsFragment : Fragment(), ToolsAdapter.FavoriteChangeListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolsAdapter: ToolsAdapter
    private lateinit var menuName: String
    private val toolsList = mutableListOf<FirebaseContent>() // Initialize an empty list

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = arguments
        menuName = args?.getString("menuName", "") ?: ""

        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewTools)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        sharedPreferences = requireContext().getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)
        dbHelper = DatabaseHelper(requireContext())

        toolsAdapter = ToolsAdapter(requireContext(), toolsList)
        toolsAdapter.setSharedPreferences(sharedPreferences)
        toolsAdapter.setDatabaseHelper(dbHelper)
        recyclerView.adapter = toolsAdapter

        fetchContentFromFirebase()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsAdapter.setFavoriteChangeListener(this)

        val intentFilter = IntentFilter("favorite_changed")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            favoriteChangeReceiver, intentFilter
        )
    }

    private val favoriteChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "favorite_changed") {
                val nameId = intent.getStringExtra("name_id")
                val isFavorite = intent.getBooleanExtra("is_favorite", false)

                // Update the data in your toolsList based on nameId and isFavorite
                // Notify the adapter that the data has changed
                toolsAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister the BroadcastReceiver when the fragment is destroyed
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            favoriteChangeReceiver
        )
    }

    override fun onFavoriteChanged(nameId: String, isFavorite: Boolean) {
        // Update the data in your toolsList based on nameId and isFavorite
        // Notify the adapter that the data has changed
        toolsAdapter.notifyDataSetChanged()
    }



    private fun fetchContentFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("menus")
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
                        val is_search = contentSnapshot.child("is_search").getValue(Boolean::class.java) ?: false
                        val using_browser_default = contentSnapshot.child("using_browser_default").getValue(Boolean::class.java) ?: false
                        val isFavorite = sharedPreferences.getBoolean(name_id, false)
                        FirebaseContent(name_id, image, desc_id, url,name_eng, desc_eng,is_search, using_browser_default, isFavorite)
                    }
                    toolsList.clear()
                    toolsList.addAll(contents)
                    toolsAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
