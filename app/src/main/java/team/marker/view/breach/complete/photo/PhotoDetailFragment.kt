package team.marker.view.breach.complete.photo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_photo_detail.*
import kotlinx.android.synthetic.main.toolbar_common.*
import team.marker.R
import team.marker.util.Constants.PHOTO_DETAIL
import team.marker.util.loadPhoto
import java.io.File

class PhotoDetailFragment : Fragment(R.layout.fragment_photo_detail) {

    private val file: File by lazy { arguments?.get(PHOTO_DETAIL) as File }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.loadPhoto(file, image)

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }
}