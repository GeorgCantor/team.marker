package team.marker.view.breach.complete.photo

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat.setTransitionName
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater.from
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_photo_detail.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.util.Constants.PHOTO_FILES
import team.marker.util.runDelayed
import team.marker.view.breach.complete.BreachCompleteViewModel
import java.io.File

class PhotoDetailFragment : Fragment(R.layout.fragment_photo_detail) {

    private val viewModel by sharedViewModel<BreachCompleteViewModel>()
    private val files: Pair<File, List<File>> by lazy { arguments?.get(PHOTO_FILES) as Pair<File, List<File>> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = from(context).inflateTransition(android.R.transition.move)
        setTransitionName(view_pager, files.first.name)

        val adapter = PagerAdapter(files.second)
        view_pager.adapter = adapter
        50L.runDelayed { view_pager.currentItem = files.second.indexOf(files.first) }

        TabLayoutMediator(tab_layout, view_pager) { _, _ -> }.attach()

        btn_back.setOnClickListener { activity?.onBackPressed() }

        btn_remove.setOnClickListener {
            viewModel.removePhoto(files.second[view_pager.currentItem])
            adapter.notifyDataSetChanged()
            if (adapter.itemCount == 0) activity?.onBackPressed()
        }
    }
}