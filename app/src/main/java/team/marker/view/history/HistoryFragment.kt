package team.marker.view.history

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.android.ext.android.inject
import team.marker.R

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel by inject<HistoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getHistory()

        viewModel.progressIsVisible.observe(viewLifecycleOwner, {
            progress_bar.isVisible = it
        })

        viewModel.response.observe(viewLifecycleOwner, {
            history_recycler.adapter = HistoryAdapter(it.info ?: mutableListOf()) {
            }
        })

        btn_back.setOnClickListener { back() }
    }

    private fun back() {
        activity?.onBackPressed()
    }
}