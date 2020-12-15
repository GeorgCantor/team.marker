package team.marker.view.breach.complete

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_breach_complete.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import team.marker.R
import team.marker.util.Constants.PRODUCT_IDS

class BreachCompleteFragment : Fragment(R.layout.fragment_breach_complete) {

    private lateinit var viewModel: BreachCompleteViewModel
    private val productIds: ArrayList<String> by lazy { arguments?.get(PRODUCT_IDS) as ArrayList<String> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getSharedViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var productId = 0
        if (productIds.size > 0 && productIds[0] != "") productId = productIds[0].toInt()

        btn_back.setOnClickListener { activity?.onBackPressed() }
        btn_send.setOnClickListener { send(productId) }
        add_photo_btn.setOnClickListener { findNavController().navigate(R.id.action_breachCompleteFragment_to_photoFragment) }

        viewModel.photos.observe(viewLifecycleOwner) {
            photos_recycler.setHasFixedSize(true)
            photos_recycler.adapter = PhotosAdapter(it)
            photos_recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)
    }

    override fun onDetach() {
        viewModel.photos.value = mutableListOf()
        val cw = ContextWrapper(requireContext())
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        directory.deleteRecursively()
        super.onDetach()
    }

    private fun send(productId: Int) {
        val reasonId = 0
        val userReason = ""
        val comment = input_comment.text.toString()
        if (comment == "") return
        viewModel.breach(productId, reasonId, userReason, comment)
        findNavController().navigate(R.id.action_breachCompleteFragment_to_homeFragment)
    }
}