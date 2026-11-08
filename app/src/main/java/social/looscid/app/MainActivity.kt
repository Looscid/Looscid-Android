package social.looscid.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * The whole app: one full-screen WebView showing the live Looscid web app.
 *
 * Accessibility note: we never override key handling here. TalkBack, braille
 * displays and keyboards talk straight to the web page, exactly like Chrome.
 */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private var pendingFilePick: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = LooscidClient()
            webChromeClient = FilePickerClient()
        }

        // Draw edge to edge, then pad so nothing hides under the status bar,
        // the navigation bar or the on-screen keyboard.
        val root = FrameLayout(this).apply { addView(web) }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        setContentView(root)

        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) {
            web.loadUrl(HOME_URL)
        }

        Updater(this).checkForUpdate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    /** Back goes back inside Looscid first, then leaves the app. */
    @Deprecated("Still the simplest back handler for a plain Activity.")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }

    /** Looscid pages stay in the app. Every other link opens in the browser. */
    private inner class LooscidClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (request.url.host == HOME_HOST) return false
            try {
                startActivity(Intent(Intent.ACTION_VIEW, request.url))
            } catch (e: ActivityNotFoundException) {
                // Nothing on the phone can open it. Stay put.
            }
            return true
        }
    }

    /** Lets "choose a file" buttons on the web page open the phone's file picker. */
    private inner class FilePickerClient : WebChromeClient() {
        override fun onShowFileChooser(
            view: WebView,
            callback: ValueCallback<Array<Uri>>,
            params: FileChooserParams
        ): Boolean {
            pendingFilePick?.onReceiveValue(null)
            pendingFilePick = callback
            return try {
                startActivityForResult(params.createIntent(), PICK_FILE)
                true
            } catch (e: ActivityNotFoundException) {
                pendingFilePick = null
                false
            }
        }
    }

    @Deprecated("Matches startActivityForResult on a plain Activity.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE) {
            pendingFilePick?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            pendingFilePick = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val HOME_URL = "https://looscid.github.io/Looscid/"
        const val HOME_HOST = "looscid.github.io"
        private const val PICK_FILE = 1
    }
}
