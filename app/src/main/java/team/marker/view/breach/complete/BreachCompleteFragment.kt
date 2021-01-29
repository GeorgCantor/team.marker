package team.marker.view.breach.complete

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
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
import team.marker.util.hasInternetBeforeAction

class BreachCompleteFragment : Fragment(R.layout.fragment_breach_complete) {

    private val viewModel by sharedViewModel<BreachCompleteViewModel>()
    private val productIds: ArrayList<String> by lazy { arguments?.get(PRODUCT_IDS) as ArrayList<String> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            photos_recycler.adapter = PhotosAdapter(it) { file ->
                findNavController().navigate(
                    R.id.action_breachCompleteFragment_to_photoDetailFragment,
                    Bundle().apply { putSerializable(PHOTO_DETAIL, file) }
                )
            }
            photos_recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        }

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
        super.onDetach()
    }

    private fun send(productId: Int) {
        if (!requireContext().hasInternetBeforeAction()) return
        val reasonId = 0
        val userReason = ""
        val comment = input_comment.text.toString().trim()
        if (comment == "") {
            comment_input_view.error = getString(R.string.enter_description)
            return
        }
        viewModel.breach(productId, reasonId, userReason, comment)
        activity?.onBackPressed()
    }
}