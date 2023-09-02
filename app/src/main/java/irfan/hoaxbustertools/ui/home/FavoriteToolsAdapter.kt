package irfan.hoaxbustertools.ui.home

import DatabaseHelper
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.ToolActivity
import irfan.hoaxbustertools.ui.tools.FirebaseContent
import java.util.Locale

data class ToolItem(
    val nameId: String,
    val name_id: String,
    val name_eng: String,
    val desc_id: String,
    val desc_eng: String,
    val image: String,
    val url: String,
    val is_search: Boolean = false,
    val using_browser_default: Boolean = false,
    var isFavorite: Boolean = false
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
        val toolIndex = favoriteTools.indexOfFirst { it.nameId == toolId }
        if (toolIndex != -1) {
            val tool = favoriteTools[toolIndex]
            tool.isFavorite = isFavorite
            notifyItemChanged(toolIndex)
        }
    }

    fun removeTool(toolId: String) {
        val toolIndex = favoriteTools.indexOfFirst { it.nameId == toolId }
        if (toolIndex != -1) {
            favoriteTools.removeAt(toolIndex)
            notifyItemRemoved(toolIndex)
        }
    }

    fun getCurrentLanguage(context: Context): String {
        return context.resources.configuration.locales[0].language
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_tools_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentLanguage = getCurrentLanguage(holder.context)

        Log.d("language", "currentLanguage $currentLanguage")
        val tool = favoriteTools[position]



        val toolName = when(currentLanguage) {
            "in" -> tool.name_id
            "en" -> tool.name_eng
            else -> tool.name_eng // Default to English
        }

        val toolDescription = when(currentLanguage) {
            "in" -> tool.desc_id
            "en" -> tool.desc_eng
            else -> tool.desc_eng // Default to English
        }

        Log.d("favorite", "name ${toolName}")
        Log.d("favorite", "desc ${toolDescription}")


        Log.d("FavoriteToolsAdapter", "FavoriteToolsAdapter $tool")
        holder.toolNameTextView.text = toolName
        holder.toolDescriptionTextView.text = toolDescription

        Glide.with(holder.itemView.context)
            .load(tool.image)
            .into(holder.toolImageView)

        holder.favoriteButton.setBackgroundResource(
            if (dbHelper.isToolFavorite(tool.nameId)) R.drawable.favorite_star_yellow_24
            else R.drawable.favorite_star_shadow_24
        )

        holder.favoriteButton.setOnClickListener {
            val toolIndex = favoriteTools.indexOf(tool) // Get the current tool's index

            Log.d("FavoriteToolsAdapter", "Before Click - Index: $toolIndex, Size: ${favoriteTools.size}")

            val newFavoriteStatus = !dbHelper.isToolFavorite(tool.nameId)
            dbHelper.updateFavoriteStatus(tool.nameId, newFavoriteStatus)

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
            if(tool.using_browser_default){
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tool.url))
                holder.context.startActivity(browserIntent)
            } else{
                val intent = Intent(holder.context, ToolActivity::class.java)
                intent.putExtra("url", tool.url)
                intent.putExtra("name_id", toolName)
                intent.putExtra("is_search", tool.is_search)
                holder.context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int {
        return favoriteTools.size
    }
}
