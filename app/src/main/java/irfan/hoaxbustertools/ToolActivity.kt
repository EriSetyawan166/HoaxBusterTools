package irfan.hoaxbustertools

import DatabaseHelper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager

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
    private var webHistory: MutableList<String> = mutableListOf()

    fun setFavoriteChangeListener(listener: FavoriteChangeListener) {
        favoriteChangeListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool)
        
        toolbar = findViewById(R.id.toolbarTool)
        webView = findViewById(R.id.webViewTool)
        searchLayout = findViewById<LinearLayout>(R.id.searchLayout)
        searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchButton = findViewById<Button>(R.id.searchButton)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_close_24)

        dbHelper = DatabaseHelper(this)

        val url = intent.getStringExtra("url")
        val nameId = intent.getStringExtra("name_id")
        isSearchMode = intent.getBooleanExtra("is_search", false)

        supportActionBar?.title = nameId

        if (isSearchMode) {
            searchLayout.visibility = View.VISIBLE
            webView.visibility = View.GONE
        } else {
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
            val userInput = searchEditText.text.toString().trim()
            if (userInput.isNotEmpty()) {
                val baseUrl = intent.getStringExtra("url")
                if (baseUrl != null) {
                    val newUrl = baseUrl.replace("{search}", userInput)
                    webView.loadUrl(newUrl)
                    webView.visibility = View.VISIBLE
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                } else {

                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tool, menu)
        val toolId = intent.getStringExtra("name_id")
        if (toolId != null) {
            val isFavorited = dbHelper.isToolFavorite(toolId)
            updateFavoriteIcon(menu.findItem(R.id.action_tambah_favorit), isFavorited)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                finish()
                return true
            }
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

    override fun onBackPressed() {
        if (isSearchMode && !webHistory.isEmpty()) {
            val previousUrl = webHistory.removeAt(webHistory.size - 1)
            webView.loadUrl(previousUrl)
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
