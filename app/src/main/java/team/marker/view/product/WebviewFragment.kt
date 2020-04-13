package team.marker.view.product

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_webview.*
import team.marker.R
import java.io.File


class WebviewFragment : Fragment() {

    private val path: String by lazy { arguments?.get("path") as String }

    private var mFileDescriptor: ParcelFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_webview, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("Message", "open file $path")
        //val myUri = Uri.parse("https://marker.team/servers/media/storage/2020/0325/3ef4431134.pdf")
        //pdfView.fromUri(myUri).load();

        /*wv.getSettings().setJavaScriptEnabled(true)
        wv.getSettings().setPluginState(WebSettings.PluginState.ON)
        wv.setWebViewClient(WebViewClient())
        val fileUrl = path
        wv.loadUrl("http://docs.google.com/gview?embedded=true&url=$fileUrl")*/

        /*val file = File("https://marker.team/servers/media/storage/2020/0325/3ef4431134.pdf")
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        val renderer = PdfRenderer(mFileDescriptor)

        val pageCount = renderer.pageCount
        for (i in 0 until pageCount) {
            val page: PdfRenderer.Page = renderer.openPage(i)
            page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
        }
        renderer.close()*/

    }
}