package team.marker.view.breach.complete

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_breach_complete.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.util.Constants.PHOTO_DETAIL
import team.marker.util.Constants.PRODUCT_IDS
import team.marker.util.isNetworkAvailable
import team.marker.util.longToast

class BreachCompleteFragment : Fragment(R.layout.fragment_breach_complete) {

    private val viewModel by sharedViewModel<BreachCompleteViewModel>()
    private val productIds: ArrayList<String> by lazy { arguments?.get(PRODUCT_IDS) as ArrayList<String> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.sending_report)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_breachCompleteFragment_to_homeFragment)
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        var productId = 0
        try {
            if (productIds.size > 0 && productIds[0] != "") productId = productIds[0].toInt()
        } catch (e: NumberFormatException) {
        }

        btn_back.setOnClickListener { activity?.onBackPressed() }
        btn_send.setOnClickListener { send(productId) }
        add_photo_btn.setOnClickListener { findNavController().navigate(R.id.action_breachCompleteFragment_to_photoFragment) }

        viewModel.photos.observe(viewLifecycleOwner) {
            photos_recycler.setHasFixedSize(true)
            photos_recycler.adapter = PhotosAdapter(it) { file, extras ->
                findNavController().navigate(
                    R.id.action_breachCompleteFragment_to_photoDetailFragment,
                    bundleOf(PHOTO_DETAIL to file),
                    null,
                    extras
                )
            }
            photos_recycler.layoutManager = GridLayoutManager(requireContext(), 3)
            postponeEnterTransition()
            photos_recycler.viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { it?.let { context?.longToast(it) } }

        viewModel.sentSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                context?.longToast(getString(R.string.breach_request_sent))
                activity?.onBackPressed()
            }
        }

        viewModel.progressIsVisible.observe(viewLifecycleOwner) { progress_bar.isVisible = it }

        input_comment.doOnTextChanged { text, _, _, _ ->
            when (text?.isBlank()) {
                true -> comment_input_view.error = getString(R.string.enter_description)
                false -> comment_input_view.error = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        comment_input_view.error = null
    }

    override fun onDetach() {
        viewModel.removeFiles()
        viewModel.sentSuccess.value = false
        super.onDetach()
    }

    private fun send(productId: Int) {
        val reasonId = 0
        val userReason = ""
        val comment = input_comment.text.toString().trim()
        if (comment == "") {
            comment_input_view.error = getString(R.string.enter_description)
            return
        }
        if (context?.isNetworkAvailable() == true) {
            viewModel.breach(productId, reasonId, userReason, comment)
        } else {
            viewModel.saveFilePathsForDeferredSending(productId, comment)
            context?.longToast(getString(R.string.send_later))
            activity?.onBackPressed()
        }
    }
}