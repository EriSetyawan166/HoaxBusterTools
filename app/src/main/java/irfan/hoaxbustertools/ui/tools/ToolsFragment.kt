package irfan.hoaxbustertools.ui.tools

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*


import irfan.hoaxbustertools.R

class ToolsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolsAdapter: ToolsAdapter
    private lateinit var menuName: String
    private val toolsList = mutableListOf<FirebaseContent>() // Initialize an empty list

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args = arguments
        menuName = args?.getString("menuName", "") ?: ""

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        // Set up RecyclerView and ToolsAdapter
        recyclerView = view.findViewById(R.id.recyclerViewTools)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        toolsAdapter = ToolsAdapter(requireContext(), toolsList)
        recyclerView.adapter = toolsAdapter

        // Fetch data from Firebase based on menuName
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
                        Log.d("FirebaseData", "desc_id: $desc_id, name_id: $name_id, image: $image, url: $url")
                        FirebaseContent(name_id, image, desc_id, url)
                    }
                    // Clear the existing list and add new data
                    toolsList.clear()
                    toolsList.addAll(contents)
                    toolsAdapter.notifyDataSetChanged() // Notify the adapter of data change
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }



}
