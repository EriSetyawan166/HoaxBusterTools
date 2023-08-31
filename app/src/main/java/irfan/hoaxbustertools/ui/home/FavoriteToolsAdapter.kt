package irfan.hoaxbustertools.ui.home

import DatabaseHelper
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.ToolActivity
import irfan.hoaxbustertools.ui.tools.FirebaseContent

data class ToolItem(
    val name_id: String,
    val desc_id: String,
    val image: String,
    val url: String,
    var isFavorite: Boolean = false,
)

interface OnFavoriteStatusChangedListener {
    fun onFavoriteStatusChanged(tool: ToolItem)
}

class FavoriteToolsAdapter(private val favoriteTools: MutableList<ToolItem>,
                           private val dbHelper: DatabaseHelper,
                           private val listener: OnFavoriteStatusChangedListener) :
    RecyclerView.Adapter<FavoriteToolsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val toolNameTextView: TextView = itemView.findViewById(R.id.titleTextViewFavorite)
        val toolDescriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextViewFavorite)
        val toolImageView: ImageView = itemView.findViewById(R.id.imageViewFavorite)
        val favoriteButton: Button = itemView.findViewById(R.id.favoriteIconFavorite)
        val openToolsButton: Button = itemView.findViewById(R.id.bukaToolButtonFavorite)
        val context: Context = itemView.context
    }

    fun updateFavoriteStatus(toolId: String, isFavorite: Boolean) {
        val toolIndex = favoriteTools.indexOfFirst { it.name_id == toolId }
        if (toolIndex != -1) {
            val tool = favoriteTools[toolIndex]
            tool.isFavorite = isFavorite
            notifyItemChanged(toolIndex)
        }
    }

    fun removeTool(toolId: String) {
        val toolIndex = favoriteTools.indexOfFirst { it.name_id == toolId }
        if (toolIndex != -1) {
            favoriteTools.removeAt(toolIndex)
            notifyItemRemoved(toolIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_tools_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tool = favoriteTools[position]
        Log.d("FavoriteToolsAdapter", "FavoriteToolsAdapter $tool")
        holder.toolNameTextView.text = tool.name_id
        holder.toolDescriptionTextView.text = tool.desc_id

        Glide.with(holder.itemView.context)
            .load(tool.image)
            .into(holder.toolImageView)

        holder.favoriteButton.setBackgroundResource(
            if (dbHelper.isToolFavorite(tool.name_id)) R.drawable.favorite_star_yellow_24
            else R.drawable.favorite_star_shadow_24
        )

        holder.favoriteButton.setOnClickListener {
            val toolIndex = favoriteTools.indexOf(tool) // Get the current tool's index

            Log.d("FavoriteToolsAdapter", "Before Click - Index: $toolIndex, Size: ${favoriteTools.size}")

            val newFavoriteStatus = !dbHelper.isToolFavorite(tool.name_id)
            dbHelper.updateFavoriteStatus(tool.name_id, newFavoriteStatus)

            if (!newFavoriteStatus) {
                if (favoriteTools.size == 1) {
                    // Remove the last item and show empty state
                    favoriteTools.clear()
                    notifyDataSetChanged()
                    listener.onFavoriteStatusChanged(tool)
                } else {
                    // Remove the item from the list and notify the adapter
                    favoriteTools.removeAt(toolIndex)
                    notifyItemRemoved(toolIndex)

                    // Update the tool index to a valid value
                    val newToolIndex = toolIndex.coerceAtMost(favoriteTools.size - 1)

                    // Notify adapter about the change
                    notifyItemRangeChanged(newToolIndex, favoriteTools.size - newToolIndex)
                }
            } else {
                // Update the favorite status of the current tool and notify the adapter
                tool.isFavorite = newFavoriteStatus
                notifyItemChanged(toolIndex)
            }

            Log.d("FavoriteToolsAdapter", "After Click - Index: $toolIndex, Size: ${favoriteTools.size}")
        }


        holder.openToolsButton.setOnClickListener {
            val intent = Intent(holder.context, ToolActivity::class.java)
            intent.putExtra("url", tool.url)
            intent.putExtra("name_id", tool.name_id)
            holder.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return favoriteTools.size
    }
}
