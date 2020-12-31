package team.marker.view.product

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_web_view.*
import kotlinx.android.synthetic.main.toolbar_file.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import team.marker.R
import team.marker.util.Constants.PATH
import team.marker.util.Constants.TITLE
import team.marker.util.gone
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class WebViewFragment : Fragment(R.layout.fragment_web_view) {

    private val filePath: String by lazy { arguments?.get(PATH) as String }
    private val fileTitle: String by lazy { arguments?.get(TITLE) as String }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            var inputStream: InputStream? = null
            val uri = URL(filePath)
            val urlConnection = uri.openConnection() as HttpURLConnection
            if (urlConnection.responseCode == 200) {
                inputStream = BufferedInputStream(urlConnection.inputStream)

                withContext(Dispatchers.Main) {
                    pdf_screen.fromStream(inputStream)
                        .defaultPage(0)
                        .spacing(0)
                        .fitEachPage(true)
                        .onLoad { progress_bar.gone() }
                        .load()
                }
            }
        }

        btn_download.setOnClickListener { downloadFile() }
        btn_back.setOnClickListener { activity?.onBackPressed() }
    }

    private fun downloadFile() {
        val filename = "$fileTitle.pdf"

        val request = DownloadManager.Request(Uri.parse(filePath)).apply {
            setDescription("Document File")
            setTitle(filename)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(requireContext(), "/downloads", filename)
        }

        val manager = activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}