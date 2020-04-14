package team.marker.view.product

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.toolbar_file.*
import team.marker.R
import team.marker.util.shortToast
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class WebViewActivity : AppCompatActivity() {

    private val pdfview: PDFView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_webview)
        // включаем поддержку JavaScript
        //wv.settings.javaScriptEnabled = true
        // указываем страницу загрузки
        //wv.loadUrl("https://marker.team/servers/media/storage/2020/0325/3ef4431134.pdf")

        val file_path = intent.getStringExtra("path")
        val file_title = intent.getStringExtra("title")
        //Log.e("Message", path)

        /*wv.getSettings().setJavaScriptEnabled(true)
        wv.getSettings().setPluginState(WebSettings.PluginState.ON)
        wv.setWebViewClient(WebViewClient())
        val fileUrl = path
        wv.loadUrl("http://docs.google.com/gview?embedded=true&url=$fileUrl")*/

        //val myUri = Uri.parse("https://marker.team/servers/media/storage/2020/0325/3ef4431134.pdf")
        //pdfView.fromUri(myUri).load()
        //val pdf_screen: PDFView = findViewById(R.id.pdf_screen);

        class RetrivePdfStream : AsyncTask<String?, Void?, InputStream?>() {

            override fun doInBackground(vararg strings: String?): InputStream? {
                var inputStream: InputStream? = null
                try {
                    val uri = URL(strings[0])
                    val urlConnection =
                        uri.openConnection() as HttpURLConnection
                    if (urlConnection.responseCode == 200) {
                        inputStream = BufferedInputStream(urlConnection.inputStream)
                    }
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
                    //.autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
                    //.linkHandler(DefaultLinkHandler)
                    //.pageFitPolicy(FitPolicy.WIDTH)
                    .fitEachPage(true)
                    .load()
            }
        }

        RetrivePdfStream().execute(file_path)


        val output = File(this.filesDir.path + this.packageName + "/download/")
        btn_download.setOnClickListener { downloadFile(file_path, file_title, output) }

        btn_back.setOnClickListener { back() }

    }

    private fun downloadFile(url: String?, file_title: String?, outputFile: File) {
        val DownloadUrl: String = url!!
        val filename = "$file_title.pdf"
        val request1 = DownloadManager.Request(Uri.parse(DownloadUrl))
        request1.setDescription("Document File") //appears the same in Notification bar while downloading

        request1.setTitle(filename)
        //request1.setVisibleInDownloadsUi(false)

        //request1.allowScanningByMediaScanner()
        //request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request1.setDestinationInExternalFilesDir(applicationContext, "/downloads", filename)

        val manager1 =
            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        Objects.requireNonNull(manager1).enqueue(request1)
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            shortToast("Загрузка завершена")
            //request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            //DownloadSuccess()
        }
    }

    private fun back() {
        finish()
    }

}