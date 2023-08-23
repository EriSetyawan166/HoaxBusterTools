package irfan.hoaxbustertools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ToolActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool)

        // Find views
        toolbar = findViewById(R.id.toolbarTool)
        webView = findViewById(R.id.webViewTool)

        // Set up Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get URL and load it in WebView
        val url = intent.getStringExtra("url")
        val nameId = intent.getStringExtra("name_id")
        Log.d("ToolActivity", "Received URL: $url")
        Log.d("ToolActivity", "Received Name ID: $nameId")
        supportActionBar?.title = nameId


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

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tool, menu)
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
                    Toast.makeText(this, "Link telah disalin", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.action_bagikan_link -> {
                val url = intent.getStringExtra("url")
                if (!url.isNullOrBlank()) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url)
                    startActivity(Intent.createChooser(shareIntent, "Bagikan link melalui"))
                }
                return true
            }

            R.id.action_tambah_favorit -> {
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
