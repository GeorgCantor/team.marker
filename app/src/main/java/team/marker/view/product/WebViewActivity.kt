package team.marker.view.product

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.toolbar_file.*
import team.marker.R
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // common
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        // vars
        val filePath = intent.getStringExtra("path")
        val fileTitle = intent.getStringExtra("title")
        // stream
        class PdfStream : AsyncTask<String?, Void?, InputStream?>() {

            override fun doInBackground(vararg strings: String?): InputStream? {
                var inputStream: InputStream? = null
                try {
                    val uri = URL(strings[0])
                    val urlConnection = uri.openConnection() as HttpURLConnection
                    if (urlConnection.responseCode == 200) inputStream = BufferedInputStream(urlConnection.inputStream)
                } catch (e: IOException) {
                    return null
                }
                return inputStream
            }

            override fun onPostExecute(inputStream: InputStream?) {
                super.onPostExecute(inputStream)
                pdf_screen.fromStream(inputStream)
                    .defaultPage(0)
                    .spacing(0)
                    .fitEachPage(true)
                    .onLoad { progress_bar.visibility = View.GONE }
                    .load()
            }
        }
        PdfStream().execute(filePath)
        // listeners
        btn_download.setOnClickListener { downloadFile(filePath, fileTitle) }
        btn_back.setOnClickListener { back() }
    }

    private fun downloadFile(url: String?, file_title: String?) {
        // vars
        val downloadUrl: String = url!!
        val filename = "$file_title.pdf"
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
        // request
        request.setDescription("Document File")
        request.setTitle(filename)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(applicationContext, "/downloads", filename)
        //request.setVisibleInDownloadsUi(false)
        //request.allowScanningByMediaScanner()
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        // manager
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        Objects.requireNonNull(manager).enqueue(request)
        //if (DownloadManager.STATUS_SUCCESSFUL == 8) shortToast("Загрузка завершена")
    }

    private fun back() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}