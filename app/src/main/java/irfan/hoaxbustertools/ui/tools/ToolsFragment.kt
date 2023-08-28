package irfan.hoaxbustertools.ui.tools

import DatabaseHelper
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.ui.tools.FirebaseContent
import irfan.hoaxbustertools.ui.tools.ToolsAdapter

class ToolsFragment : Fragment() {

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
                        val image = contentSnapshot.child("image").getValue(String::class.java) ?: ""
                        val url = contentSnapshot.child("url").getValue(String::class.java) ?: ""
                        val isFavorite = sharedPreferences.getBoolean(name_id, false)
                        FirebaseContent(name_id, image, desc_id, url, isFavorite)
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
