package social.looscid.app

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Checks GitHub for a newer Looscid app when the app opens.
 *
 * Most Looscid updates never need this, because the app loads the live web
 * app. This is only for when the Android shell itself changes.
 *
 * Every step is wrapped so a bad network, odd release data or a missing
 * installer just skips the update. It never closes the app.
 */
class Updater(private val activity: Activity) {

    private data class Release(val version: String, val notes: String, val apkUrl: String)

    fun checkForUpdate() {
        thread {
            val release = try { fetchLatestRelease() } catch (e: Exception) { null }
            if (release != null && isNewer(release.version, BuildConfig.VERSION_NAME)) {
                activity.runOnUiThread { safely { showUpdateDialog(release) } }
            }
        }
    }

    private fun fetchLatestRelease(): Release? {
        val conn = URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val assets = json.getJSONArray("assets")
            val apk = (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk") } ?: return null
            return Release(
                version = json.getString("tag_name").removePrefix("v"),
                notes = json.optString("body").trim(),
                apkUrl = apk.getString("browser_download_url")
            )
        } finally {
            conn.disconnect()
        }
    }

    /** A plain Android dialog, so TalkBack and braille displays read it like any system alert. */
    private fun showUpdateDialog(release: Release) {
        if (activity.isFinishing || activity.isDestroyed) return
        val notes = release.notes.ifBlank { activity.getString(R.string.update_no_notes) }
        AlertDialog.Builder(activity)
            .setTitle(R.string.update_title)
            .setMessage(activity.getString(R.string.update_message, release.version, notes))
            .setPositiveButton(R.string.update_button) { _, _ -> safely { download(release) } }
            .setNegativeButton(R.string.later_button, null)
            .show()
    }

    private fun download(release: Release) {
        val app = activity.applicationContext
        val file = File(app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_NAME)
        file.delete()

        val downloads = app.getSystemService(DownloadManager::class.java)
        val id = downloads.enqueue(
            DownloadManager.Request(Uri.parse(release.apkUrl))
                .setTitle("Looscid ${release.version}")
                .setMimeType(APK_MIME)
                .setDestinationInExternalFilesDir(app, Environment.DIRECTORY_DOWNLOADS, APK_NAME)
        )
        Toast.makeText(app, R.string.update_downloading, Toast.LENGTH_LONG).show()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) != id) return
                safely {
                    context.unregisterReceiver(this)
                    if (succeeded(downloads, id)) install(context, file)
                    else Toast.makeText(context, R.string.update_failed, Toast.LENGTH_LONG).show()
                }
            }
        }
        ContextCompat.registerReceiver(
            app, receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun succeeded(downloads: DownloadManager, id: Long): Boolean =
        downloads.query(DownloadManager.Query().setFilterById(id)).use { c ->
            c.moveToFirst() &&
                c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL
        }

    /** Opens Android's own installer. The first time, Android asks to allow installs from Looscid. */
    private fun install(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, APK_MIME)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Runs [block], and if anything goes wrong, skips it quietly instead of crashing. */
    private inline fun safely(block: () -> Unit) {
        try { block() } catch (e: Exception) { /* no update this time */ }
    }

    companion object {
        private const val LATEST_RELEASE_API =
            "https://api.github.com/repos/Looscid/Looscid-Android/releases/latest"
        private const val APK_NAME = "looscid-update.apk"
        private const val APK_MIME = "application/vnd.android.package-archive"

        /** True when [remote] (like "0.2.0") is a higher version than [local]. */
        fun isNewer(remote: String, local: String): Boolean {
            val a = remote.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
            val b = local.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
            for (i in 0 until maxOf(a.size, b.size)) {
                val x = a.getOrElse(i) { 0 }
                val y = b.getOrElse(i) { 0 }
                if (x != y) return x > y
            }
            return false
        }
    }
}
