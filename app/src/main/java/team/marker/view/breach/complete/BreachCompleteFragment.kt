package team.marker.view.breach.complete

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_breach_complete.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import team.marker.R

class BreachCompleteFragment : Fragment(R.layout.fragment_breach_complete) {

    private lateinit var viewModel: BreachCompleteViewModel
    private val productIds: ArrayList<String> by lazy { arguments?.get("product_ids") as ArrayList<String> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getSharedViewModel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (productId in productIds) {
            Log.e("Message", productId)
        }
        var productId = 0
        if (productIds.size > 0) productId = productIds[0].toInt()

        btn_back.setOnClickListener { activity?.onBackPressed() }
        btn_send.setOnClickListener { send(view, productId) }
        add_photo_btn.setOnClickListener { findNavController().navigate(R.id.action_breachCompleteFragment_to_photoFragment) }

        viewModel.photos.observe(viewLifecycleOwner, Observer {
            photos_recycler.setHasFixedSize(true)
            photos_recycler.adapter = PhotosAdapter(it)
            photos_recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        })
    }

    override fun onDetach() {
        viewModel.photos.value = mutableListOf()

        super.onDetach()
    }

    private fun send(view: View, productId: Int) {
        // vars
        val reasonId = 0
        val userReason = ""
        val comment = input_comment.text.toString()
        // validate
        if (comment == "") return
        // send
        viewModel.breach(productId, reasonId, userReason, comment)
        Navigation.findNavController(view).navigate(R.id.action_breachCompleteFragment_to_homeFragment)
    }
}