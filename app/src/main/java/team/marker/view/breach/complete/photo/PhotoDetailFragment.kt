package team.marker.view.breach.complete.photo

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import kotlinx.android.synthetic.main.fragment_photo_detail.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.util.Constants.PHOTO_DETAIL
import team.marker.util.loadPhoto
import team.marker.view.breach.complete.BreachCompleteViewModel
import java.io.File

class PhotoDetailFragment : Fragment(R.layout.fragment_photo_detail) {

    private val viewModel by sharedViewModel<BreachCompleteViewModel>()
    private val file: File by lazy { arguments?.get(PHOTO_DETAIL) as File }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(image, file.name)

        context?.loadPhoto(file, image)

        btn_back.setOnClickListener { activity?.onBackPressed() }

        btn_remove.setOnClickListener {
            viewModel.removePhoto(file)
            activity?.onBackPressed()
        }
    }
}