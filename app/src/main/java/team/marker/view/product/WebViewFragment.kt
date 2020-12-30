package team.marker.view.product

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_web_view.*
import kotlinx.android.synthetic.main.toolbar_file.*
import team.marker.R
import team.marker.util.Constants.PATH
import team.marker.util.Constants.TITLE
import team.marker.util.gone
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Objects.requireNonNull

class WebViewFragment : Fragment(R.layout.fragment_web_view) {

    private val filePath: String by lazy { arguments?.get(PATH) as String }
    private val fileTitle: String by lazy { arguments?.get(TITLE) as String }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)

        class PdfStream : AsyncTask<String?, Void?, InputStream?>() {
            override fun doInBackground(vararg strings: String?): InputStream? {
                var inputStream: InputStream? = null
                try {
                    val uri = URL(strings[0])
                    val urlConnection = uri.openConnection() as HttpURLConnection
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
                    .fitEachPage(true)
                    .onLoad { progress_bar.gone() }
                    .load()
            }
        }
        PdfStream().execute(filePath)

        btn_download.setOnClickListener { downloadFile(filePath, fileTitle) }
        btn_back.setOnClickListener { activity?.onBackPressed() }
    }

    private fun downloadFile(url: String, file_title: String?) {
        val downloadUrl: String = url
        val filename = "$file_title.pdf"

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setDescription("Document File")
            setTitle(filename)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(requireContext(), "/downloads", filename)
        }

        val manager = activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        requireNonNull(manager).enqueue(request)
    }
}