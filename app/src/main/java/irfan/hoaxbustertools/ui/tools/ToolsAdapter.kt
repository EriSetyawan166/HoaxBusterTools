package irfan.hoaxbustertools.ui.tools

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.ToolActivity

data class FirebaseContent(
    val name_id: String,
    val image: String,
    val desc_id: String,
    val url: String,
)

class ToolsAdapter(private val context: Context, private val toolsList: List<FirebaseContent>) :
    RecyclerView.Adapter<ToolsAdapter.ToolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_tools, parent, false)
        return ToolViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val currentTool = toolsList[position]

        // Set data to views in the CardView
        holder.titleTextView.text = currentTool.name_id
        holder.descriptionTextView.text = currentTool.desc_id
        // Set image using Glide or other image loading library
        Glide.with(holder.itemView.context)
            .load(currentTool.image)
            .into(holder.imageView)

        // Set click listener for "Buka Tool" button
//        holder.bukaToolButton.setOnClickListener {
//            val fragment = ToolFragment.newInstance(currentTool.url, currentTool.name_id)
//            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
//            fragmentManager.beginTransaction()
//                .replace(R.id.fragment_container_tool, fragment) // Use the correct container ID
//                .addToBackStack(null)
//                .commit()
//        }

        holder.bukaToolButton.setOnClickListener {
            val intent = Intent(context, ToolActivity::class.java)
            intent.putExtra("url", currentTool.url)
            intent.putExtra("name_id", currentTool.name_id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = toolsList.size

    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val bukaToolButton: Button = itemView.findViewById(R.id.bukaToolButton)
    }
}
