package irfan.hoaxbustertools

import DatabaseHelper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Locale

interface FavoriteChangeListener {
    fun onFavoriteChanged(nameId: String, isFavorite: Boolean)
}

class ToolActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private var isSearchMode: Boolean = false
    private var favoriteChangeListener: FavoriteChangeListener? = null

    fun setFavoriteChangeListener(listener: FavoriteChangeListener) {
        favoriteChangeListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool)


        // Find views
        toolbar = findViewById(R.id.toolbarTool)
        webView = findViewById(R.id.webViewTool)
        searchLayout = findViewById<LinearLayout>(R.id.searchLayout)
        searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchButton = findViewById<Button>(R.id.searchButton)

        // Set up Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)

        // Get URL and load it in WebView
        val url = intent.getStringExtra("url")
        val nameId = intent.getStringExtra("name_id")
        isSearchMode = intent.getBooleanExtra("is_search", false)
        Log.d("ToolActivity", "Received URL: $url")
        Log.d("ToolActivity", "Received Name ID: $nameId")
        Log.d("ToolActivity", "Received is_search: $isSearchMode")


        supportActionBar?.title = nameId

        if (isSearchMode) {
            // Jika dalam mode pencarian, tampilkan elemen pencarian
            searchLayout.visibility = View.VISIBLE
            webView.visibility = View.GONE
        } else {
            // Jika bukan dalam mode pencarian, tampilkan WebView
            searchLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
        }





        if (!url.isNullOrBlank()) {
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { nonNullUrl ->
                        view?.loadUrl(nonNullUrl)
                    }
                    return true
                }
            }
        }

        searchButton.setOnClickListener {
            Log.d("SearchButton", "Button clicked")
            val userInput = searchEditText.text.toString().trim()
            if (userInput.isNotEmpty()) {
                val baseUrl = intent.getStringExtra("url")
                if (baseUrl != null) {
                    val newUrl = baseUrl.replace("{search}", userInput)
                    Log.d("SearchButton", "New URL: $newUrl")
                    webView.loadUrl(newUrl)
                    webView.visibility = View.VISIBLE
                } else {
                    // Handle the case where baseUrl is null
                }
            }
        }



    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tool, menu)
        val toolId = intent.getStringExtra("name_id")
        if (toolId != null) {
            val isFavorited = dbHelper.isToolFavorite(toolId)
            Log.d("ToolActivity", "isFavorited: $isFavorited")
            updateFavoriteIcon(menu.findItem(R.id.action_tambah_favorit), isFavorited)
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_buka_browser -> {
                val url = intent.getStringExtra("url")
                if (!url.isNullOrBlank()) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                }
                return true
            }
            R.id.action_salin_link -> {
                val url = intent.getStringExtra("url")
                if (!url.isNullOrBlank()) {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("URL", url)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, getString(R.string.salin), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.action_bagikan_link -> {
                val url = intent.getStringExtra("url")
                if (!url.isNullOrBlank()) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url)
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                }
                return true
            }

            R.id.action_tambah_favorit -> {
                val toolId = intent.getStringExtra("name_id")
                if (toolId != null) {
                    val isFavorited = dbHelper.isToolFavorite(toolId)
                    val newFavoriteStatus = !isFavorited
                    dbHelper.updateFavoriteStatus(toolId, newFavoriteStatus)
                    updateFavoriteIcon(item, newFavoriteStatus)

                    // Notify ToolsFragment about the favorite change
                    val intent = Intent("favorite_changed")
                    intent.putExtra("name_id", toolId)
                    intent.putExtra("is_favorite", newFavoriteStatus)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun getFavoriteTools(): Set<String> {
        val preferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        return preferences.getStringSet("favorites", setOf()) ?: setOf()
    }

    private fun updateFavoriteIcon(menuItem: MenuItem, isFavorited: Boolean) {
        if (isFavorited) {
            menuItem.setIcon(R.drawable.favorite_star_yellow_24) // Set filled icon
        } else {
            menuItem.setIcon(R.drawable.favorite_star_shadow_24) // Set shadow icon
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}
