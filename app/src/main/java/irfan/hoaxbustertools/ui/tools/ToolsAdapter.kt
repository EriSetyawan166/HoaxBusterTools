package irfan.hoaxbustertools.ui.tools

import DatabaseHelper
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import irfan.hoaxbustertools.LocaleUtil
import irfan.hoaxbustertools.MyApp
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.ToolActivity

data class FirebaseContent(
    val name_id: String,
    val image: String,
    val desc_id: String,
    val url: String,
    val name_eng: String,
    val desc_eng: String,
    val is_search: Boolean = false,
    val using_browser_default: Boolean = false,
    var isFavorite: Boolean = false
)


class ToolsAdapter(private val context: Context, private val toolsList: List<FirebaseContent>) :
    RecyclerView.Adapter<ToolsAdapter.ToolViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var favoriteChangeListener: FavoriteChangeListener? = null
    private var currentLanguage: String

    init {
        val myApp = context.applicationContext as MyApp
        val storage = myApp.storage
        val currentLocale = LocaleUtil.getLocaleFromPrefCode(storage.getPreferredLocale())
        currentLanguage = currentLocale.toString()
    }

    interface FavoriteChangeListener {
        fun onFavoriteChanged(nameId: String, isFavorite: Boolean)
    }

    fun setFavoriteChangeListener(listener: FavoriteChangeListener) {
        favoriteChangeListener = listener
    }

    fun setSharedPreferences(sharedPrefs: SharedPreferences) {
        sharedPreferences = sharedPrefs
    }

    fun setDatabaseHelper(databaseHelper: DatabaseHelper) {
        dbHelper = databaseHelper
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_tools, parent, false)
        return ToolViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val currentTool = toolsList[position]

        val toolName = when(currentLanguage) {
            "in" -> currentTool.name_id
            "en" -> currentTool.name_eng
            else -> currentTool.name_eng
        }

        val toolDescription = when(currentLanguage) {
            "in" -> currentTool.desc_id
            "en" -> currentTool.desc_eng
            else -> currentTool.desc_eng
        }

        holder.titleTextView.text = toolName
        holder.descriptionTextView.text = toolDescription

        Glide.with(holder.itemView.context)
            .load(currentTool.image)
            .into(holder.imageView)

        holder.favoriteIcon.setBackgroundResource(
            if (dbHelper.isToolFavorite(currentTool.name_id)) R.drawable.favorite_star_yellow_24
            else R.drawable.favorite_star_shadow_24
        )

        holder.favoriteIcon.setOnClickListener {
            val newFavoriteStatus = !dbHelper.isToolFavorite(currentTool.name_id)
            dbHelper.updateFavoriteStatus(currentTool.name_id, newFavoriteStatus)
            notifyItemChanged(position)
            holder.favoriteIcon.setBackgroundResource(
                if (newFavoriteStatus) R.drawable.favorite_star_yellow_24
                else R.drawable.favorite_star_shadow_24
            )
        }

        holder.bukaToolButton.setOnClickListener {
            if (currentTool.using_browser_default){
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentTool.url))
                holder.context.startActivity(browserIntent)
            } else{
                val intent = Intent(context, ToolActivity::class.java)
                intent.putExtra("url", currentTool.url)
                intent.putExtra("name_id", toolName)
                intent.putExtra("is_search", currentTool.is_search)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = toolsList.size

    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val bukaToolButton: Button = itemView.findViewById(R.id.bukaToolButton)
        val favoriteIcon: Button = itemView.findViewById(R.id.favoriteIcon)
        val context: Context = itemView.context
    }
}
